package tables

import model._
import slick.jdbc.PostgresProfile.api._

class BookGenreTable(tag: Tag) extends Table[BookGenre](tag, "book_genre"){
  val genreId = column[Int]("gen_id")
  val bookId = column[Int]("bk_id")

  def * = (genreId, bookId) <> ((BookGenre.apply _).tupled, BookGenre.unapply)

  val bookIdForeignKey = foreignKey(
    "bk_id_fk", bookId, BookTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )
}

object BookGenreTable{
  val query = TableQuery[BookGenreTable]
}

