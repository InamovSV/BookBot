import scala.io.Source

object Main {

  def main(args: Array[String]): Unit = {
    val token = args.head
    new Bot(token)
  }
}
