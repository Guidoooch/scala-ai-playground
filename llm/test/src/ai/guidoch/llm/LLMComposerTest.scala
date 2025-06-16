package ai.guidoch.llm

import ai.guidoch.llm.google.GeminiRAGSystem
import ai.guidoch.llm.openai.OpenAiRagSystem
import cats.effect.unsafe.implicits.global
import utest.*

object LLMComposerTest extends TestSuite {
  val tests: Tests = Tests {
    test("LLMComposer should create RAG systems") {
      // Test OpenAi RAG system creation
      val OpenAiKey = sys.env.getOrElse("OpenAi_API_KEY", "")
      if (OpenAiKey.nonEmpty) {
        val OpenAiRag = OpenAiRagSystem.create(OpenAiKey)
        assert(OpenAiRag != null)
      } else {
        println("Skipping OpenAi RAG test that requires API key")
      }

      // Test Gemini RAG system creation
      val googleKey = sys.env.getOrElse("GOOGLE_API_KEY", "")
      if (googleKey.nonEmpty) {
        val geminiRag = GeminiRAGSystem.create(googleKey)
        assert(geminiRag != null)
      } else {
        println("Skipping Gemini RAG test that requires API key")
      }
    }

    test("LLMComposer should add texts to RAG systems") {
      // Create a mock RAG system
      val mockClient = new RAGSystemTest.MockLLMClient()
      val mockEmbedding = new RAGSystemTest.MockEmbeddingModel()
      val vectorStore = VectorStore.inMemory(mockEmbedding, dimensions = 10)
      val ragSystem = RAGSystem.basic(mockClient, vectorStore)

      // Add documents
      val documents = Seq(
        "Scala is a programming language.",
        "Gemini is a large language model developed by Google."
      )

      val docIds = LLMComposer.addTextsToRAG(ragSystem, documents).unsafeRunSync()

      // Verify documents were added
      assert(docIds.length == 2)
    }
  }
}
