package scrapper

import models.Movie
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{attr, element, text, texts}

object MovieExtractor {
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
