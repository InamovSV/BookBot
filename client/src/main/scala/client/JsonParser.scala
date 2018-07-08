package client

import io.circe.generic.JsonCodec

object JsonParser {

  @JsonCodec
  case class GoogleBooksApiResp(totalItems: Int, items: List[Book])
//https://www.googleapis.com/books/v1/volumes?q=Harry%20Potter&fields=items(volumeInfo(title,authors,description,categories,averageRating,ratingsCount,language,canonicalVolumeLink)),totalItems
  @JsonCodec
  case class VolumeInfo(title: String,
                  authors: List[String],
                  description: Option[String],
                  categories: List[String],
                  averageRating: Option[Double],
                  ratingsCount: Option[Int],
                  language: String,
                  canonicalVolumeLink: String)

  @JsonCodec
  case class Book(volumeInfo: VolumeInfo)
}
