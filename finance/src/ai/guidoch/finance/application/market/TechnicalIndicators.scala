package ai.guidoch.finance.application.market

import scala.collection.mutable.ArrayBuffer
import scala.math.*

object TechnicalIndicators {

  def calculateSMA(prices: Seq[Double], period: Int): Seq[Double] = {
    prices.sliding(period).map(window => window.sum / window.length).toSeq
  }

  def calculateEMA(prices: Seq[Double], period: Int): Seq[Double] = {
    val multiplier = 2.0 / (period + 1.0)
    val ema        = ArrayBuffer[Double]()

    if (prices.nonEmpty) {
      ema += prices.head

      for (i <- 1 until prices.length) {
        val currentEma = (prices(i) * multiplier) + (ema(i - 1) * (1 - multiplier))
        ema += currentEma
      }
    }
    ema.toSeq
  }

  def calculateRSI(prices: Seq[Double], period: Int = 14): Seq[Double] = {
    val changes = prices.zip(prices.tail).map { case (prev, curr) => curr - prev }
    val gains   = changes.map(change => if (change > 0) change else 0.0)
    val losses  = changes.map(change => if (change < 0) -change else 0.0)

    val avgGains  = calculateSMA(gains, period)
    val avgLosses = calculateSMA(losses, period)

    avgGains.zip(avgLosses).map { case (gain, loss) =>
      if (loss == 0) 100.0
      else {
        val rs = gain / loss
        100.0 - (100.0 / (1.0 + rs))
      }
    }
  }

  def calculateVolatility(prices: Seq[Double], period: Int = 20): Double = {
    val returns    = prices.zip(prices.tail).map { case (prev, curr) => log(curr / prev) }
    val meanReturn = returns.sum / returns.length
    val variance   = returns.map(ret => pow(ret - meanReturn, 2)).sum / returns.length
    sqrt(variance) * sqrt(252) // Annualized volatility
  }

  def calculateDrawdown(prices: Seq[Double]): Seq[Double] = {
    val runningMax = prices.scanLeft(0.0)((max, price) => math.max(max, price)).tail
    prices.zip(runningMax).map { case (price, peak) => (price - peak) / peak }
  }
}
