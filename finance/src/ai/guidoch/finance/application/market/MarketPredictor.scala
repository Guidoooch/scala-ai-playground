package ai.guidoch.finance.application.market

import ai.guidoch.finance.application.market.MarketPredictor.FeatureVector
import ai.guidoch.finance.domain.market.{MarketCrash, MarketData, MarketPrediction}
import ai.guidoch.finance.domain.news.NewsArticle

import java.time.OffsetDateTime

class MarketPredictor {

  def predictMarket(
      marketData: Seq[MarketData],
      crashes: Seq[MarketCrash],
      news: Seq[NewsArticle]
  ): MarketPrediction =
    val features   = extractFeatures(marketData, crashes, news)
    val prediction = makeMLPrediction(features)

    prediction

  private def extractFeatures(
      marketData: Seq[MarketData],
      crashes: Seq[MarketCrash],
      news: Seq[NewsArticle]
  ): FeatureVector = {

    val prices       = marketData.map(_.close)
    val volumes      = marketData.map(_.volume)
    val currentPrice = prices.last

    // Technical indicators
    val sma20      = TechnicalIndicators.calculateSMA(prices, 20).lastOption.getOrElse(currentPrice)
    val rsi        = TechnicalIndicators.calculateRSI(prices).lastOption.getOrElse(50.0)
    val volatility = TechnicalIndicators.calculateVolatility(prices.takeRight(20))

    // Volume analysis
    val avgVolume   = volumes.takeRight(20).sum / 20
    val volumeRatio = volumes.last / avgVolume

    // Crash frequency (crashes per year)
    val yearsOfData    = marketData.length / 252.0 // Assuming 252 trading days per year
    val crashFrequency = crashes.length / math.max(yearsOfData, 1.0)

    // News sentiment
    val recentNews   = news.filter(_.date.isAfter(OffsetDateTime.now().minusDays(7)))
    val avgSentiment = if (recentNews.nonEmpty) {
      recentNews.map(_.sentiment).sum / recentNews.length
    } else 0.0

    // Seasonality (simple month-based factor)
    val month       = OffsetDateTime.now().getMonthValue
    val seasonality = math.sin(2 * math.Pi * month / 12.0) // Cyclical pattern

    FeatureVector(
      smaRatio = currentPrice / sma20,
      rsi = rsi,
      volatility = volatility,
      volumeRatio = volumeRatio,
      crashFrequency = crashFrequency,
      sentimentScore = avgSentiment,
      seasonality = seasonality
    )
  }

  private def makeMLPrediction(features: FeatureVector): MarketPrediction = {
    // Simplified ML model - in practice, would use proper ML libraries
    var score      = 0.0
    var confidence = 0.5

    // Technical analysis signals
    if (features.smaRatio > 1.05) score += 0.2
    else if (features.smaRatio < 0.95) score -= 0.2

    if (features.rsi > 70) score -= 0.15      // Overbought
    else if (features.rsi < 30) score += 0.15 // Oversold

    // Volatility impact
    if (features.volatility > 0.3) {
      score -= 0.1 // High volatility is concerning
      confidence -= 0.1
    }

    // Volume confirmation
    if (features.volumeRatio > 1.5) confidence += 0.1

    // Historical crash frequency
    if (features.crashFrequency > 0.5) score -= 0.1

    // Sentiment impact
    score += features.sentimentScore * 0.3
    confidence += math.abs(features.sentimentScore) * 0.2

    // Seasonality
    score += features.seasonality * 0.1

    // Normalize confidence
    confidence = math.max(0.1, math.min(1.0, confidence))

    val direction =
      if (score > 0.1) "BULLISH"
      else if (score < -0.1) "BEARISH"
      else "NEUTRAL"

    val riskLevel =
      if (features.volatility > 0.4 || features.crashFrequency > 0.8) "HIGH"
      else if (features.volatility > 0.2 || features.crashFrequency > 0.4) "MEDIUM"
      else "LOW"

    MarketPrediction(
      predictedDirection = direction,
      confidenceScore = confidence,
      priceTarget = None, // Could calculate based on technical levels
      timeHorizon = 30,   // 30 days
      riskLevel = riskLevel
    )
  }
}

object MarketPredictor:

  case class FeatureVector(
      smaRatio: Double,       // Current price / SMA(20)
      rsi: Double,            // RSI indicator
      volatility: Double,     // Recent volatility
      volumeRatio: Double,    // Volume vs average
      crashFrequency: Double, // Historical crash frequency
      sentimentScore: Double, // News sentiment
      seasonality: Double     // Seasonal factor
  )
