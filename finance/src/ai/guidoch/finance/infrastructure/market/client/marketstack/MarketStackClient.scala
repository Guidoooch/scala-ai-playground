package ai.guidoch.finance.infrastructure.market.client.marketstack

import ai.guidoch.finance.application.market.client.MarketDataClient
import ai.guidoch.finance.domain.market.MarketData
import ai.guidoch.finance.infrastructure.JavaDateTimeReadWriter
import ai.guidoch.finance.infrastructure.market.MarketDataAdapter
import ai.guidoch.finance.infrastructure.market.client.marketstack.model.MarketStackResponse
import cats.effect.IO
import sttp.client4
import sttp.client4.UriContext
import sttp.client4.httpclient.cats.HttpClientCatsBackend

import java.time.OffsetDateTime

class MarketStackClient extends MarketDataClient:

  private val accessKey = ""

  private val offsetStep = 1000
  private val baseUrl   = uri"https://api.marketstack.com/v2/eod"
    .withParams(
      "access_key" -> accessKey,
      "limit"      -> offsetStep.toString,
      "sort"       -> "ASC"
    )

  private val backendResource = HttpClientCatsBackend.resource[IO]()

  override def fetchMarketData(symbol: String, from: OffsetDateTime, to: OffsetDateTime): IO[Seq[MarketData]] =
    val dateFrom = JavaDateTimeReadWriter.formatter.format(from)
    val dateTo   = JavaDateTimeReadWriter.formatter.format(to)

    val uri = baseUrl.addParams(
      "date_from" -> dateFrom,
      "date_to"   -> dateTo,
      "symbols"   -> symbol
    )

    def fetchRecursive(offset: Long)(acc: Seq[MarketData]): IO[Seq[MarketData]] =
      val request = client4.basicRequest.get(uri.addParam("offset", offset.toString))
      backendResource
        .use(backend => request.send(backend))
        .map(_.body)
        .flatMap:
          case Right(body) =>
            val response = upickle.default.read[MarketStackResponse](body)
            val data     = MarketDataAdapter.fromMarketStackResponse(response)

            println(response.pagination.count)

            if response.pagination.count > 0 then
              fetchRecursive(offset + offsetStep)(acc ++ data)
            else
              IO.pure(acc)
          case Left(error) =>
            IO.raiseError(new RuntimeException(error))

    fetchRecursive(0L)(Seq.empty[MarketData])
