package client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import slick.jdbc.PostgresProfile.api._
import com.typesafe.config._
import repositories.{AuthorRep, BookRep}
import tables.AuthorTable

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await

object Main extends App {
  implicit val system = ActorSystem("book-client")
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val db = Database.forConfig("postgresql", config)
  val bookRep = new BookRep(db)
  bookRep.createSchema()
//  val authRep = new AuthorRep(db)
//  authRep.insertWithId(model.Author(0,"Джордж Оруэлл")).foreach(x => println(
//    s"""--------------------------------------
//      |${x.id}
//       ${x.name}
//      |--------------------------------------
//    """.stripMargin))

//  println(Await.result(authRep.insertWithId(model.Author(0,"Джордж Оруэлл")), 3 seconds))
//bookRep.insertWithId(model.Book(0,"1984", "en", None, "https://market.android.com/details?id=book-uHOGAwAAQBAJ", None, None))
//  bookRep.getBookByTitle("1984").foreach(println)
//  bookRep.dropSchema().foreach(_ => bookRep.createSchema())
//  bookRep.createSchema()
//  val bookClient = new BookBotAPIClient()
//println(BookBotAPIClient.Terms.InTitle())
//  val res = Await.result(bookClient.getBookByKeyWords("fsdfsdfs", 5), 5.seconds)
//  println(res.items.mkString("\n"))

//    val res = scala.io.Source.fromURL("https://www.googleapis.com/books/v1/volumes?q=Harry+Potter&fields=items(volumeInfo(title,authors,description,categories,averageRating,ratingsCount,language,canonicalVolumeLink)),totalItems")
//    println(res.mkString)
}
