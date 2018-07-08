package repositories

import model.User
import tables.UserTable
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext

class UserRep(val db: Database)(implicit ec: ExecutionContext)
extends BaseRepository[User](UserTable.query){
}
