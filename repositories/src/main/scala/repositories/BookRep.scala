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

  private val insertWithIdQuery =
    BookTable.query returning BookTable.query.map(_.id) into ((item, id) => item.copy(id = id))

  def insertWithId(item: Book): Future[Book] = db.run(insertWithIdQuery += item)

  def findAll(): Future[Vector[Book]] = db.run(BookTable.query.to[Vector].result)

  def findSimpleUserBooksByLogin(login: Long) = for (
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
      .map(x => (x._1._1.title, x._2.name))
      .result)
  ) yield res.toSet
}
