package ai.guidoch.llm.google

import ai.guidoch.llm.model.Prompt
import cats.effect.unsafe.implicits.global
import utest.*

object GeminiClientTest extends TestSuite {
  val tests: Tests = Tests {
    test("Gemini client should work with API key") {
      // Skip this test if no API key is available
      val apiKey = sys.env.getOrElse("GOOGLE_API_KEY", "")

      if (apiKey.isEmpty) {
        println("Skipping test that requires Google API key")
      } else {
        val client = GeminiClient(apiKey, "gemini-2.5-pro-preview-06-05")
        val prompt = Prompt(
          text = "What is Scala?",
          systemPrompt = Some("You are a helpful programming assistant who provides concise answers.")
        )

        val response = client.complete(prompt).unsafeRunSync()

        // Check that we got a non-empty response
        assert(response.text.nonEmpty)

        // Check that the response likely contains information about Scala
        assert(
          response.text.toLowerCase.contains("scala") ||
          response.text.toLowerCase.contains("programming") ||
          response.text.toLowerCase.contains("language")
        )
      }
    }
  }
}
