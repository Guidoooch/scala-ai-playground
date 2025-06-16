package ai.guidoch.llm.model

/** Represents a document with its embedding for retrieval
  * @param id
  *   Unique identifier for the document
  * @param content
  *   The document content
  * @param embedding
  *   Vector representation of the document
  * @param metadata
  *   Optional metadata about the document
  */
case class Document(
    id: String,
    content: String,
    embedding: Option[Embedding] = None,
    metadata: Map[String, String] = Map.empty
)
