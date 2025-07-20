package ai.guidoch.finance.infrastructure.market.client.marketstack.model

import upickle.default.ReadWriter

case class MarketStackResponse(
    data: Seq[Data],
    pagination: Pagination
) derives ReadWriter
