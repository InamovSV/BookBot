import java.time.LocalDateTime

import akka.actor.{Actor, ActorSystem, Props}
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
import repositories._
import slick.jdbc.PostgresProfile.api._
import tables._

import scala.concurrent.duration._
import scala.util.Random

class Bot extends TelegramBot
  with Polling
  with Commands
  with Callbacks {
  def token = "604121505:AAFfz1XrwMUOdY49Ha7DALz7e8-YTVsfsm0"

  private val config = ConfigFactory.load()
  private val db = Database.forConfig("postgresql", config)

  private val bookRep = new BookRep(db)
  private val userRep = new UserRep(db)
  private val genreRep = new GenreRep(db)
  private val authorRep = new AuthorRep(db)

  val rand = new Random()
  //  var currentBook:JsonParser.GoogleBooksApiResp
  //  val bookClient = new BookBotAPIClient(bookRep)

  onCommand("/hello") { implicit massege =>
    reply("Hi!")
  }
  //////////////////
  val TAG_addBook = "addBook_TAG1"

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
  def bookMarkup(book: JsonParser.Book) = {
    Option(InlineKeyboardMarkup.singleColumn(
      Seq(
        InlineKeyboardButton.callbackData("Add book", tag(book.id)
          //          tag(book.items match {
          //          case Some(list) if list.nonEmpty => list.head.selfLink
          //          case _ => "Empty"
          //        })
        )
      )))
  }

  def tag = prefixTag(TAG_addBook) _

  onCommand("/counter") { implicit msg =>
    reply("Press to increment!", replyMarkup = markupCounter(0))
  }

  onCallbackWithTag(TAG_addBook) { implicit cbq =>
    // Notification only shown to the user who pressed the button.
    ackCallback(Option("The book was added to the read"))
    // Or just ackCallback()
    implicit val system = ActorSystem("books-client")
    implicit val materializer = ActorMaterializer()
    val client = new BookBotAPIClient()

    for {
      data <- cbq.data
      msg <- cbq.message
    } /* do */ {
      client.getBookById(data).foreach{ book =>
        println(book)
        val mBook = new model.Book(0,
          book.volumeInfo.title,
          book.volumeInfo.language,
          book.volumeInfo.description,
          book.volumeInfo.canonicalVolumeLink,
          book.volumeInfo.averageRating,
          book.volumeInfo.ratingsCount)


          bookRep.insertWithId(mBook).foreach{b =>
            db.run(BookUserTable.query += model.BookUser(msg.chat.id, b.id))

            book.volumeInfo.authors.getOrElse(List("Unknown author")).foreach{a=>
              authorRep.insertWithId(model.Author(0, a)).foreach{ar=>
                db.run(BookAuthoringTable.query += model.BookAuthoring(ar.id, b.id))
              }
            }
            book.volumeInfo.categories.getOrElse(List("Unknown genre")).foreach{g=>
              genreRep.insertWithId(model.Genre(0,g)).foreach{gr=>
                db.run(BookGenreTable.query += model.BookGenre(gr.id, b.id))
              }
            }
          }
      }
      println("data: " + data)
      println("msg: " + msg)
    }
  }
  //  for {
  //          data <- cbq.data
  //          msg <- cbq.message
  //        } /* do */ {
  //          println("inlineButton was pressed")
  //          EditMessageReplyMarkup(
  //      Option(ChatId(msg.source)), // msg.chat.id
  //      Option(msg.messageId),
  //      replyMarkup = markupCounter(n + 1))
  //        }
  //
  //////////////////

  onCommand('start) { implicit message =>
    reply(s"Привет, юный ${message.chat.firstName.getOrElse("user")}, мимо проходил или по делу?")
    val chat = message.chat
    userRep.insert(model.User(chat.id, chat.username.getOrElse("User")))
  }

  onCommand('getbook) { implicit message =>
    implicit val system = ActorSystem("books-client")
    implicit val materializer = ActorMaterializer()
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
                book.items match {
                  case Some(books) if books.nonEmpty => reply(book.items.getOrElse(List("Books not found")).mkString("\n"),
                    replyMarkup = bookMarkup(books.head))
                  case _ => reply("Book not found")
                }

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
  //     val client = new BookBotAPIClient(bookRep)
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
