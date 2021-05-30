package wikiscraper

object Tools {
  def timeit[T](body: => T): (T, Double) = {
    val tic = System.nanoTime
    val value = body
    val toc = System.nanoTime
    val duration = (toc - tic).toDouble / 1e9
    value -> duration
  }
}

