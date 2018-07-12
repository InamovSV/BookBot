package repositories

import model.Genre
import tables.GenreTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class GenreRep(val db: Database)(implicit ec: ExecutionContext)
  extends BaseRepository[Genre](GenreTable.query) {
  private val insertWithIdQuery =
    GenreTable.query returning GenreTable.query.map(_.id) into ((item, id) => item.copy(id = id))

  def insertWithId(item: Genre): Future[Genre] = db.run(insertWithIdQuery += item)
}
