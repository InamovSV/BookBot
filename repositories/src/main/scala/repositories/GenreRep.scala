package repositories

import model.Genre
import tables._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class GenreRep(val db: Database)(implicit ec: ExecutionContext)
  extends BaseRepository[Genre](GenreTable.query) {
  private val insertWithIdQuery =
    GenreTable.query returning GenreTable.query.map(_.id) into ((item, id) => item.copy(id = id))

  def insertWithId(item: Genre): Future[Genre] = db.run(insertWithIdQuery += item)

  def getAllUserGenre(userId: Long) = for(
    res <- db.run(UserTable.query
      .filter(_.id === userId)
      .join(BookUserTable.query)
      .on(_.id === _.userId)
      .join(BookTable.query)
      .on(_._2.bookId === _.id)
      .map(_._2)
      .join(BookGenreTable.query)
      .on(_.id === _.bookId)
      .join(GenreTable.query)
      .on(_._2.genreId === _.id)
      .map(_._2.title)
      .result)
  ) yield res
}
