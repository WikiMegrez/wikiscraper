package wikiscraper

import io.circe.Codec
import io.circe.syntax._

/**
 * Wiki entry in Wikipedia.
 * @param title
 * @param entryId
 * @param mainDesc
 * @param mainImage
 * @param outlinks
 * @param content
 * @param otherImages
 */
case class WikiEntry(title: String,
                     entryId: String,
                     mainDesc: String,
                     mainImage: Option[String],
                     outlinks: List[String],
                     content: String,
                     otherImages: List[String]) derives Codec.AsObject {
  override def toString: String =
    s"WikiEntry\ntitle = $title\nid = $entryId\nmainDesc = $mainDesc\nmainImage = $mainImage\noutlinks = $outlinks"

  def jsonify: String = this.asJson.spaces2
}

object WikiEntry
