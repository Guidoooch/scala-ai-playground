package ai.guidoch.finance.domain.market

import java.time.OffsetDateTime

case class MarketCrash(
    startDate: OffsetDateTime,
    endDate: OffsetDateTime,
    peakPrice: Double,
    troughPrice: Double,
    crashPercentage: Double,
    duration: Int, // days
    volumeSpike: Double
)
