package wikiscraper

@main def run(iterations: Int) = {
  println("Welcome to wikiscraper")
  val names = List("Scala_(programming_language)", "Beijing_University_of_Posts_and_Telecommunications")
  val config = WikiScraper.Config("output/", 500)
  val scraper = new WikiScraper(names, Nil)
  given WikiScraper.Config = config

  for _ <- 1 to iterations do scraper.iterate
}

