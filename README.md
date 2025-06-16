# Scala LLM Integration

A Scala library for integrating and composing Large Language Models (LLMs), embedding stores, and Retrieval-Augmented Generation (RAG) systems.

## Features

- **LLM Integration**: Connect to OpenAi's GPT models for text generation
- **Embedding Generation**: Create vector embeddings from text using OpenAi's embedding models
- **Vector Storage**: Store and retrieve embeddings using an in-memory HNSW index
- **RAG System**: Combine LLMs with vector stores for context-aware responses
- **Composable Components**: Easily compose different components to create custom AI systems

## Requirements

- Scala 3.7.1
- Mill build tool
- OpenAi API key

## Getting Started

### Setting up the project

1. Clone the repository
2. Set your OpenAi API key as an environment variable:
   ```
   export OpenAi_API_KEY=your-api-key
   ```
3. Build the project:
   ```
   ./mill Common.compile
   ```

### Running the example

```
./mill Common.run
```

This will run the example application that demonstrates how to use the RAG system.

### Running the tests

```
./mill Common.test
```

## Usage

### Creating a RAG System

```scala
import ai.guidoch.llm._

// Create a RAG system with default settings
val ragSystem = LLMComposer.createRAGSystem(
  OpenAiApiKey = "your-api-key",
  llmModel = "gpt-3.5-turbo",
  embeddingModel = "text-embedding-ada-002",
  maxContextDocs = 3
)

// Add documents to the knowledge base
val documents = Seq(
  "Scala is a programming language that combines object-oriented and functional programming.",
  "Retrieval-Augmented Generation (RAG) enhances LLMs with external knowledge."
)
LLMComposer.addTextsToRAG(ragSystem, documents).unsafeRunSync()

// Query the RAG system
val answer = ragSystem.query("What is Scala?").unsafeRunSync()
println(answer)
```

### Creating Custom Components

You can also create and compose the individual components:

```scala
import ai.guidoch.llm._

// Create an LLM client
val llmClient = OpenAiClient("your-api-key", "gpt-3.5-turbo")

// Create an embedding model
val embeddingModel = OpenAiEmbeddingModel("your-api-key", "text-embedding-ada-002")

// Create a vector store
val vectorStore = VectorStore.inMemory(embeddingModel)

// Create a RAG system
val ragSystem = RAGSystem.basic(llmClient, vectorStore, maxContextDocs = 3)
```

## Architecture

The library consists of the following main components:

1. **LLMClient**: Interface for interacting with Large Language Models
2. **EmbeddingModel**: Interface for generating text embeddings
3. **VectorStore**: Interface for storing and retrieving embeddings
4. **RAGSystem**: Combines LLMs with vector stores for context-aware responses
5. **LLMComposer**: Utility for composing the different components

## License

MIT
