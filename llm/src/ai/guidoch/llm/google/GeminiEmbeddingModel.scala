package ai.guidoch.llm.google

import ai.guidoch.llm.EmbeddingModel
import ai.guidoch.llm.model.Embedding
import cats.effect.IO
import cats.implicits.toTraverseOps
import sttp.client3.*
import upickle.default.*

/** Implementation of EmbeddingModel for Google's Gemini models
  */
private class GeminiEmbeddingModel(apiKey: String, model: String)
    extends EmbeddingModel {

  private val backend = HttpClientSyncBackend()
  private val baseUrl = uri"https://generativelanguage.googleapis.com/v1/models/$model:embedContent?key=$apiKey"

  override def embed(text: String): IO[Embedding] = IO {
    // Prepare the request body
    val requestBody = ujson.Obj(
      "content" -> ujson.Obj(
        "parts" -> ujson.Arr(
          ujson.Obj("text" -> text)
        )
      ),
      "taskType" -> "RETRIEVAL_DOCUMENT"
    )

    // Make the API request
    val response = basicRequest
      .post(baseUrl)
      .header("Content-Type", "application/json")
      .body(write(requestBody))
      .send(backend)

    // Parse the response
    response.body match {
      case Right(body) =>
        val json = ujson.read(body)
        val embedding =
          json("embedding")("values").arr.map(_.num.toFloat).toArray
        Embedding(vector = embedding, text = text)
      case Left(error) =>
        throw new RuntimeException(s"Error calling Gemini API: $error")
    }
  }

  override def embedBatch(texts: Seq[String]): IO[Seq[Embedding]] = {
    // For simplicity, we'll implement this as multiple single embedding requests
    // In a real implementation, you would batch the requests if the API supports it
    texts.traverse { text =>
      embed(text)
    }
  }
}

object GeminiEmbeddingModel {

  /** Create a Google Gemini embedding model
    *
    * @param apiKey
    *   The Google API key
    * @param model
    *   The model to use (default: embedding-001)
    * @return
    *   An embedding model for Google Gemini
    */
  def apply(
      apiKey: String,
      model: String = "embedding-001"
  ): GeminiEmbeddingModel =
    new GeminiEmbeddingModel(apiKey, model)

}
