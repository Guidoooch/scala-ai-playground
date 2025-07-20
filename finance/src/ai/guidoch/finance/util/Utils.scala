package ai.guidoch.finance.util

object Utils {

  def formatPercentage(value: Double): String = f"${value * 100}%+.2f%%"

  def formatCurrency(value: Double): String = f"$$$value%.2f"

  def calculateCompoundReturn(returns: Seq[Double]): Double = {
    returns.foldLeft(1.0)((acc, ret) => acc * (1 + ret)) - 1.0
  }

  def calculateSharpeRatio(returns: Seq[Double], riskFreeRate: Double = 0.02): Double = {
    val excessReturns   = returns.map(_ - riskFreeRate / 252) // Daily risk-free rate
    val avgExcessReturn = excessReturns.sum / excessReturns.length
    val returnStdDev    = math.sqrt(excessReturns.map(r => math.pow(r - avgExcessReturn, 2)).sum / excessReturns.length)
    avgExcessReturn / returnStdDev * math.sqrt(252) // Annualized
  }
}
