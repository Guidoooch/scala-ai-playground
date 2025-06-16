package ai.guidoch.llm

import ai.guidoch.llm.google.GeminiRAGSystem
import model.{Embedding, LLMResponse, Prompt, TokenUsage}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import utest.*

/**
 * Tests for the RAG system
 */
object RAGSystemTest extends TestSuite {

  // Mock implementation of LLMClient for testing
  class MockLLMClient extends LLMClient {

    override def complete(prompt: Prompt): IO[LLMResponse] = IO {
      // Extract the question from the prompt
      val questionPattern = "Question:\\s+(.*?)\\s*Answer:".r
      val questionOpt = questionPattern.findFirstMatchIn(prompt.text).map(_.group(1))
      val question = questionOpt.getOrElse(prompt.text) // Use the entire prompt if question extraction fails

      // Simple mock that returns a response based on the question
      val response =
        if question.contains("RAG") then
          "RAG stands for Retrieval-Augmented Generation."
        else if (question.contains("Scala"))
          "Scala is a programming language created by Martin Odersky."
        else
          "I don't know the answer to that question."

      LLMResponse(
        text = response,
        tokenUsage = TokenUsage(promptTokens = 10, completionTokens = 10, totalTokens = 20)
      )
    }

    override def streamComplete(prompt: Prompt): fs2.Stream[IO, String] = {
      fs2.Stream.eval(complete(prompt)).flatMap { response =>
        fs2.Stream.emits(response.text.toCharArray.map(_.toString))
      }
    }
  }

  // Mock implementation of EmbeddingModel for testing
  class MockEmbeddingModel extends EmbeddingModel {
    // Map to store consistent embeddings for the same text
    private val embeddingCache = scala.collection.mutable.Map[String, Array[Float]]()

    override def embed(text: String): IO[Embedding] = IO {
      // Get or create a consistent embedding for this text
      val vector = embeddingCache.getOrElseUpdate(text, {
        // For "RAG" related queries, use a specific pattern
        if (text.toLowerCase.contains("rag")) {
          Array(0.9f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f)
        } 
        // For "Scala" related queries, use a different pattern
        else if (text.toLowerCase.contains("scala")) {
          Array(0.1f, 0.9f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f)
        } 
        // For other queries, use a random pattern
        else {
          Array.fill(10)(scala.util.Random.nextFloat())
        }
      })

      Embedding(vector = vector, text = text)
    }

    override def embedBatch(texts: Seq[String]): IO[Seq[Embedding]] = IO {
      texts.map(text => embed(text).unsafeRunSync())
    }
  }

  val tests: Tests = Tests {
    test("RAG system should return relevant answers") {
      // Create mock components
      val llmClient = new MockLLMClient()
      val embeddingModel = new MockEmbeddingModel()
      val vectorStore = VectorStore.inMemory(embeddingModel, dimensions = 10)

      // Create the RAG system
      val ragSystem = RAGSystem.basic(llmClient, vectorStore)

      // Add documents
      val documents = Seq(
        "Scala is a programming language.",
        "RAG is a technique for enhancing LLMs."
      )

      LLMComposer.addTextsToRAG(ragSystem, documents).unsafeRunSync()

      // Test queries
      val scalaAnswer = ragSystem.query("Tell me about Scala").unsafeRunSync()
      val ragAnswer = ragSystem.query("What is RAG?").unsafeRunSync()
      val unknownAnswer = ragSystem.query("What is the capital of France?").unsafeRunSync()

      assert(scalaAnswer.contains("Scala"))
      assert(ragAnswer.contains("RAG"))
      assert(unknownAnswer.contains("don't know"))
    }

    test("LLMComposer should create a working RAG system") {
      // Skip this test if no API key is available
      val apiKey = sys.env.getOrElse("OpenAi_API_KEY", "")

      if (apiKey.isEmpty) {
        println("Skipping test that requires OpenAi API key")
      } else {
        // Create RAG system using LLMComposer
        val ragSystem = GeminiRAGSystem.create(apiKey)

        // Add a simple document
        val docIds = LLMComposer.addTextsToRAG(
          ragSystem, 
          Seq("This is a test document for the RAG system.")
        ).unsafeRunSync()

        // Verify document was added
        assert(docIds.length == 1)
      }
    }
  }
}
