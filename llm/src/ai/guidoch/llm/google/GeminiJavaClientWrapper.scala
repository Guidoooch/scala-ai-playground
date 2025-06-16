package ai.guidoch.llm.google
import ai.guidoch.llm.LLMClient
import ai.guidoch.llm.model.{LLMResponse, Prompt, TokenUsage}
import cats.effect.IO
import com.google.genai.Client
import com.google.genai.types.GenerateContentResponse

import scala.jdk.FutureConverters.CompletionStageOps

class GeminiJavaClientWrapper(apiKey: String, model: String) extends LLMClient {

  override def complete(prompt: Prompt): IO[LLMResponse] =
    val client = Client.builder().apiKey(apiKey).build()
    IO.fromFuture(IO(client.async.models.generateContent(model, prompt.text, null).asScala))
      .map { response =>
        val text  = response.text()
        val usage = TokenUsage.guessTokenUsage(prompt.text, prompt.systemPrompt.getOrElse(""), text)
        LLMResponse(text, usage)
      }
    
  override def streamComplete(prompt: Prompt): fs2.Stream[IO, String] =
    fs2.Stream.eval(complete(prompt)).flatMap { response =>
      fs2.Stream.emits(response.text.toCharArray.map(_.toString))
    }
    
}
