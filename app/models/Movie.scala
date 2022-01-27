package models

case class Movie(
                badge: Option[String],
                title: Option[String],
                originalTitle: Option[String],
                year: Option[Int],
                rate: Option[Double],
                countOfRates: Option[Int],
                genres: Iterable[String],
                directors: Iterable[String],
                countries: Iterable[String],
                wantsToSee: Option[Int]
                )
