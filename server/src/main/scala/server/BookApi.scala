package server

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.{ExecutionContext, Future}
import akka.stream.scaladsl._
import akka.http.scaladsl.model.ws._
import io.circe.parser._
import repositories.BookRep

class BookApi(booksRepository: BookRep)(implicit ex: ExecutionContext)
  extends Directives
    with FailFastCirceSupport
    with StrictLogging {
  def index: Route =
    pathSingleSlash {
      get {
        complete("Hello")
      }
    } ~ path("index") {
      get {
        val html =
          """
          <html>
          <h1>HELLO, AKKA</h1>
          </html>
        """
        val response = HttpResponse(
          entity = HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            html
          )
        )
        complete(response)
      }
    }

  def findAllBooks: Route =
    path("movies" / "all") {
      val futureBooks = booksRepository.findAll()
      onSuccess(futureBooks) { books =>
        complete(books)
      }
    }

  def routes: Route = index ~ findAllBooks
}
