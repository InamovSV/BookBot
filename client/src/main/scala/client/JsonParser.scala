package client

import io.circe.generic.JsonCodec

object JsonParser {

  @JsonCodec
  case class GoogleBooksApiResp(totalItems: Int, items: Option[List[Book]])

  @JsonCodec
  case class PreGoogleApiResp(totalItems: Int)

  //https://www.googleapis.com/books/v1/volumes?q=Harry%20Potter&fields=items(volumeInfo(title,authors,description,categories,averageRating,ratingsCount,language,canonicalVolumeLink)),totalItems
  @JsonCodec
  case class VolumeInfo(title: String,
                        authors: Option[List[String]],
                        description: Option[String],
                        categories: Option[List[String]],
                        averageRating: Option[Double],
                        ratingsCount: Option[Int],
                        language: String,
                        canonicalVolumeLink: String)

  @JsonCodec
  case class Book(id: String, volumeInfo: VolumeInfo) {
    override def toString: String = {
      val genres = volumeInfo.categories match {
        case Some(list) => "\nCategories: " + list.mkString(", ")
        case None => ""
      }
      val stars = volumeInfo.averageRating match {
        case Some(v) => "\nStars: " + v
        case None => ""
      }
      val ratingsCount = volumeInfo.ratingsCount match {
        case Some(v) => "\nPlace in rating: " + v
        case None => ""
      }
      val description = volumeInfo.description match {
        case Some(v) => "\n" + v.take(3000)
        case None => ""
      }
      val authors = "\n" + s"""by ${volumeInfo.authors.getOrElse(List("unknown authors")).mkString(", ")}"""

      s""""${volumeInfo.title}"""" +
        authors +
        genres +
        description +
        stars +
        ratingsCount +
        s"\nlanguage: ${volumeInfo.language}" +
        s"\n${volumeInfo.canonicalVolumeLink}"
    }

  }

}
