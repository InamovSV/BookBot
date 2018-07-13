package client

import java.net.URLEncoder

import slick.jdbc.PostgresProfile.api._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import client.BookBotAPIClient.Terms
import client.BookBotAPIClient.Terms._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import repositories._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class BookBotAPIClient(implicit system: ActorSystem,
                       materializer: ActorMaterializer,
                       ec: ExecutionContext)
  extends FailFastCirceSupport {

  private val http = Http()
  private val fieldsConf =
    "items(id,volumeInfo(title,authors,description,categories,averageRating,ratingsCount,language,canonicalVolumeLink)),totalItems"
//&fields=id,volumeInfo(title,authors,description,categories,averageRating,ratingsCount,language,canonicalVolumeLink)
  def getBookById(id: String) = {
    val req = s"https://www.googleapis.com/books/v1/volumes/$id"
    println(req)
    val response = http.singleRequest(
      HttpRequest(
        HttpMethods.GET,
        req
      )
    )
    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, responseEntity, _) =>
        Unmarshal(responseEntity).to[JsonParser.Book]
    }
  }

  def getPreTotalItems(query: String) = {
    val response = http.singleRequest(
      HttpRequest(
        HttpMethods.GET,
        s"https://www.googleapis.com/books/v1/volumes?q=$query&fields=totalItems"
      )
    )
    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, responseEntity, _) =>
        Unmarshal(responseEntity).to[JsonParser.PreGoogleApiResp]
    }
  }

  def getBookByQuery(query: String, maxResults: Int) = {
    val req = s"https://www.googleapis.com/books/v1/volumes?q=$query&maxResults=$maxResults&fields=$fieldsConf"
    println(req)

    val response = http.singleRequest(
      HttpRequest(
        HttpMethods.GET,
        req
      )
    )
    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, responseEntity, _) =>
        Unmarshal(responseEntity).to[JsonParser.GoogleBooksApiResp]
    }
  }

  def getBookInTerms(key: String, maxResults: Int, term: Terms) = {
    val t = term match {
      case InAuthor() => "inauthor"
      case InTitle() => "intitle"
      case Subject() => "subject"
    }
    val req = s"https://www.googleapis.com/books/v1/volumes?q=$t:$key&maxResults=$maxResults&fields=$fieldsConf"
    println(req)
    val response = http.singleRequest(
      HttpRequest(
        HttpMethods.GET,
        req
      )
    )

    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, responseEntity, _) =>
        Unmarshal(responseEntity).to[JsonParser.GoogleBooksApiResp]
    }
  }


  def getAllUserBooks(booksRepository: BookRep, userLogin: Long) =
    booksRepository.findSimpleUserBooksByLogin(userLogin)


  def makeQuery(msg: String): Option[String] = {
    val listQ = msg.split(", ").toList
    println(listQ)
    listQ match {
      case x::Nil => Some(URLEncoder.encode(x, "UTF-8"))
      case list if list.length <= 4 =>
        var queries:List[String] = List()

        if(list.head != "-") queries = URLEncoder.encode(list.head, "UTF-8") :: queries
        if(list(1) != "-") queries = queries ::: List("inauthor:" + URLEncoder.encode(list(1), "UTF-8"))
        if(list.isDefinedAt(2) && list(2) != "-") queries = queries ::: List("subject:" + URLEncoder.encode(list(2), "UTF-8"))
        val q3 = if(list.isDefinedAt(3) && list(3) != "-" && list(3).length == 2) "&langRestrict=" + list(3) else ""

        println(q3)
        Some(s"${queries.mkString("+")}$q3")
      case _ => None
    }
  }

}

object BookBotAPIClient {

  sealed trait Terms

  object Terms {

    case class InAuthor() extends Terms

    case class InTitle() extends Terms

    case class Subject() extends Terms

  }

}
