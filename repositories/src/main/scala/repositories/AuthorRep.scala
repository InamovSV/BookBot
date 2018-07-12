package repositories

import model.Author
import tables.AuthorTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class AuthorRep(val db: Database)(implicit ec: ExecutionContext)
  extends BaseRepository[Author](AuthorTable.query) {

  private val insertWithIdQuery =
    AuthorTable.query returning AuthorTable.query.map(_.id) into ((item, id) => item.copy(id = id))

  def insertWithId(item: Author): Future[Author] = db.run(insertWithIdQuery += item)
}
