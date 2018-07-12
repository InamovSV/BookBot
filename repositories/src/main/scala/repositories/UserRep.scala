package repositories

import model.User
import tables.UserTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class UserRep(val db: Database)(implicit ec: ExecutionContext)
extends BaseRepository[User](UserTable.query){
  private val insertWithIdQuery =
    UserTable.query returning UserTable.query.map(_.id) into ((item, id) => item.copy(id = id))

  def insertWithId(item: User): Future[User] = db.run(insertWithIdQuery += item)
}
