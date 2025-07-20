package ai.guidoch.finance.infrastructure.market

import ai.guidoch.finance.domain.market.MarketData
import ai.guidoch.finance.infrastructure.market.client.marketstack.model.MarketStackResponse

object MarketDataAdapter:

  def fromMarketStackResponse(response: MarketStackResponse): Seq[MarketData] =
    response.data.map: data =>
      MarketData(
        date = data.date,
        open = data.open,
        high = data.high,
        low = data.low,
        close = data.close,
        volume = data.volume
      )
