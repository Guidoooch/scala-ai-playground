package ai.guidoch.finance.domain.news

import java.time.OffsetDateTime

case class NewsArticle(
    title: String,
    content: String,
    date: OffsetDateTime,
    sentiment: Double // -1 to 1, where -1 is very negative, 1 is very positive
)
