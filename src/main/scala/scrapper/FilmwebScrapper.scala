package scrapper

import elasticserach.sender.ElasticSearchSender
import model.Movie
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{attr, element, elementList, text}
import scrapper.FilmwebScrapper.browser.DocumentType
import ujson.Value

import java.util.concurrent.Executors
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, ExecutionContext, Future}

object FilmwebScrapper {

  val browser: Browser = JsoupBrowser()
  val filmwebHost = "https://www.filmweb.pl"
  val filmsEndpoint = "/films"
  val serialsEndpoint = "/serials"
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))

  def doScrapping(request: cask.Request) = {
    val phrases: Set[String] = request.text.split("&").collect{
      case s"phrase=$phr" => phr
    }.toSet
    val scrapFutureResult = phrases.map(phrase => phrase -> scrapPhrase(phrase)).toMap
    val scrapResult = scrapFutureResult.map(r => r._1 -> Await.result(r._2, Inf))
    val allMovies = scrapResult.flatMap(s => s._2)
    allMovies.foreach(ElasticSearchSender.send)
    scrapResult.map(r => ujson.Obj("phrase" -> ujson.Str(r._1), "count" -> ujson.Str(r._2.length.toString)))
  }

  def scrapPhrase(phrase: String): Future[Seq[Movie]] = {
    scrapFilms(phrase).zip(scrapSerials(phrase)).map {
      case (films, serials) => films ++ serials
    }
  }

  def scrapFilms(phrase: String): Future[Seq[Movie]] = {
    scrapMovies(filmsEndpoint, phrase)
  }

  def scrapSerials(phrase: String): Future[Seq[Movie]] = {
    scrapMovies(serialsEndpoint, phrase)
  }

  def scrapMovies(endpoint: String, phrase: String): Future[Seq[Movie]] = {
    val firstPage = browser.get(filmwebHost + endpoint + "/search?q=" + phrase)
    val resultsCount = (firstPage >?> element(".resultsHeader")).flatMap(_ >?> text("span").map(_.replace(" ", "").toInt))
    if (resultsCount.isEmpty) {
      Future{ Seq.empty }
    } else {
      val numberOfPages = (resultsCount.get / 10) + 1 // i.e. 4 results means 1 page, not 0, that's why +1
      if (numberOfPages < 2) {
        Future { scrapMoviesFromPage(firstPage) }
      } else {
        val pagination = firstPage >> element(".pagination__list")
        val nextPagesEndpoint = (pagination >> attr("href")("a")).dropRight(1)
        val remainingPages = getRemainingPages(filmwebHost + endpoint + "/search" + nextPagesEndpoint, 2 to numberOfPages)
        val allPages = firstPage +: remainingPages
        val futures = allPages.map(p => Future{ scrapMoviesFromPage(p) })
        Future.sequence(futures).map(_.flatten)
      }
    }
  }

  def getRemainingPages(url: String, pagesRange: Range): Seq[DocumentType] = {
    val futures = pagesRange.map(pageNumber => Future{ JsoupBrowser().get(url + pageNumber).asInstanceOf[DocumentType] })
    Await.result(Future.sequence(futures), Inf)
  }

  def scrapMoviesFromPage(page: DocumentType): Seq[Movie] = {
    val searchResult = page >?> element("#searchResult")
    val pageContainer = searchResult.flatMap(_ >?> element(".page__container"))
    val htmlList = pageContainer.flatMap(_ >?> element(".resultsList"))
    val moviesList = htmlList.flatMap(_ >?> elementList(".hits__item")).getOrElse(List.empty)
    moviesList.map(MovieExtractor.extractMovie)
  }
}
