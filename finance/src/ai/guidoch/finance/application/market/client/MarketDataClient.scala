package ai.guidoch.finance.application.market.client

import ai.guidoch.finance.domain.market.MarketData
import cats.effect.IO

import java.time.OffsetDateTime

trait MarketDataClient:

  def fetchMarketData(symbol: String, dateFrom: OffsetDateTime, dateTime: OffsetDateTime): IO[Seq[MarketData]]
