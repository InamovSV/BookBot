import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import client.BookBotAPIClient
import com.typesafe.config.ConfigFactory
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.SendMessage
import info.mukel.telegrambot4s.models.Message
import repositories.BookRep
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration._

class Bot extends TelegramBot with Polling with Commands {
  def token = "604121505:AAFfz1XrwMUOdY49Ha7DALz7e8-YTVsfsm0"

  private val config = ConfigFactory.load()
  private val db = Database.forConfig("postgresql", config)
  private val rep = new BookRep(db)
  //  val bookClient = new BookBotAPIClient(bookRep)

  onCommand("/hello") { implicit massege =>
    reply("Hello, my young reader")
  }

  onCommand("/getBook") { implicit message =>
    implicit val system = ActorSystem("news-client")
    implicit val materializer = ActorMaterializer()

    val client = new BookBotAPIClient(rep)
    withArgs {
      case keys if keys.nonEmpty =>
        println(keys)
        client
          .getBookByKeyWords(keys.mkString("%20"), 1)
          .flatMap(book => {
            println(book)
            reply(book.items.getOrElse(List()).mkString("\n"))
          })
      case _ => reply("Not found")
    }
  }

//   override def receiveMessage(msg: Message): Unit = {
//     implicit val system = ActorSystem("news-client")
//     implicit val materializer = ActorMaterializer()
//
//     val client = new BookBotAPIClient(rep)
//     for (text <- msg.text)
//       request(SendMessage(msg.source, text))
//   }
  system.scheduler.schedule(10 seconds, 5 seconds){
    request(SendMessage("398766533", LocalDateTime.now.toString))
  }

  run()
}
