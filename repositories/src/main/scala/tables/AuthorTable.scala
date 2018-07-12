package tables

import model.Author
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class AuthorTable(tag: Tag) extends Table[Author](tag, "author")
  with BaseTable[Author] {
  val id = column[Long]("auth_id", O.PrimaryKey, O.AutoInc)
  val name = column[String]("auth_name", O.Unique)

  def * = (id, name) <> ((Author.apply _).tupled, Author.unapply)
}

object AuthorTable {
  val query = TableQuery[AuthorTable]
}
