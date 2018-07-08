package tables

import model._
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class UserTable(tag: Tag) extends Table[User](tag, "user")
  with BaseTable[User] {
  val id = column[Int]("us_id", O.PrimaryKey)
  val name = column[String]("us_name")

  def * = (id, name) <> ((User.apply _).tupled, User.unapply)
}

object UserTable {
  val query = TableQuery[UserTable]
}
