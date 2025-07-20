package ai.guidoch.finance.domain.market

import java.time.OffsetDateTime

case class MarketData(
    date: OffsetDateTime,
    open: Double,
    high: Double,
    low: Double,
    close: Double,
    volume: Double
)
