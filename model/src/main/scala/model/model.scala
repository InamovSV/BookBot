package model

import io.circe.generic.JsonCodec

trait HasId {
  val id: Long
}

@JsonCodec
case class Author(id: Long, name: String) extends HasId

@JsonCodec
case class BookAuthoring(authId: Long, bookId: Long)

@JsonCodec
case class Book(id: Long,
                title: String,
                language: String,
                description: Option[String],
                ref: String,
                stars: Option[Double],
                numOfRating: Option[Int]) extends HasId

object Book{
  sealed trait Filter
  object Filter{
    case class Title(like: String) extends Filter
    case class Genre(like: String) extends Filter
    case class Author(like: String) extends Filter
    case class Rating(value: Double) extends Filter
    case class Country(like: String) extends Filter
  }
}

@JsonCodec
case class Genre(id: Long, title: String) extends HasId

@JsonCodec
case class BookGenre(genreId: Long, bookId: Long)

@JsonCodec
case class User(id: Long, login: String) extends HasId

@JsonCodec
case class BookUser(userId: Long, bookId: Long)