import java.net.{URLDecoder, URLEncoder}

import scala.util.Random
//import akka.NotUsed
//import akka.actor.ActorSystem
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
//import akka.http.scaladsl.unmarshalling.Unmarshal
//import client.{BookBotAPIClient, JsonParser}
//import akka.stream._
//import akka.stream.scaladsl._
//import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
//
//import scala.concurrent.{ExecutionContext, Future}
//
//private val http = Http()
//private val fieldsConf =
//  "items(volumeInfo(title,authors,description,categories,averageRating,ratingsCount,language,canonicalVolumeLink)),totalItems"
//class BookBotAPIClient(implicit system: ActorSystem,
//                       materializer: ActorMaterializer,
//                       ec: ExecutionContext)
//  extends FailFastCirceSupport {
//  def getBookByKeyWords(key: String, maxResults: Int) = {
//    val req = s"https://www.googleapis.com/books/v1/volumes?q=$key&maxResults=$maxResults&fields=$fieldsConf"
//    println(req)
//
//    val response = http.singleRequest(
//      HttpRequest(
//        HttpMethods.GET,
//        req
//      )
//    )
//    response.flatMap {
//      case HttpResponse(StatusCodes.OK, _, responseEntity, _) =>
//        Unmarshal(responseEntity).to[JsonParser.GoogleBooksApiResp]
//    }
//  }
//}
//implicit val system = ActorSystem("books-client")
//implicit val materializer = ActorMaterializer()
//val api = new BookBotAPIClient()
//api.getBookByKeyWords("it&startIndex=1000", 1).flatMap{book1 =>
//  api.getBookByKeyWords(s"it&startIndex=${book1.totalItems}",1).flatMap(book2 =>
//    Future(println(book2.items.getOrElse(List()).mkString("\n")))
//  )
//}

val list: List[Int] = List(1)
list.headOption.flatMap{ x =>
  x match {
    case v => Some(v + 1)
    case _ => None
  }
}

