package ai.guidoch.llm

import ai.guidoch.llm.google.GeminiRAGSystem
import cats.effect.{ExitCode, IO, IOApp}

/** Main application to demonstrate the usage of the LLM integration system
  */
object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    // Check if API key is provided
    val apiKey = sys.env.getOrElse("GOOGLE_API_KEY", "")

    if (apiKey.isEmpty) {
      IO.println("Error: OPENAI_API_KEY environment variable is not set.") >>
      IO.println("Please set it to your OpenAI API key and try again.") >>
      IO.pure(ExitCode.Error)
    } else {
      // Create the RAG system
      val llmModel  = "gemini-2.5-pro-preview-06-05"
      val ragSystem = GeminiRAGSystem.create(apiKey, llmModel)

      // Sample documents to add to the knowledge base
      val documents = Seq(
        "Scala is a strong statically typed general-purpose programming language which supports both object-oriented programming and functional programming. Designed to be concise, many of Scala's design decisions are aimed to address criticisms of Java.",
        "Scala runs on the Java Virtual Machine (JVM) and is compatible with existing Java programs. It also compiles to JavaScript, allowing Scala code to run in the browser.",
        "Scala was created by Martin Odersky and released in 2004. The name Scala is a portmanteau of 'scalable' and 'language', signifying that it is designed to grow with the demands of its users.",
        "Retrieval-Augmented Generation (RAG) is a technique that enhances large language models by allowing them to access external knowledge not included in their training data. RAG combines information retrieval with text generation to produce more accurate and up-to-date responses.",
        "Vector databases store and index vector embeddings, which are numerical representations of data like text, images, or audio. They enable efficient similarity search, which is crucial for applications like semantic search, recommendation systems, and RAG."
      )

      // Add the documents to the RAG system
      for {
        _ <- IO.println("Initializing RAG system...")
        _ <- LLMComposer.addTextsToRAG(ragSystem, documents)
        _ <- IO.println("Documents added to the knowledge base.")

        // Example queries
        _ <- // Query about Scala
          LLMComposer.queryAndPrint(
            ragSystem,
            "What is Scala and who created it?"
          )

        _ <- // Query about RAG
          LLMComposer.queryAndPrint(
            ragSystem,
            "Explain what RAG is and how it works."
          )

        _ <- // Query that requires combining information
          LLMComposer.queryAndPrint(
            ragSystem,
            "How could Scala be used to implement a RAG system?"
          )

        _ <- // Query outside the knowledge base
          LLMComposer.queryAndPrint(ragSystem, "What is the capital of France?")

      } yield ExitCode.Success
    }
  }
}
