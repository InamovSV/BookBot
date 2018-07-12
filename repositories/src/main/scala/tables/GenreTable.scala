package tables

import model._
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class GenreTable(tag: Tag) extends Table[Genre](tag, "genre")
  with BaseTable[Genre] {
  val id = column[Long]("gen_id", O.PrimaryKey, O.AutoInc)
  val title = column[String]("gen_title", O.Unique)

  def * = (id, title) <> ((Genre.apply _).tupled, Genre.unapply)
}

object GenreTable {
  val query = TableQuery[GenreTable]
}
