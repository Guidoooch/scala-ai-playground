package ai.guidoch.finance.infrastructure

import upickle.default.{ReadWriter, readwriter}

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object JavaDateTimeReadWriter:

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")

  given ReadWriter[OffsetDateTime] = readwriter[ujson.Value].bimap[OffsetDateTime](
    dateTime => formatter.format(dateTime),
    json => OffsetDateTime.parse(json.str, formatter)
  )


