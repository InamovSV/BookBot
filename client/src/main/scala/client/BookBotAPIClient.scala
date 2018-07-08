package client

import slick.jdbc.PostgresProfile.api._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import repositories._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Success, Failure}

class BookBotAPIClient(val booksRepository: BookRep)
                       (implicit system: ActorSystem,
                       materializer: ActorMaterializer,
                       ec: ExecutionContext)
  extends FailFastCirceSupport {

  private val http = Http()

  def getBookByKeyWords = {
    val response = http.singleRequest(
      HttpRequest(
        HttpMethods.GET,
        "https://www.googleapis.com/books/v1/volumes?q=Harry%20Potter&fields=items(volumeInfo(title,authors,description,categories,averageRating,ratingsCount,language,canonicalVolumeLink)),totalItems"
      )
    )
    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, responseEntity, _) =>
        Unmarshal(responseEntity).to[JsonParser.GoogleBooksApiResp]
    }
  }


  def getAllUserBooks(userLogin: String) =
    booksRepository.findUserBookByLogin(userLogin)
  
}
