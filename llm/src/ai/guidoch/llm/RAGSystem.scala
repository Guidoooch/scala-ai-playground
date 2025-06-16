package ai.guidoch.llm

import ai.guidoch.llm.model.{Document, Prompt}
import cats.effect.IO

/**
 * Retrieval-Augmented Generation (RAG) system
 * Combines an LLM with a vector store to provide context-aware responses
 */
trait RAGSystem {
  /**
   * Add a document to the knowledge base
   * @param document The document to add
   * @return An IO containing the document ID
   */
  def addDocument(document: Document): IO[String]
  
  /**
   * Add multiple documents to the knowledge base
   * @param documents The documents to add
   * @return An IO containing the document IDs
   */
  def addDocuments(documents: Seq[Document]): IO[Seq[String]]
  
  /**
   * Query the RAG system with a question
   * @param question The question to ask
   * @return An IO containing the answer
   */
  def query(question: String): IO[String]
  
  /**
   * Stream a response from the RAG system
   * @param question The question to ask
   * @return An IO stream of response tokens
   */
  def streamQuery(question: String): fs2.Stream[IO, String]
}

/**
 * Factory object for creating RAG systems
 */
object RAGSystem {
  /**
   * Create a basic RAG system
   * @param llmClient The LLM client to use
   * @param vectorStore The vector store to use
   * @param maxContextDocs The maximum number of context documents to include
   * @return A RAG system
   */
  def basic(
    llmClient: LLMClient, 
    vectorStore: VectorStore, 
    maxContextDocs: Int = 3
  ): RAGSystem = new BasicRAGSystem(llmClient, vectorStore, maxContextDocs)
  
  /**
   * Implementation of a basic RAG system
   */
  private class BasicRAGSystem(
    llmClient: LLMClient, 
    vectorStore: VectorStore, 
    maxContextDocs: Int
  ) extends RAGSystem {
    
    override def addDocument(document: Document): IO[String] = 
      vectorStore.addDocument(document)
    
    override def addDocuments(documents: Seq[Document]): IO[Seq[String]] = 
      vectorStore.addDocuments(documents)
    
    override def query(question: String): IO[String] = {
      for {
        // Retrieve relevant documents
        relevantDocs <- vectorStore.searchByText(question, maxContextDocs)
        
        // Format the context
        context = formatContext(relevantDocs.map(_._1))
        
        // Create the prompt with context
        prompt = createPromptWithContext(question, context)

        // Get the response from the LLM
        response <- llmClient.complete(prompt)
      } yield response.text
    }
    
    override def streamQuery(question: String): fs2.Stream[IO, String] = {
      // First retrieve the relevant documents
      fs2.Stream.eval(vectorStore.searchByText(question, maxContextDocs))
        .flatMap { relevantDocs =>
          // Format the context
          val context = formatContext(relevantDocs.map(_._1))
          
          // Create the prompt with context
          val prompt = createPromptWithContext(question, context)
          
          // Stream the response from the LLM
          llmClient.streamComplete(prompt)
        }
    }
    
    /**
     * Format the context documents into a string
     */
    private def formatContext(documents: Seq[Document]): String = {
      documents.zipWithIndex.map { case (doc, i) =>
        s"""Document ${i + 1}:
           |${doc.content}
           |""".stripMargin
      }.mkString("\n")
    }
    
    /**
     * Create a prompt with context for the LLM
     */
    private def createPromptWithContext(question: String, context: String): Prompt = {
      val systemPrompt = 
        """You are a helpful assistant that answers questions based on the provided context.
          |If the answer cannot be found in the context, say "I don't know" instead of making up an answer.
          |""".stripMargin
      
      val promptText = 
        s"""Context:
           |$context
           |
           |Question: $question
           |
           |Answer:""".stripMargin
      
      Prompt(text = promptText, systemPrompt = Some(systemPrompt))
    }
  }
}
