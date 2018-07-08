package tables

import model._
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class GenreTable(tag: Tag) extends Table[Genre](tag, "genre")
  with BaseTable[Genre] {
  val id = column[Int]("gen_id", O.PrimaryKey)
  val title = column[String]("gen_title")

  def * = (id, title) <> ((Genre.apply _).tupled, Genre.unapply)
}

object GenreTable {
  val query = TableQuery[GenreTable]
}
