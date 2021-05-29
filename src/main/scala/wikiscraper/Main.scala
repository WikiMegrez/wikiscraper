package wikiscraper
import org.jsoup._
import nodes._

@main def run() = {
  println("Welcome to wikiscraper")
  val url = "https://en.wikipedia.org/wiki/Scala_(programming_language)"
  val doc: Document = Jsoup.connect(url).get()
  println(doc)

  println(doc.select("h1#firstHeading"))

  val res = WikiParser.parseWikiEntry(url, doc)
  println(res)
}
