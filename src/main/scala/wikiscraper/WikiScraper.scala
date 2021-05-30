package wikiscraper

import scala.util.Random
import scala.collection.mutable
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import org.jsoup.Jsoup
import java.io.{File, FileWriter, BufferedWriter}

class WikiScraper(initNames: List[String], initSeen: List[String]) {
  import WikiScraper.Config

  private var myNamePool: List[String] = initNames
  private var mySeen: mutable.Set[String] = mutable.Set.from(initSeen)

  /** Retrive the current names to be fetched.
    */
  def namePool: List[String] = myNamePool

  /** Return all names that have been fetched.
    */
  def seen: Set[String] = mySeen.toSet

  private def urlOfName(name: String): String =
    "https://en.wikipedia.org/wiki/" + name

  def fetchEntryByName(name: String): Future[WikiParser.ParseResult[WikiEntry]] = Future {
    val url = urlOfName(name)
    try {
      val doc = Jsoup.connect(url).get()
      val res = WikiParser.parseWikiEntry(url, doc)
      res foreach { e => println(s"[FETCHED] fetched wiki entry for ${e.entryId}") }
      res
    } catch {
      case e: Exception =>
        Left(WikiParser.ConnectError(e.toString))
    }
  }

  def serializeEntry(e: WikiEntry)(using config: Config): Unit = {
    val path = config.saveDir + e.entryId + ".json"
    val file = new File(path)
    val writer = new BufferedWriter(new FileWriter(file))
    writer.write(e.jsonify)
    writer.close()
  }

  def serializeEntries(es: List[WikiEntry])(using Config): Unit = {
    val futures = es map { e => Future { serializeEntry(e) } }
    futures foreach { f => Await.result(f, Duration.Inf) }
  }

  def fetchAll(names: List[String]): List[WikiEntry] = {
    val futures = names map fetchEntryByName
    val results = futures map { x => Await.result(x, Duration.Inf) }
    names zip results flatMap { (name, res) =>
      res match {
        case Left(err) =>
          println(s"[WARNING] error when fetching $name: $err")
          None
        case Right(x) => Some(x)
      }
    }
  }

  def iterate(using config: Config): Unit = {
    import Tools.timeit
    val totalNames = myNamePool.length
    val (entries, t1) = timeit { fetchAll(myNamePool) }
    val (_, t2) = timeit { serializeEntries(entries) }
    val succNames = entries.length

    mySeen ++= entries map { e => e.entryId }

    val nextNames = {
      val all: Set[String] = (entries flatMap { e => e.outlinks }).toSet
      val xs = all.diff(mySeen).toList
      if xs.length > config.maxBranch then
        Random.shuffle(xs).take(config.maxBranch)
      else
        xs
    }
    myNamePool = nextNames

    println(s"[FINISHED] iteration: total $totalNames, succ ${succNames}, fail ${totalNames - succNames}, fetch time $t1, serialize time $t2, seen ${mySeen.size}, next ${nextNames.length}")
  }
}

object WikiScraper {
  /** Configuration of the scraper.
    * 
    * @param saveDir The directory to save scraped wiki to.
    * @param maxBranch Maximum branches when scraping.
    */
  case class Config(saveDir: String, maxBranch: Int)
}

