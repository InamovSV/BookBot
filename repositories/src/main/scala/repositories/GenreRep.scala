package repositories

import model.Genre
import tables.GenreTable
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext

class GenreRep(val db: Database)(implicit ec: ExecutionContext)
  extends BaseRepository[Genre](GenreTable.query) {
}
