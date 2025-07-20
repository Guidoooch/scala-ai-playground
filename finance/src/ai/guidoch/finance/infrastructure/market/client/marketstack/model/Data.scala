package ai.guidoch.finance.infrastructure.market.client.marketstack.model

import ai.guidoch.finance.infrastructure.JavaDateTimeReadWriter.given
import upickle.default.ReadWriter

import java.time.OffsetDateTime

case class Data(
    open: Double,
    high: Double,
    low: Double,
    close: Double,
    volume: Double,
    adj_high: Double,
    adj_low: Double,
    adj_close: Double,
    adj_open: Double,
    adj_volume: Double,
    split_factor: Double,
    dividend: Double,
    name: String,
    exchange_code: String,
    asset_type: String,
    price_currency: String,
    symbol: String,
    exchange: String,
    date: OffsetDateTime
) derives ReadWriter
