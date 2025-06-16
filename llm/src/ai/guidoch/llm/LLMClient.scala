package ai.guidoch.llm

import ai.guidoch.llm.model.{LLMResponse, Prompt}
import cats.effect.IO
import sttp.model.Uri

/**
 * Interface for interacting with Large Language Models
 */
trait LLMClient {
  /**
   * Send a prompt to the LLM and get a response
   * @param prompt The prompt to send
   * @return An IO containing the LLM response
   */
  def complete(prompt: Prompt): IO[LLMResponse]

  /**
   * Stream a response from the LLM token by token
   * @param prompt The prompt to send
   * @return An IO stream of response tokens
   */
  def streamComplete(prompt: Prompt): fs2.Stream[IO, String]
}
