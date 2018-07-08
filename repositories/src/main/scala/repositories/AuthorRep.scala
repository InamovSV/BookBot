package repositories

import model.Author
import tables.AuthorTable
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext

class AuthorRep(val db: Database)(implicit ec: ExecutionContext)
  extends BaseRepository[Author](AuthorTable.query) {
}
