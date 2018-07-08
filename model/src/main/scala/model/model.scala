package model

import io.circe.generic.JsonCodec

trait HasId {
  val id: Int
}

@JsonCodec
case class Author(id: Int, name: String) extends HasId

@JsonCodec
case class BookAuthoring(authId: Int, bookId: Int)

@JsonCodec
case class Book(id: Int,
                title: String,
                language: String,
                description: String,
                ref: String,
                stars: Double,
                numOfRating: Int) extends HasId

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
case class Genre(id: Int, title: String) extends HasId

@JsonCodec
case class BookGenre(genreId: Int, bookId: Int)

@JsonCodec
case class User(id: Int, login: String) extends HasId

@JsonCodec
case class BookUser(userId: Int, bookId: Int)