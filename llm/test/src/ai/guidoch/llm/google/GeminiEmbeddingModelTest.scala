package ai.guidoch.llm.google

import cats.effect.unsafe.implicits.global
import utest.*

object GeminiEmbeddingModelTest extends TestSuite {
  val tests: Tests = Tests {
    test("Gemini embedding model should generate embeddings") {
      // Skip this test if no API key is available
      val apiKey = sys.env.getOrElse("GOOGLE_API_KEY", "")

      if (apiKey.isEmpty) {
        println("Skipping test that requires Google API key")
      } else {
        val model = GeminiEmbeddingModel(apiKey)

        // Generate embedding for a test text
        val text = "This is a test for the Gemini embedding model"
        val embedding = model.embed(text).unsafeRunSync()

        // Check that the embedding has a non-empty vector
        assert(embedding.vector.nonEmpty)

        // Check that the embedding contains the original text
        assert(embedding.text == text)

        // Test batch embedding
        val texts = Seq(
          "First test text", 
          "Second test text", 
          "Third test text"
        )

        val embeddings = model.embedBatch(texts).unsafeRunSync()

        // Check that we got the correct number of embeddings
        assert(embeddings.length == texts.length)

        // Check that each embedding has a non-empty vector
        assert(embeddings.forall(_.vector.nonEmpty))

        // Check that each embedding contains the correct original text
        assert(embeddings.map(_.text) == texts)
      }
    }
  }
}
