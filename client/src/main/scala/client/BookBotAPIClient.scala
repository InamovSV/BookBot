package client

import slick.jdbc.PostgresProfile.api._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import client.BookBotAPIClient.Parameter.MaxResults
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import repositories._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class BookBotAPIClient(val booksRepository: BookRep)
                      (implicit system: ActorSystem,
                       materializer: ActorMaterializer,
                       ec: ExecutionContext)
  extends FailFastCirceSupport {

  private val http = Http()
  private val fieldsConf =
    "items(volumeInfo(title,authors,description,categories,averageRating,ratingsCount,language,canonicalVolumeLink)),totalItems"

  def getBookByKeyWords(key: String, maxResults: Int) = {
    val response = http.singleRequest(
      HttpRequest(
        HttpMethods.GET,
        s"https://www.googleapis.com/books/v1/volumes?q=$key&maxResults=$maxResults&fields=$fieldsConf"
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

object BookBotAPIClient {

  sealed trait Parameter

  object Parameter {

    case class Download() extends Parameter

    case class LangRestrict(lang: String) extends Parameter

    case class MaxResults(num: Int) extends Parameter

  }

  sealed trait Terms

  object Terms {

    case class InAuthor() extends Terms

    case class InTitle() extends Terms

    case class Subject() extends Terms

  }

}
