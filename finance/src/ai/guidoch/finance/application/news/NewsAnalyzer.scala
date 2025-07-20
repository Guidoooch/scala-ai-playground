package ai.guidoch.finance.application.news

import ai.guidoch.finance.domain.news.NewsArticle
import cats.effect.IO

import java.time.OffsetDateTime

class NewsAnalyzer {

  // Simple sentiment words dictionary
  private val positiveWords = Set(
    "growth",
    "profit",
    "gain",
    "bull",
    "rally",
    "surge",
    "rise",
    "boost",
    "strong",
    "positive",
    "optimistic",
    "recovery",
    "expansion",
    "success"
  )

  private val negativeWords = Set(
    "crash",
    "fall",
    "decline",
    "bear",
    "recession",
    "loss",
    "drop",
    "plunge",
    "weak",
    "negative",
    "pessimistic",
    "crisis",
    "contraction",
    "failure"
  )

  def analyzeSentiment(text: String): Double = {
    val words               = text.toLowerCase.split("\\W+").toSet
    val positiveCount       = words.intersect(positiveWords).size
    val negativeCount       = words.intersect(negativeWords).size
    val totalSentimentWords = positiveCount + negativeCount

    if (totalSentimentWords == 0) 0.0
    else (positiveCount - negativeCount).toDouble / totalSentimentWords
  }

  def fetchNewsData(symbol: String): IO[Seq[NewsArticle]] =
    IO {
      // Simulated news data - in real implementation, would fetch from news APIs
      generateSampleNews(symbol)
    }

  private def generateSampleNews(symbol: String): Seq[NewsArticle] = {
    val sampleNews = Seq(
      (
        "Market Shows Strong Recovery Signs",
        "The market has shown remarkable resilience with strong growth indicators and positive investor sentiment.",
        -0.5
      ),
      (
        "Economic Uncertainty Looms",
        "Analysts warn of potential market volatility due to geopolitical tensions and inflation concerns.",
        -0.7
      ),
      (
        "Tech Sector Leads Market Rally",
        "Technology stocks surge as innovation drives optimistic outlook for future growth.",
        0.8
      ),
      (
        "Federal Reserve Signals Caution",
        "Central bank officials express concerns about economic headwinds and market stability.",
        -0.3
      ),
      (
        "Corporate Earnings Beat Expectations",
        "Strong quarterly results boost investor confidence and drive market optimism.",
        0.6
      )
    )

    sampleNews.zipWithIndex.map { case ((title, content, sentiment), index) =>
      NewsArticle(
        title = title,
        content = content,
        date = OffsetDateTime.now().minusDays(index),
        sentiment = sentiment + (scala.util.Random.nextGaussian() * 0.1) // Add some noise
      )
    }
  }
}
