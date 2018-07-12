//package client
//
//import model._
//import repositories._
//import slick.jdbc.PostgresProfile.api._
//import tables._
//
//import scala.concurrent.Await
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.duration._
//
//object SampleData {
//
//  val books = Set(
//    Book(
//      101,
//      "test book 1",
//      "en",
//      Some("Sample description"),
//      "https://books.google.com.ua/books/about/It.html?hl=&id=S85NCwAAQBAJ&redir_esc=y",
//      Some(4.0),
//      Some(237)
//    ), Book(
//      102,
//      "test book 2",
//      "Sample description",
//      "en",
//      "https://books.google.com.ua/books/about/It.html?hl=&id=S85NCwAAQBAJ&redir_esc=y",
//      4.1,
//      236
//    )
//  )
//
//  val autors = Set(
//    Author(201, "Author 1"),
//    Author(202, "Author 2")
//  )
//
//  val bookAuthorings = Set(
//    BookAuthoring(201, 101),
//    BookAuthoring(202, 102)
//  )
//
//  val genres = Set(
//    Genre(301, "Genre 1"),
//    Genre(302, "Genre 2")
//  )
//
//  val bookGenres = Set(
//    BookGenre(401, 101),
//    BookGenre(402, 102)
//  )
//
//  val users = Set(
//    User(501, "Login 1"),
//    User(502, "Login 2")
//  )
//
//  val bookUsers = Set(
//    BookUser(501, 101),
//    BookUser(502, 102)
//  )
//
//  def main(args: Array[String]): Unit = {
//    val db = Database.forConfig("postgresql")
//    val bookRepository = new BookRep(db)
//
////    Await.result(bookRepository.dropSchema(), 5.seconds)
//    try {
//      Await.result(bookRepository.createSchema(), 5.seconds)
//      println("Created schema")
////      Await.result(
////          for {
////            _ <- db.run { BookTable.query ++= books }
////            _ <- db.run { AuthorTable.query ++= autors }
////            _ <- db.run { GenreTable.query ++= genres }
////            _ <- db.run { UserTable.query ++= users }
////            _ <- db.run { BookAuthoringTable.query ++= bookAuthorings }
////            _ <- db.run { BookGenreTable.query ++= bookGenres }
////            _ <- db.run { BookUserTable.query ++= bookUsers }
////          } yield (),
////          10.seconds
////      )
//      println("Inserted sample data")
//    } catch {
//      case e: Throwable => e.printStackTrace()
//    } finally {
//      db.close()
//    }
//  }
//}
