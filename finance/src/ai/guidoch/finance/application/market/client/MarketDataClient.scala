package ai.guidoch.finance.application.market.client

import ai.guidoch.finance.domain.market.MarketData
import cats.effect.IO

trait MarketDataClient:

  def fetchMarketData(symbol: String): IO[Seq[MarketData]]
