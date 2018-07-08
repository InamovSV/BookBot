package tables

import model._
import slick.jdbc.PostgresProfile.api._

class BookUserTable(tag: Tag) extends Table[BookUser](tag, "book_user"){
  val bookId = column[Int]("bk_id")
  val userId = column[Int]("us_id")

  def * = (userId, bookId) <> ((BookUser.apply _).tupled, BookUser.unapply)

  val bookIdForeignKey = foreignKey(
    "bk_id_fk", bookId, BookTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )

  val userIdForeignKey = foreignKey(
    "us_id_fk", userId, UserTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )
}

object BookUserTable{
  val query = TableQuery[BookUserTable]
}
