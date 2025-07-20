package ai.guidoch.finance.application.market

import ai.guidoch.finance.domain.market.{MarketCrash, MarketData}

import scala.collection.mutable.ArrayBuffer

class CrashDetector {

  def detectCrashes(
      data: Seq[MarketData],
      crashThreshold: Double = -0.10, // 10% decline
      minDuration: Int = 5
  ): Seq[MarketCrash] = {

    val prices    = data.map(_.close)
    val drawdowns = TechnicalIndicators.calculateDrawdown(prices)
    val crashes   = ArrayBuffer[MarketCrash]()

    var inCrash    = false
    var crashStart = Option.empty[Int]
    var peakPrice  = 0.0
    var peakIndex  = 0

    for ((drawdown, index) <- drawdowns.zipWithIndex) do
      if !inCrash && drawdown <= crashThreshold then
        // Crash starts
        inCrash = true
        crashStart = Some(findPeakBefore(prices, index))
        peakPrice = prices(crashStart.get)
        peakIndex = crashStart.get
      else if inCrash && drawdown > crashThreshold * 0.5 then
        // Crash recovery (50% recovery from crash threshold)
        val troughIndex = findTroughInRange(prices, peakIndex, index)
        val troughPrice = prices(troughIndex)
        val duration    = index - peakIndex

        if duration >= minDuration then
          val crashPercentage = (troughPrice - peakPrice) / peakPrice
          val volumeSpike     = calculateVolumeSpike(data, peakIndex, index)

          crashes += MarketCrash(
            startDate = data(peakIndex).date,
            endDate = data(index).date,
            peakPrice = peakPrice,
            troughPrice = troughPrice,
            crashPercentage = crashPercentage,
            duration = duration,
            volumeSpike = volumeSpike
          )

        inCrash = false
        crashStart = None

    crashes.toSeq
  }

  private def findPeakBefore(prices: Seq[Double], index: Int, lookback: Int = 20): Int =
    val start   = math.max(0, index - lookback)
    val segment = prices.slice(start, index + 1)
    start + segment.zipWithIndex.maxBy(_._1)._2

  private def findTroughInRange(prices: Seq[Double], start: Int, end: Int): Int =
    val segment = prices.slice(start, end + 1)
    start + segment.zipWithIndex.minBy(_._1)._2

  private def calculateVolumeSpike(data: Seq[MarketData], start: Int, end: Int): Double =
    val crashVolumes = data.slice(start, end + 1).map(_.volume)
    val avgVolume    = data.take(start).takeRight(20).map(_.volume).sum / 20
    crashVolumes.max / avgVolume

}
