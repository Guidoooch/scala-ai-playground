package ai.guidoch.finance.application.market

import ai.guidoch.finance.application.news.NewsAnalyzer
import ai.guidoch.finance.domain.market.{MarketCrash, MarketPrediction}
import ai.guidoch.finance.infrastructure.market.client.marketstack.MarketStackClient
import cats.effect.IO

import java.time.OffsetDateTime

class FinancialAnalyzer {

  private val crashDetector    = new CrashDetector()
  private val marketDataClient = new MarketStackClient()
  private val newsAnalyzer     = new NewsAnalyzer()
  private val predictor        = new MarketPredictor()

  private val to   = OffsetDateTime.now()
  private val from = to.minusYears(10)

  def analyzeMarket(symbol: String): IO[AnalysisReport] = for
    marketData <- marketDataClient.fetchMarketData(symbol, from, to)
    news       <- newsAnalyzer.fetchNewsData(symbol)
  yield
    val crashes    = crashDetector.detectCrashes(marketData)
    val prediction = predictor.predictMarket(marketData, crashes, news)

    AnalysisReport(
      symbol = symbol,
      analysisDate = OffsetDateTime.now(),
      marketData = marketData,
      crashes = crashes,
      news = news,
      prediction = prediction,
      summary = generateSummary(crashes, prediction)
    )

  private def generateSummary(crashes: Seq[MarketCrash], prediction: MarketPrediction): String =
    val crashFreq =
      if crashes.nonEmpty then
        val avgDuration = crashes.map(_.duration).sum.toDouble / crashes.length
        val avgDecline  = crashes.map(_.crashPercentage).sum / crashes.length
        s"${crashes.length} crashes detected with average decline of ${(avgDecline * 100).round}% over ${avgDuration.round} days"
      else "No significant crashes detected"

    s"""
       |Market Analysis Summary:
       |- $crashFreq
       |- Prediction: ${prediction.predictedDirection} (${(prediction.confidenceScore * 100).round}% confidence)
       |- Risk Level: ${prediction.riskLevel}
       |
       |Based on technical analysis, historical patterns, and current news sentiment.
    """.stripMargin

}
