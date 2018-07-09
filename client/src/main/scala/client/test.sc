import io.circe.generic.JsonCodec
import io.circe.parser._
import client.JsonParser

@JsonCodec
case class MyJsonClass(name: String, list: Option[List[Book]])
@JsonCodec
case class Book(title: String, page: Int)

val json = scala.io.Source
  .fromURL("https://www.googleapis.com/books/v1/volumes?q=1000%20and%201%20night&fields=items(volumeInfo(title,authors,description,categories,averageRating,ratingsCount,language,canonicalVolumeLink)),totalItems").mkString
val inst = decode[JsonParser.GoogleBooksApiResp](json)
inst match {
  case Right(v) => println(v.items.getOrElse(None))
  case Left(e) => println(e.getMessage)
}

val str = "Привет 1 2 3"
