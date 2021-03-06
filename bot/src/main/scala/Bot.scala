import java.net.URLEncoder
import java.time.LocalDateTime

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import client.BookBotAPIClient
import com.typesafe.config.ConfigFactory
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Extractors, Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.{EditMessageReplyMarkup, ParseMode, SendMessage}
import info.mukel.telegrambot4s.models._
import repositories._
import slick.jdbc.PostgresProfile.api._
import tables._

import scala.concurrent.duration._
import scala.io.{Codec, Source}
import scala.util.{Random, Try}

class Bot(botToken: String) extends TelegramBot
  with Polling
  with Commands
  with Callbacks {

  def token = botToken

  private val config = ConfigFactory.load()
  private val db = Database.forConfig("postgresql", config)

  private val bookRep = new BookRep(db)
  private val userRep = new UserRep(db)
  private val genreRep = new GenreRep(db)
  private val authorRep = new AuthorRep(db)

  val rand = new Random()

  onCommand("/hello") { implicit massege =>
    reply("Hi!")
  }

  onCommand('help) { implicit message =>
    reply(
      """This is your personal librarian!
        |
        |Remember a few rules:
        |   *no noise in the library
        |   *learning is light, but for light it is necessary to pay
        |   *you need to read only 10 books,
        |   but to find them, you need to search through thousand
        |
        |And some syntax rules:
        |command /getbook takes parameters like [title, author, categories, language(2 character)]
        |*without square brackets*
        |you can overlook parameters from the end or, if necessary, omit them in the middle with the symbol "-"
        |like [title, -, categories, -] or [title, author]
        |Spaces are allowed in the parameters, but they must be separated by commas
        |commands /recommendbyauthor and /recommendbygenre have the same rules, but they have only two parameters:
        |[author/genre, language]
        |
        |If you want to add a book to your shelf, click "Add book" button under message with book
        |
        |Recommendations are formed in accordance with your books on the shelf
        |
        |If you want to thank Creator, write to the mail sergej.inamov@gmail.com
        |
        |Good luck and good read!
      """.stripMargin)
  }
  //////////////////
  val TAG_addBook = "addBook_TAG"
  val TAG_next = "next_TAG"

  var currentQuery:String = ""
  var n: Int = 0
  def bookMarkup(bookId: String) = {
        Option(InlineKeyboardMarkup.singleColumn(
          Seq(
            InlineKeyboardButton.callbackData("Add book", prefixTag(TAG_addBook)(bookId)),
            InlineKeyboardButton.callbackData(s"Next - $n", prefixTag(TAG_next)(bookId))
          )
        ))
      }

    onCallbackWithTag(TAG_next) { implicit cbq =>

      implicit val system = ActorSystem("books-client")
      implicit val materializer = ActorMaterializer()
      val client = new BookBotAPIClient()
      n += 1
      for {
        data <- cbq.data
        msg <- cbq.message
      } /* do */ {
        currentQuery = currentQuery.replaceAll("startIndex=\\d+","startIndex=" + n)
        client
          .getBookByQuery(currentQuery, 1)
          .flatMap(preBook => preBook.items match {
            case Some(items) if items.nonEmpty => client.getBookByLinkId(items.head.id)
              .flatMap{book =>
                val req = request(
                  SendMessage(ChatId(msg.source),
                    rightFormatHTML(book.toString),
                    Option(ParseMode.HTML),
                    replyMarkup = bookMarkup(book.id))
                )
                req.recover{
                  case _ => request(SendMessage(ChatId(msg.source), book.toString, replyMarkup = bookMarkup(book.id)))
                }
                req
          }
            case _ => request(SendMessage(ChatId(msg.source), "Book not found"))
          })
      }
    }

    onCallbackWithTag(TAG_addBook) { implicit cbq =>

      ackCallback(Option("The book was added to the read"))

      implicit val system = ActorSystem("books-client")
      implicit val materializer = ActorMaterializer()
      val client = new BookBotAPIClient()

      for {
        data <- cbq.data
        msg <- cbq.message
      } /* do */ {
        client.getBookByLinkId(data)
              .foreach { book =>
                println(book)
                val mBook = model.Book(0,
                  book.volumeInfo.title,
                  book.volumeInfo.language,
                  book.volumeInfo.description,
                  book.volumeInfo.canonicalVolumeLink,
                  book.volumeInfo.averageRating,
                  book.volumeInfo.ratingsCount)


                bookRep.insertWithId(mBook).foreach { b =>
                  db.run(BookUserTable.query += model.BookUser(msg.chat.id, b.id))
                  book.volumeInfo.authors.getOrElse(List("Unknown author")).foreach { a =>
                    authorRep.insertWithId(model.Author(0, a)).foreach { ar =>
                      db.run(BookAuthoringTable.query += model.BookAuthoring(ar.id, b.id))
                    }
                  }
                  book.volumeInfo.categories.getOrElse(List("Unknown genre")).flatMap(_.split(" / ")).foreach { g =>
                    genreRep.insertWithId(model.Genre(0, g)).foreach { gr =>
                      db.run(BookGenreTable.query += model.BookGenre(gr.id, b.id))
                    }
                  }
                }
              }

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
    reply(s"Hello, young ${message.chat.firstName.getOrElse("user")}! Are you passing by or on business?")
    val chat = message.chat
    userRep.insert(model.User(chat.id, chat.username.getOrElse("User")))
  }

  onCommand('getbook) { implicit message =>
    implicit val system = ActorSystem("books-client")
    implicit val materializer = ActorMaterializer()
    val client = new BookBotAPIClient()
    n = 0
    withArgs {
      case keys if keys.nonEmpty =>
        client.makeQuery(keys.mkString(" ")) match {
          case Some(q) =>
            currentQuery = q + "&startIndex=0"
            client
              .getBookByQuery(q, 1)
              .flatMap(preBook => preBook.items match {
                case Some(items) if items.nonEmpty =>
                  client.getBookByLinkId(items.head.id)
                  .flatMap{book =>
                    val req = reply(rightFormatHTML(book.toString),
                    Option(ParseMode.HTML),
                    replyMarkup = bookMarkup(book.id))
                  req.recover{
                    case _ => reply(book.toString, replyMarkup = bookMarkup(book.id))
                  }
                  req
              }
                case _ => reply("Book not found")
              })
          case None => reply("Invalid command")
        }

      case _ => reply("Invalid command")
    }
  }

  onCommand('mybooks) { implicit message =>
    implicit val system = ActorSystem("books-client")
    implicit val materializer = ActorMaterializer()
    val client = new BookBotAPIClient()

    client.getAllUserBooks(bookRep, message.chat.id).flatMap { books =>
      if (books.nonEmpty) {
        val resp = books.map(x => s"${"\"" + x._1 + "\""}\n\tby ${x._2}\n${x._3}").mkString("\n\n")
        println(resp)
        reply(resp, disableWebPagePreview = Some(true))
      }
      else
        reply("You haven't any books")
    }
  }

  onCommand('reccomendbook) { implicit message =>
    implicit val system = ActorSystem("books-client")
    implicit val materializer = ActorMaterializer()

    val client = new BookBotAPIClient()

    genreRep.getAllUserGenre(message.chat.id).foreach { genres =>
      authorRep.getAllUserAuthor(message.chat.id).foreach { authors =>
        val prefList = List(genres, authors).filter(_.nonEmpty) match {
          case preferences if preferences.nonEmpty => rand.nextInt(preferences.length) match {
            case 0 =>
              val q = s"subject:${URLEncoder.encode(genres(rand.nextInt(genres.length)), "UTF-8")}"
              client
                .getPreTotalItems(q)
                .flatMap(resp =>
                  client
                    .getBookByQuery(q + s"&startIndex=${rand.nextInt(resp.totalItems * 2 / 3)}", 1)
                    .flatMap(preBook => preBook.items match {
                      case Some(items) if items.nonEmpty => client.getBookByLinkId(items.head.id)
                        .flatMap(book => reply(rightFormatHTML(book.toString),
                          Option(ParseMode.HTML), replyMarkup = bookMarkup(book.id)))
                      case _ => reply("Book not found")
                    })
                )
            case 1 =>
              val q = s"inauthor:${URLEncoder.encode(authors(rand.nextInt(authors.length)), "UTF-8")}"
              client
                .getPreTotalItems(q)
                .flatMap(resp =>
                  client
                    .getBookByQuery(q + s"&startIndex=${rand.nextInt(resp.totalItems * 2 / 3)}", 1)
                    .flatMap(preBook => preBook.items match {
                      case Some(items) if items.nonEmpty => client.getBookByLinkId(items.head.id)
                        .flatMap(book => reply(rightFormatHTML(book.toString),
                          Option(ParseMode.HTML), replyMarkup = bookMarkup(book.id)))
                      case _ => reply("Book not found")
                    })
                )
          }
          case _ => reply("You haven't any books in shelf to get recommendation")
        }
      }
    }
  }

  onCommand('recommendbyauthor) { implicit message =>
    implicit val system = ActorSystem("books-client")
    implicit val materializer = ActorMaterializer()

    val client = new BookBotAPIClient()
    withArgs {
      case keys if keys.nonEmpty =>
        val msgQuery = keys.mkString(" ").split(", ")
        client.makeQuery(List("-", msgQuery.head, "-", msgQuery.tail.headOption.getOrElse("-")).mkString(", ")) match {
          case Some(q) => client
            .getPreTotalItems(q)
            .flatMap(resp =>
              client
                .getBookByQuery(q + s"&startIndex=${rand.nextInt(resp.totalItems * 2 / 3)}", 1)
                .flatMap(preBook => preBook.items match {
                  case Some(items) if items.nonEmpty => client.getBookByLinkId(items.head.id)
                    .flatMap(book => reply(rightFormatHTML(book.toString),
                      Option(ParseMode.HTML), replyMarkup = bookMarkup(book.id)))
                  case _ => reply("Book not found")
                })
            )
          case None => reply("Invalid command")
        }
      case _ => reply("Invalid command")
    }
  }

  onCommand('recommendbygenre) { implicit message =>
    implicit val system = ActorSystem("books-client")
    implicit val materializer = ActorMaterializer()
    val client = new BookBotAPIClient()

    withArgs {
      case keys if keys.nonEmpty =>
        val msgQuery = keys.mkString(" ").split(", ")
        client.makeQuery(List("-", "-", msgQuery.head, msgQuery.tail.headOption.getOrElse("-")).mkString(", ")) match {
          case Some(q) => client
            .getPreTotalItems(q)
            .flatMap(resp =>
              client
                .getBookByQuery(q + s"&startIndex=${rand.nextInt(resp.totalItems * 2 / 3)}", 1)
                .flatMap(preBook => preBook.items match {
                  case Some(items) if items.nonEmpty => client.getBookByLinkId(items.head.id)
                    .flatMap(book => reply(rightFormatHTML(book.toString),
                      Option(ParseMode.HTML), replyMarkup = bookMarkup(book.id)))
                  case _ => reply("Book not found")
                })
            )
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

  //To Do make more right regex
  def rightFormatHTML(str: String): String = str
    .replaceAll("<br>|<p>|<\\/p>", "\n")
//    .replaceAll("<p>|<\\/p>", "\n")

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }

  run()
}
