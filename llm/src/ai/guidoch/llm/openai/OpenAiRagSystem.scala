package ai.guidoch.llm.openai

import ai.guidoch.llm.{RAGSystem, VectorStore}

object OpenAiRagSystem:

  /** Create a RAG system using OpenAI
    *
    * @param apiKey
    *   The OpenAI API key
    * @param llmModel
    *   The LLM model to use (default: gpt-3.5-turbo)
    * @param embeddingModel
    *   The embedding model to use (default: text-embedding-ada-002)
    * @return
    *   A RAG system
    */
  def create(
      apiKey: String,
      llmModel: String = "gpt-3.5-turbo",
      embeddingModel: String = "text-embedding-ada-002"
  ): RAGSystem =
    val llmClient   = OpenAiClient(apiKey, llmModel)
    val embModel    = OpenAiEmbeddingModel(apiKey, embeddingModel)
    val vectorStore = VectorStore.inMemory(embModel)

    RAGSystem.basic(llmClient, vectorStore)
