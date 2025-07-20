package ai.guidoch.finance.application.market

import ai.guidoch.finance.domain.market.{MarketCrash, MarketData, MarketPrediction}
import ai.guidoch.finance.domain.news.NewsArticle

import java.time.OffsetDateTime

case class AnalysisReport(
    symbol: String,
    analysisDate: OffsetDateTime,
    marketData: Seq[MarketData],
    crashes: Seq[MarketCrash],
    news: Seq[NewsArticle],
    prediction: MarketPrediction,
    summary: String
)
