package tables

import model._
import slick.jdbc.PostgresProfile.api._

class BookAuthoringTable(tag: Tag) extends Table[model.BookAuthoring](tag, "book_authoring"){
  val authId = column[Int]("auth_id")
  val bookId = column[Int]("bk_id")

  def * = (authId, bookId) <> ((BookAuthoring.apply _).tupled, BookAuthoring.unapply)

  val authIdForeignKey = foreignKey(
    "auth_id_fk", authId, AuthorTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )

  val bookIdForeignKey = foreignKey(
    "bk_id_fk", bookId, BookTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )
}

object BookAuthoringTable{
  val query = TableQuery[BookAuthoringTable]
}