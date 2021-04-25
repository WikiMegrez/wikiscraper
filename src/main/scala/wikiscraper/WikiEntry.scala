package wikiscraper

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
                     otherImages: List[String])

object WikiEntry
