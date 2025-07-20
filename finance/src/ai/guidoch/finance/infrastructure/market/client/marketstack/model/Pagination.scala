package ai.guidoch.finance.infrastructure.market.client.marketstack.model

import upickle.default.ReadWriter

case class Pagination(
    count: Int,
    limit: Int,
    offset: Int,
    total: Int
) derives ReadWriter
