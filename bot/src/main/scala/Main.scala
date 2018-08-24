import scala.io.Source

object Main extends App{
  val token = Source.fromResource("token.txt").getLines().mkString
  val bot = new Bot(token)
}
