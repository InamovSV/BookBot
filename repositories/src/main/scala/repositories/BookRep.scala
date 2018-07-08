package repositories

import model.Book
import tables._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{ExecutionContext, Future}

class BookRep(val db: Database)(implicit ec: ExecutionContext)
  extends BaseRepository[Book](BookTable.query) {
  def createSchema(): Future[Unit] = {
    db.run((
      BookTable.query.schema ++
        AuthorTable.query.schema ++
        GenreTable.query.schema ++
        UserTable.query.schema ++
        BookAuthoringTable.query.schema ++
        BookGenreTable.query.schema ++
        BookUserTable.query.schema
      ).create)
  }

  def dropSchema(): Future[Unit] = {
    db.run((
      BookTable.query.schema ++
        AuthorTable.query.schema ++
        GenreTable.query.schema ++
        UserTable.query.schema ++
        BookAuthoringTable.query.schema ++
        BookGenreTable.query.schema ++
        BookUserTable.query.schema
      ).drop)
  }

  def findAll(): Future[Vector[Book]] = db.run(BookTable.query.to[Vector].result)

  def findUserBookByLogin(login: String): Future[Set[Book]] = for(
    res <- db.run
    (UserTable.query
      .filter(_.name === login)
      .join(BookUserTable.query)
      .on(_.id === _.userId)
      .join(BookTable.query)
      .on(_._2.bookId === _.id)
      .map(_._2)
      .result)
  ) yield res.toSet
}
