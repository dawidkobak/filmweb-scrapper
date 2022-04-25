package elasticserach.sender

import model.Movie
import org.json4s.native.Serialization.write
import org.json4s.{DefaultFormats, Formats}

object ElasticSearchSender {

  val indexName = "filmweb_v1"
  val elasticSearchHost = "http://localhost:9200"

  implicit val formats: Formats = DefaultFormats

  def send(movie: Movie): Unit = {
    val hash = movie.hashCode()
    val movieJson = write(movie)
    requests.post(elasticSearchHost + "/" + indexName + "/_doc/" + hash, headers = Map("Content-Type" -> "application/json") , data = movieJson)
  }
}
