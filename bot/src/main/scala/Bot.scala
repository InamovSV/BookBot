import java.time.LocalDateTime

import akka.actor.{ActorSystem, Actor, Props}
import akka.stream.ActorMaterializer
import client.BookBotAPIClient
import client.JsonParser
import client.BookBotAPIClient.Terms
import client.JsonParser.{Book, VolumeInfo}
import com.typesafe.config.ConfigFactory
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Extractors, Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.{EditMessageReplyMarkup, ParseMode, SendMessage}
import info.mukel.telegrambot4s.models._
import repositories.BookRep
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration._
import scala.util.Random

class Bot extends TelegramBot
  with Polling
  with Commands
  with Callbacks {
  def token = "604121505:AAFfz1XrwMUOdY49Ha7DALz7e8-YTVsfsm0"

  private val config = ConfigFactory.load()
  private val db = Database.forConfig("postgresql", config)
  private val rep = new BookRep(db)

  val rand = new Random()
  //  var currentBook:JsonParser.GoogleBooksApiResp
  //  val bookClient = new BookBotAPIClient(bookRep)

  onCommand("/hello") { implicit massege =>
    reply("Hi!")
  }
  //////////////////
  val TAG1 = "addBook_TAG1"

  var requestCount = 0

  def markupCounter(n: Int) = {
    requestCount += 1
    Option(InlineKeyboardMarkup.singleColumn(
      Seq(
        InlineKeyboardButton.callbackData(
          s"Press me 1!!!\n$n - $requestCount", tag(n.toString)),
        InlineKeyboardButton.callbackData(s"Press me 2!!!\n$n - $requestCount", tag(n.toString))
      ))
    )
  }
//ToDo передавать в колбэк ориджин линк к апи и вернуть по ней книгу
  def addBookInBD(book: JsonParser.GoogleBooksApiResp) = {
    Option(InlineKeyboardMarkup.singleColumn(
      Seq(
        InlineKeyboardButton.callbackData("Next book", tag(book.items.get.head.)),
        InlineKeyboardButton.callbackData("Add book", tag("0"))
      )))
  }

  def tag = prefixTag(TAG1) _

  onCommand("/counter") { implicit msg =>
    reply("Press to increment!", replyMarkup = markupCounter(0))
  }

  onCallbackWithTag(TAG1) { implicit cbq =>
    // Notification only shown to the user who pressed the button.
    ackCallback(Option(cbq.from.firstName + " pressed the button!"))
    // Or just ackCallback()

    for {
      data <- cbq.data
      Extractors.Int(n) = data
      msg <- cbq.message
    } /* do */ {
      println("inlineButton was pressed")
      request(
        EditMessageReplyMarkup(
          Option(ChatId(msg.source)), // msg.chat.id
          Option(msg.messageId),
          replyMarkup = markupCounter(n + 1)))
    }
  }

  //////////////////
  onCommand('getbook) { implicit message =>
    implicit val system = ActorSystem("books-client")
    implicit val materializer = ActorMaterializer()
    //    InlineKeyboardButton()
    val client = new BookBotAPIClient()
    withArgs {
      case keys if keys.nonEmpty =>
        println(keys)
        client.makeQuery(keys.mkString("%20")) match {
          case Some(q) =>
            println(q)
            client
              .getBookByKeyWords(q, 1)
              .flatMap(book => {
                println(book)
                //                currentBook = book
                reply(book.items.getOrElse(List("Books not found")).mkString("\n"))
              })
          case None => reply("Invalid command")
        }

      case _ => reply("Invalid command")
    }
  }

  onCommand('recommendbyauthor) { implicit message =>
    implicit val system = ActorSystem("books-client")
    implicit val materializer = ActorMaterializer()

    val client = new BookBotAPIClient()
    withArgs {
      case keys if keys.nonEmpty =>
        val msgQuery = keys.mkString("%20").split(",%20")
        client.makeQuery(List("-", msgQuery.head, "-", msgQuery.tail.headOption.getOrElse("-")).mkString(",%20")) match {
          case Some(q) => client
            .getBookByKeyWords(q + s"&startIndex=${rand.nextInt(10)}", 1)
            .flatMap(book => {
              println(book)
              reply(book.items.getOrElse(List()).mkString("\n"))
            })
          case None => reply("Invalid command")
        }
      case _ => reply("Invalid command")
    }
  }

  onCommand('recommendbygenre) { implicit message =>
    implicit val system = ActorSystem("books-client")
    implicit val materializer = ActorMaterializer()
    //ToDo рандом должен быть в рамках возвращённых книг
    val client = new BookBotAPIClient()
    withArgs {
      case keys if keys.nonEmpty =>
        val msgQuery = keys.mkString("%20").split(",%20")
        client.makeQuery(List("-", "-", msgQuery.head, msgQuery.tail.headOption.getOrElse("-")).mkString(",%20")) match {
          case Some(q) => client
            .getBookByKeyWords(q + s"&startIndex=${rand.nextInt(50)}", 1)
            .flatMap(book => {
              println(book)
              reply(book.items.getOrElse(List()).mkString("\n"))
            })
          case None => reply("Invalid command")
        }

      case _ => reply("Invalid command")
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

  //    system.scheduler.schedule(5 seconds, 5 seconds){
  //      request(SendMessage("398766533", "Привет!"))
  //    }
  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }

  run()
}
