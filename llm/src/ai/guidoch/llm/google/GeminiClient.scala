package ai.guidoch.llm.google

import ai.guidoch.llm.LLMClient
import ai.guidoch.llm.model.{LLMResponse, Prompt, TokenUsage}
import cats.effect.IO
import sttp.client4.*
import sttp.client4.httpclient.HttpClientSyncBackend
import upickle.default.*

/**
 * Implementation of LLMClient for Google's Gemini models
 */
class GeminiClient(apiKey: String, model: String) extends LLMClient {

  private val backend = HttpClientSyncBackend()
  private val baseUrl = uri"https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

  override def complete(prompt: Prompt): IO[LLMResponse] = IO {
    // Prepare the request body
    val requestBody = prompt.systemPrompt match {
      case Some(systemPrompt) =>
        ujson.Obj(
          "contents" -> ujson.Arr(
            ujson.Obj(
              "role" -> "user",
              "parts" -> ujson.Arr(
                ujson.Obj("text" -> systemPrompt)
              )
            ),
            ujson.Obj(
              "role" -> "user",
              "parts" -> ujson.Arr(
                ujson.Obj("text" -> prompt.text)
              )
            )
          ),
          "generationConfig" -> ujson.Obj(
            "temperature" -> 0.7,
            "topP" -> 0.95,
            "topK" -> 40
          )
        )
      case None =>
        ujson.Obj(
          "contents" -> ujson.Arr(
            ujson.Obj(
              "role" -> "user",
              "parts" -> ujson.Arr(
                ujson.Obj("text" -> prompt.text)
              )
            )
          ),
          "generationConfig" -> ujson.Obj(
            "temperature" -> 0.7,
            "topP" -> 0.95,
            "topK" -> 40
          )
        )
    }

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

        // Extract the generated text from the response
        val text = json("candidates")(0)("content")("parts")(0)("text").str

        val usage = TokenUsage.guessTokenUsage(prompt.text, prompt.systemPrompt.getOrElse(""), text)

        LLMResponse(
          text = text,
          tokenUsage = usage
        )
      case Left(error) =>
        throw new RuntimeException(s"Error calling Gemini API: $error")
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

object GeminiClient {
  /**
   * Create a Google Gemini LLM client
   *
   * @param apiKey The Google API key
   * @param model The model to use
   * @return A LLM client for Google Gemini
   */
  def apply(apiKey: String, model: String): GeminiClient =
    new GeminiClient(apiKey, model)
}
