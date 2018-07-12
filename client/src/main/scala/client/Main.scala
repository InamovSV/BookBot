package client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import slick.jdbc.PostgresProfile.api._
import com.typesafe.config._
import repositories.BookRep

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await

object Main extends App {
  implicit val system = ActorSystem("book-client")
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val db = Database.forConfig("postgresql", config)
  val bookRep = new BookRep(db)
//  bookRep.dropSchema()
  bookRep.createSchema()
//  val bookClient = new BookBotAPIClient()
//println(BookBotAPIClient.Terms.InTitle())
//  val res = Await.result(bookClient.getBookByKeyWords("fsdfsdfs", 5), 5.seconds)
//  println(res.items.mkString("\n"))

//    val res = scala.io.Source.fromURL("https://www.googleapis.com/books/v1/volumes?q=Harry+Potter&fields=items(volumeInfo(title,authors,description,categories,averageRating,ratingsCount,language,canonicalVolumeLink)),totalItems")
//    println(res.mkString)
}
