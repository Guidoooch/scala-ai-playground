package ai.guidoch.llm.google

import ai.guidoch.llm.{RAGSystem, VectorStore}

object GeminiRAGSystem:

  /** Create a RAG system using Google Gemini
    *
    * @param apiKey
    *   The Google API key
    * @param llmModel
    *   The LLM model to use (default: gemini-2.0-flash)
    * @param embeddingModel
    *   The embedding model to use (default: embedding-001)
    * @return
    *   The RAG system
    */
  def create(
      apiKey: String,
      llmModel: String = "gemini-2.0-flash",
      embeddingModel: String = "embedding-001"
  ): RAGSystem =
    val llmClient   = new GeminiJavaClientWrapper(apiKey, llmModel)
    val embModel    = GeminiEmbeddingModel(apiKey, embeddingModel)
    val vectorStore = VectorStore.inMemory(embModel)

    RAGSystem.basic(llmClient, vectorStore)
