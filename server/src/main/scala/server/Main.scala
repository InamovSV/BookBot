package server

import com.typesafe.config._
import slick.jdbc.PostgresProfile.api._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import repositories._
import repositories.BookRep

object Main {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val db = Database.forConfig("postgresql", config)
    val bookRepository = new BookRep(db)

    val bookApi = new BookApi(bookRepository)

    implicit val system = ActorSystem("book-system")
    implicit val materializer = ActorMaterializer()

    val http = Http()
    val bindingFuture = http.bindAndHandle(bookApi.routes, "0.0.0.0", 8080)
    scala.io.StdIn.readLine("Server is up")
    val stop = for {
      binding <- bindingFuture
      _ <- binding.unbind()
      _ <- system.terminate()
    } yield ()
    Await.result(stop, 1.second)
    db.close()
  }
}
