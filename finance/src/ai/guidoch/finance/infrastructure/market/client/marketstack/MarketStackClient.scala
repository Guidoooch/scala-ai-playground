package ai.guidoch.finance.infrastructure.market.client.marketstack

import ai.guidoch.finance.application.market.client.MarketDataClient
import ai.guidoch.finance.domain.market.MarketData
import ai.guidoch.finance.infrastructure.market.MarketDataAdapter
import ai.guidoch.finance.infrastructure.market.client.marketstack.model.MarketStackResponse
import cats.effect.IO
import sttp.client4
import sttp.client4.UriContext
import sttp.client4.httpclient.cats.HttpClientCatsBackend

import java.time.OffsetDateTime

class MarketStackClient extends MarketDataClient:

  private val accessKey = ""
  private val baseUrl = uri"http://api.marketstack.com/v2/eod/latest".withParam("access_key", accessKey)

  private val backendResource = HttpClientCatsBackend.resource[IO]()

  override def fetchMarketData(symbol: String): IO[Seq[MarketData]] =
    val request = client4.basicRequest.get(baseUrl.addParam("symbols", symbol))
    backendResource.use(backend => request.send(backend))
      .map(_.body)
      .flatMap:
        case Right(body) =>
          val response = upickle.default.read[MarketStackResponse](body)
          val data = MarketDataAdapter.fromMarketStackResponse(response)
          IO.pure(data)
        case Left(error) =>
          IO.raiseError(new RuntimeException(error))

  private def generateSampleMarketData(symbol: String): Seq[MarketData] = {
    val random = new scala.util.Random(42) // Fixed seed for reproducible results
    val startDate = OffsetDateTime.now().minusYears(5)
    var currentPrice = 100.0

    (0 until 1260).map { i => // ~5 years of trading days
      val date = startDate.plusDays(i)

      // Simulate price movements with occasional crashes
      val normalMove = random.nextGaussian() * 0.02 // 2% daily volatility
      val crashProbability = 0.002 // 0.2% chance of crash each day
      val crashMove = if (random.nextDouble() < crashProbability) {
        -0.05 - (random.nextDouble() * 0.15) // 5-20% crash
      } else 0.0

      val dailyReturn = normalMove + crashMove
      val newPrice = currentPrice * (1 + dailyReturn)

      val open = currentPrice
      val high = math.max(open, newPrice) * (1 + random.nextDouble() * 0.01)
      val low = math.min(open, newPrice) * (1 - random.nextDouble() * 0.01)
      val close = newPrice
      val volume = (1000000 + random.nextInt(2000000)).toLong

      currentPrice = newPrice

      MarketData(date, open, high, low, close, volume)
    }
  }
