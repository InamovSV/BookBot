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

  def removeById(id: Long) = db.run(BookTable.query.filter(_.id === id).delete)

  //  def removeById(ids: Set[Long]) = db.run(BookTable.query.filter(x => ids.exists(_ == x.id)).delete)

  def removeUserBook(title: String, userId: Long) =
    db.run(BookTable.query
      .filter(_.title === title)
      .join(BookUserTable.query)
      .on(_.id === _.bookId)
      .filter(_._2.userId === userId)
      .map(_._1)
      .delete)


  def findSimpleUserBooksByLogin(login: Long) = for (
    res <- db.run
    (BookUserTable.query
      .filter(_.userId === login)
      .join(BookTable.query)
      .on(_.bookId === _.id)
      .map(_._2)
      .join(BookAuthoringTable.query)
      .on(_.id === _.bookId)
      .join(AuthorTable.query)
      .on(_._2.authId === _.id)
      .map(x => (x._1._1.title, x._2.name, x._1._1.ref))
      .result)
  ) yield res.groupBy(_._1).map(x => (x._1, x._2.map(_._2).mkString(", "), x._2.head._3)).toSet

  def getBookByTitle(title: String):Future[Set[Book]] = for (
    res <- db.run(BookTable.query
      .filter(_.title === title)
      .result)
  ) yield res.toSet
}
