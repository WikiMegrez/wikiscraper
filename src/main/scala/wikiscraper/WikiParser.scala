package wikiscraper

import scala.jdk.CollectionConverters._
import org.jsoup.nodes._

/**
 * Parsing HTML document to [[WikiEntry]].
 */
object WikiParser {
  class ParseError(val msg: String)
  case class ConnectError(errorMsg: String) extends ParseError(s"Connection error: $errorMsg")
  case class InvalidUrl(url: String) extends ParseError(s"Invalid url: $url")
  case class BadDocument(reason: String) extends ParseError(s"Bad document: $reason")

  type ParseResult[+X] = Either[ParseError, X]

  def parseWikiEntry(url: String, doc: Document): ParseResult[WikiEntry] = {
    def extractName(url: String): Option[String] = {
      val u =
        if url.endsWith("/") then
          url.dropRight(1)
        else
          url
      if u.startsWith("https://en.wikipedia.org/wiki/") then
        u.split('/').lastOption flatMap { location => location.split('#').headOption }
      else
        None
    }

    def getName = extractName(url) match {
      case Some(name) => Right(name)
      case None => Left(InvalidUrl(url))
    }

    def getTitle = doc.select("h1#firstHeading").asScala.toList match {
      case x :: _ => Right(x.text())
      case Nil => Left(BadDocument("first heading not found"))
    }

    def getContent = doc.select("div.mw-parser-output").asScala.toList match {
      case Nil => Left(BadDocument("content div not found"))
      case div :: _ =>
        val children = div.children().asScala
        var beforeToc: Boolean = true
        val mainDesc: StringBuilder = new StringBuilder
        val content: StringBuilder = new StringBuilder
        children foreach { ele =>
          if ele.id() == "toc" then
            beforeToc = false

          if ele.nodeName() == "p" then
            if beforeToc then
              mainDesc ++= ele.text()
            else
              content ++= ele.text()
        }
        Right(mainDesc.result(), content.result())
    }

    def getOutlinks: List[String] = doc.select("a").asScala.toList flatMap { ele =>
      val href = ele.absUrl("href")

      extractName(href)
    }

    def getImages: List[String] = doc.select("img").asScala.toList map { ele => ele.absUrl("src") }
    val (mainImage, otherImages) = getImages match {
      case x :: xs => (Some(x), xs)
      case Nil => (None, Nil)
    }

    for {
      name <- getName;
      title <- getTitle;
      content  <- getContent;
    } yield WikiEntry(
      title = title,
      entryId = name,
      mainDesc = content._1,
      mainImage = mainImage,
      outlinks = getOutlinks.distinct,
      content = content._2,
      otherImages = otherImages.distinct
    )
  }
}
