package scrapper

import models.Movie
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{element, elementList}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}

object FilmwebScrapper {

  val browser: Browser = JsoupBrowser()
  val filmwebHost = "https://www.filmweb.pl"
  val filmsEndpoint = "/films"
  val serialsEndpoint = "/serials"

  def doScrapping()(implicit request: Request[AnyContent]): String = {
    val form = request.body.asFormUrlEncoded
    val phrases = form.get("phrase").filter(_.nonEmpty).distinct
    val result = phrases.map(phrase => phrase -> scrapPhrase(phrase)).toMap
    Json.toJson(result.map(r => r._1 -> r._2.length)).toString()
  }

  def scrapPhrase(phrase: String): Seq[Movie] = {
    scrapFilms(phrase) ++ scrapSerials(phrase)
  }

  def scrapFilms(phrase: String): Seq[Movie] = {
    scrapMovies(filmsEndpoint, phrase)
  }

  def scrapSerials(phrase: String): Seq[Movie] = {
    scrapMovies(serialsEndpoint, phrase)
  }

  def scrapMovies(endpoint: String, phrase: String): Seq[Movie] = {
    val firstPage = browser.get(filmwebHost + endpoint + "/search?q=" + phrase)
    val resultsCount = (firstPage >?> element(".resultsHeader")).flatMap(_ >?> text("span").map(_.replace(" ", "").toInt))
    if (resultsCount.isEmpty) {
      Seq.empty
    } else {
      val numberOfPages = (resultsCount.get / 10) + 1 // i.e. 4 results means 1 page, not 0, that's why +1
      if (numberOfPages < 2) {
        scrapMoviesFromPage(firstPage)
      } else {
        val pagination = firstPage >> element(".pagination__list")
        val nextPagesEndpoint = (pagination >> attr("href")("a")).dropRight(1)
        val remainingPages = getRemainingPages(filmwebHost + endpoint + "/search" + nextPagesEndpoint, 2 to numberOfPages)
        val allPages = firstPage +: remainingPages
        allPages.flatMap(scrapMoviesFromPage)
      }
    }
  }

  def getRemainingPages(url: String, pagesRange: Range): Seq[FilmwebScrapper.browser.DocumentType] = {
    pagesRange.map(pageNumber => browser.get(url + pageNumber))
  }

  def scrapMoviesFromPage(page: FilmwebScrapper.browser.DocumentType): Seq[Movie] = {
    val searchResult = page >?> element("#searchResult")
    val pageContainer = searchResult.flatMap(_ >?> element(".page__container"))
    val htmlList = pageContainer.flatMap(_ >?> element(".resultsList"))
    val moviesList = htmlList.flatMap(_ >?> elementList(".hits__item")).getOrElse(List.empty)
    moviesList.map(extractMovie)
  }

  def extractMovie(html: Element): Movie = {
    val badge = html >?> text(".filmPreview__badge")
    val title = html >?> text(".filmPreview__titleDetails")
    val originalTitle = html >?> text(".filmPreview__originalTitle")
    val year = (html >?> text(".filmPreview__year")).flatMap(_.toIntOption)
    val rate = (html >?> text(".rateBox__rate")).flatMap(_.replace(",", ".").toDoubleOption)
    val countOfRates = (html >?> attr("content")(".rateBox__votes--count")).flatMap(_.toIntOption)
    val genres = (html >?> element(".filmPreview__info--genres")).flatMap(_ >?> texts("a")).getOrElse(Seq.empty)
    val directors = (html >?> element(".filmPreview__info--directors")).flatMap(_ >?> texts("a")).getOrElse(Seq.empty)
    val countries = (html >?> element(".filmPreview__info--countries")).flatMap(_ >?> texts("a")).getOrElse(Seq.empty)
    val wantsToSee = (html >?> attr("data-wanna")(".filmPreview__wantToSee")).flatMap(_.toIntOption)

    Movie(
      badge,
      title,
      originalTitle,
      year,
      rate,
      countOfRates,
      genres,
      directors,
      countries,
      wantsToSee
    )
  }
}
