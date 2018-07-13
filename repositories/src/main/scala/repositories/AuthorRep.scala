package repositories

import model.Author
import tables._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class AuthorRep(val db: Database)(implicit ec: ExecutionContext)
  extends BaseRepository[Author](AuthorTable.query) {

  private val insertWithIdQuery =
    AuthorTable.query returning AuthorTable.query.map(_.id) into ((item, id) => item.copy(id = id))

  def insertWithId(item: Author): Future[Author] = db.run(insertWithIdQuery += item)

  def getAllUserAuthor(login: Long) = for (
    res <- db.run
    (UserTable.query
      .filter(_.id === login)
      .join(BookUserTable.query)
      .on(_.id === _.userId)
      .join(BookTable.query)
      .on(_._2.bookId === _.id)
      .map(_._2)
      .join(BookAuthoringTable.query)
      .on(_.id === _.bookId)
      .join(AuthorTable.query)
      .on(_._2.authId === _.id)
      .map(_._2.name)
      .result)
  ) yield res
}
