package ai.guidoch.finance

import ai.guidoch.finance.application.market.{AnalysisReport, FinancialAnalyzer, TechnicalIndicators}
import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {

  private val analyzer = new FinancialAnalyzer()

  private def runAnalysis(symbol: String): IO[Unit] =
    IO.println(s"Starting financial analysis for $symbol...") >>
    analyzer.analyzeMarket(symbol)
      .flatMap(printReport)
      .recoverWith { exception =>
        IO.println(s"Analysis failed: ${exception.getMessage}")
      }

  private def printReport(report: AnalysisReport): IO[Unit] = for
    _ <- IO.println("\n" + "=" * 60)
    _ <- IO.println(s"FINANCIAL MARKET ANALYSIS REPORT")
    _ <- IO.println(s"Symbol: ${report.symbol}")
    _ <- IO.println(s"Analysis Date: ${report.analysisDate}")
    _ <- IO.println("=" * 60)

    // Market Data Summary
    latest      = report.marketData.last
    oldest      = report.marketData.head
    totalReturn = ((latest.close - oldest.close) / oldest.close * 100).round
    _ <- IO.println("\nMARKET DATA SUMMARY:")
    _ <- IO.println(f"Period: ${oldest.date} to ${latest.date}")
    _ <- IO.println(f"Total Return: $totalReturn%+d%%")
    _ <- IO.println(f"Current Price: $$${latest.close}%.2f")

    // Crash Analysis
    _ <- IO.println("\nCRASH ANALYSIS:") >> {
      if report.crashes.nonEmpty then
        val avgCrashSeverity      = report.crashes.map(_.crashPercentage).sum / report.crashes.length
        val crashFrequencyPerYear = report.crashes.length / 5.0 // 5 years of data
        IO.println(f"Total Crashes Detected: ${report.crashes.length}") >>
        IO.parTraverseN(1)(report.crashes.sortBy(_.startDate)) { crash =>
          IO.println(
            f"- ${crash.startDate} to ${crash.endDate}: ${crash.crashPercentage * 100}%.1f%% decline (${crash.duration} days)"
          )
        } >>
        IO.println(f"Average crash severity: ${avgCrashSeverity * 100}%.1f%%") >>
        IO.println(f"Crash frequency: $crashFrequencyPerYear%.1f per year")
      else IO.println("No significant market crashes detected in the analysis period.")
    }
    // News Sentiment
    _ <- IO.println("\nNEWS SENTIMENT ANALYSIS:") >> {
      val avgSentiment =
        if (report.news.nonEmpty)
          report.news.map(_.sentiment).sum / report.news.length
        else
          0.0

      val sentimentLabel =
        if (avgSentiment > 0.2) "Positive"
        else if (avgSentiment < -0.2) "Negative"
        else "Neutral"

      IO.println(f"Overall Sentiment: $sentimentLabel ($avgSentiment%.2f)") >>
      IO.println("\nRecent Headlines:") >>
      IO.parTraverseN(1)(report.news.take(3)) { article =>
        IO.println(f"- ${article.title} (Sentiment: ${article.sentiment}%.2f)")
      }
    }

    // Market Prediction
    _ <- IO.println("\nMARKET PREDICTION:") >> {
      val pred = report.prediction
      IO.println(
        f"""Direction: ${pred.predictedDirection}
           |Confidence: ${(pred.confidenceScore * 100).round}%%
           |Risk Level: ${pred.riskLevel}
           |Time Horizon: ${pred.timeHorizon} days
           |""".stripMargin
      )
    }

    // Summary
    _ <- IO.println("\nEXECUTIVE SUMMARY:") >> IO.println(report.summary)

    // Technical Indicators
    _ <- IO.println("\nTECHNICAL INDICATORS:") >> {
      val prices     = report.marketData.map(_.close)
      val currentRSI = TechnicalIndicators.calculateRSI(prices).lastOption.getOrElse(50.0)
      val volatility = TechnicalIndicators.calculateVolatility(prices.takeRight(20))
      val sma20      = TechnicalIndicators.calculateSMA(prices, 20).lastOption.getOrElse(latest.close)

      IO.println(
        f"""RSI (14): $currentRSI%.1f
           |Volatility (20d): ${volatility * 100}%.1f%%
           |Price vs SMA(20): ${(latest.close / sma20 - 1) * 100}%+.1f%%
           |""".stripMargin
      ) >>
      IO.println("=" * 60)
    }
  yield ()

  // Run the analysis
  private val symbols = Seq("SPY")

  override def run(args: List[String]): IO[ExitCode] =
    IO.parTraverseN(1)(symbols)(runAnalysis) >>
    IO.pure(ExitCode.Success)
}
