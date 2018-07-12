package repositories

import model.HasId
import slick.jdbc.PostgresProfile.api._
import tables.BookTable

import collection.immutable
import scala.concurrent.Future


trait BaseTable[A <: HasId] {
  self: Table[A] =>

  val id: Rep[Long]
}

abstract class CrudRepository[A](query: TableQuery[_ <: Table[A]]) {
  def db: Database

  def insert(item: A): Future[Int] = db.run(query += item)

  def insert(items: immutable.Seq[A]): Future[Option[Int]] = db.run(query ++= items)
}

abstract class BaseRepository[A <: HasId](query: TableQuery[_ <: Table[A] with BaseTable[A]]) extends CrudRepository[A](query){
  def db: Database

  def findById(id: Long): Future[Option[A]] = db.run(query.filter(_.id === id).result.headOption)

  def update(item: A): Future[Int] = db.run(query.filter(_.id === item.id).update(item))

  def deleteById(id: Long): Future[Int] = db.run(query.filter(_.id === id).delete)
}
