package ai.guidoch.finance.domain.market

case class MarketPrediction(
    predictedDirection: String, // "BULLISH", "BEARISH", "NEUTRAL"
    confidenceScore: Double,    // 0 to 1
    priceTarget: Option[Double],
    timeHorizon: Int, // days
    riskLevel: String // "LOW", "MEDIUM", "HIGH"
)
