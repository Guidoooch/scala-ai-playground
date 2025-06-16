package ai.guidoch.llm.openai

import ai.guidoch.llm.EmbeddingModel
import ai.guidoch.llm.model.Embedding
import cats.effect.IO
import cats.implicits.toTraverseOps
import sttp.client3.*
import upickle.default.*

/** Implementation of EmbeddingModel for OpenAi
  */
private class OpenAiEmbeddingModel(apiKey: String, model: String)
    extends EmbeddingModel {

  private val backend = HttpClientSyncBackend()
  private val baseUrl = sttp.model.Uri("https://api.openai.com/v1/embeddings")

  override def embed(text: String): IO[Embedding] = IO {
    // Prepare the request body
    val requestBody = ujson.Obj(
      "model" -> model,
      "input" -> text
    )

    // Make the API request
    val response = basicRequest
      .post(baseUrl)
      .header("Authorization", s"Bearer $apiKey")
      .header("Content-Type", "application/json")
      .body(write(requestBody))
      .send(backend)

    // Parse the response
    response.body match {
      case Right(body) =>
        val json = ujson.read(body)
        val embedding =
          json("data")(0)("embedding").arr.map(_.num.toFloat).toArray
        Embedding(vector = embedding, text = text)
      case Left(error) =>
        throw new RuntimeException(s"Error calling OpenAi API: $error")
    }
  }

  override def embedBatch(texts: Seq[String]): IO[Seq[Embedding]] = {
    // For simplicity, we'll implement this as multiple single embedding requests
    // In a real implementation, you would batch the requests
    texts.traverse { text =>
      for {
        // Prepare the request body
        requestBody <- IO.pure(
          ujson.Obj(
            "model" -> model,
            "input" -> text
          )
        )
        // Make the API request
        response <- IO {
          basicRequest
            .post(baseUrl)
            .header("Authorization", s"Bearer $apiKey")
            .header("Content-Type", "application/json")
            .body(write(requestBody))
            .send(backend)
        }

        // Parse the response
        result <- response.body match {
          case Right(body) =>
            val json = ujson.read(body)
            val embedding =
              json("data")(0)("embedding").arr.map(_.num.toFloat).toArray
            IO.pure(Embedding(vector = embedding, text = text))
          case Left(error) =>
            IO.raiseError(RuntimeException(s"Error calling OpenAi API: $error"))
        }
      } yield result

    }
  }
}

object OpenAiEmbeddingModel {

  /** Create an OpenAi embedding model
    *
    * @param apiKey
    *   The OpenAi API key
    * @param model
    *   The model to use (default: text-embedding-ada-002)
    * @return
    *   An embedding model for OpenAi
    */
  def apply(
      apiKey: String,
      model: String = "text-embedding-ada-002"
  ): OpenAiEmbeddingModel =
    new OpenAiEmbeddingModel(apiKey, model)

}
