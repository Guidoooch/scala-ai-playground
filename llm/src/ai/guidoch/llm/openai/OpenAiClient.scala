package ai.guidoch.llm.openai

import ai.guidoch.llm.LLMClient
import ai.guidoch.llm.model.{LLMResponse, Prompt, TokenUsage}
import cats.effect.IO
import io.circe.generic.auto.*
import io.circe.syntax.*
import sttp.client4.*
import sttp.client4.circe.*
import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.model.Uri
import upickle.default.*

/**
 * Implementation of LLMClient for OpenAi
 */
class OpenAiClient(apiKey: String, model: String) extends LLMClient {


  private val backend = HttpClientSyncBackend()
  private val baseUrl = Uri("https://api.OpenAi.com/v1/chat/completions")

  override def complete(prompt: Prompt): IO[LLMResponse] = IO {
    // Prepare the request body
    val messages = prompt.systemPrompt match {
      case Some(system) =>
        Seq(
          Map("role" -> "system", "content" -> system),
          Map("role" -> "user", "content" -> prompt.text)
        )
      case None =>
        Seq(Map("role" -> "user", "content" -> prompt.text))
    }

    val requestBody = ujson.Obj(
      "model" -> model,
      "messages" -> messages,
      "temperature" -> 0.7
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
        val text = json("choices")(0)("message")("content").str
        val usage = json("usage")

        LLMResponse(
          text = text,
          tokenUsage = TokenUsage(
            promptTokens = usage("prompt_tokens").num.toInt,
            completionTokens = usage("completion_tokens").num.toInt,
            totalTokens = usage("total_tokens").num.toInt
          )
        )
      case Left(error) =>
        throw new RuntimeException(s"Error calling OpenAi API: $error")
    }
  }

  override def streamComplete(prompt: Prompt): fs2.Stream[IO, String] = {
    // For simplicity, we'll implement this as a non-streaming version that returns the full response
    // In a real implementation, you would use the streaming API
    fs2.Stream.eval(complete(prompt)).flatMap { response =>
      fs2.Stream.emits(response.text.toCharArray.map(_.toString))
    }
  }
}

object OpenAiClient:

  /**
   * Create an OpenAi LLM client
   *
   * @param apiKey The OpenAi API key
   * @param model  The model to use (default: gpt-3.5-turbo)
   * @return An LLM client for OpenAi
   */
  def apply(apiKey: String, model: String = "gpt-3.5-turbo"): OpenAiClient =
    new OpenAiClient(apiKey, model)
