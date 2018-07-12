package tables

import model.Book
import repositories._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class BookTable(tag: Tag) extends Table[Book](tag, "book") with BaseTable[Book]{
  val id = column[Long]("bk_id", O.PrimaryKey, O.AutoInc)
  val title = column[String]("bk_title")
  val lang = column[String]("bk_lang", O.SqlType("varchar(2)"))
  val description = column[Option[String]]("bk_description")
  val ref = column[String]("bk_ref")
  val stars = column[Option[Double]]("bk_stars")
  val numOfRating = column[Option[Int]]("bk_num_o_rating")

  def * =
    (id, title, lang, description, ref, stars, numOfRating) <> ((Book.apply _).tupled, Book.unapply)
}

object BookTable{
  val query = TableQuery[BookTable]
}

//class BookRepository(val db: Database)(implicit ec: ExecutionContext) extends BaseRepository[Book](BookTable.query){
//  def createSchema(): Future[Unit] = {
//    db.run((
//      BookTable.query.schema ++
//      AuthorTable.query.schema ++
//      GenreTable.query.schema ++
//      UserTable.query.schema ++
//      BookAuthoringTable.query.schema ++
//      BookGenreTable.query.schema ++
//      BookUserTable.query.schema
//    ).create)
//  }
//
//  def dropSchema(): Future[Unit] = {
//    db.run((
//      BookTable.query.schema ++
//        AuthorTable.query.schema ++
//        GenreTable.query.schema ++
//        UserTable.query.schema ++
//        BookAuthoringTable.query.schema ++
//        BookGenreTable.query.schema ++
//        BookUserTable.query.schema
//      ).drop)
//  }
//
//  def findAll(): Future[Vector[Book]] = db.run(BookTable.query.to[Vector].result)
//}



