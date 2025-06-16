package ai.guidoch.llm

import ai.guidoch.llm.model.{Document, Embedding}
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.github.jelmerk.knn.DistanceFunctions

import java.lang
import scala.jdk.CollectionConverters.ListHasAsScala

/** Interface for vector stores that can store and retrieve embeddings
  */
trait VectorStore {

  /** Add a document to the vector store
    * @param document
    *   The document to add
    * @return
    *   An IO containing the document ID
    */
  def addDocument(document: Document): IO[String]

  /** Add multiple documents to the vector store
    * @param documents
    *   The documents to add
    * @return
    *   An IO containing the document IDs
    */
  def addDocuments(documents: Seq[Document]): IO[Seq[String]]

  /** Search for similar documents
    * @param query
    *   The query embedding
    * @param limit
    *   The maximum number of results to return
    * @return
    *   An IO containing the search results
    */
  def search(query: Embedding, limit: Int = 5): IO[Seq[(Document, Float)]]

  /** Search for similar documents using a text query
    * @param queryText
    *   The query text
    * @param limit
    *   The maximum number of results to return
    * @return
    *   An IO containing the search results
    */
  def searchByText(
      queryText: String,
      limit: Int = 5
  ): IO[Seq[(Document, Float)]]
}

/** Factory object for creating vector stores
  */
object VectorStore {

  /** Create an in-memory vector store using HNSW
    * @param embeddingModel
    *   The embedding model to use for text queries
    * @param dimensions
    *   The dimensions of the embeddings
    * @return
    *   A vector store
    */
  def inMemory(
      embeddingModel: EmbeddingModel,
      dimensions: Int = 1536
  ): VectorStore =
    new InMemoryVectorStore(embeddingModel, dimensions)

  /** Implementation of VectorStore using HNSW in memory
    */
  private class InMemoryVectorStore(
      embeddingModel: EmbeddingModel,
      dimensions: Int
  ) extends VectorStore {
    import com.github.jelmerk.knn.hnsw.*
    import com.github.jelmerk.knn.scalalike.*

    import java.util.UUID

    // Item class for the HNSW index
    private case class IndexItem(id: String, vector: Array[Float], document: Document, dimensions: Int)
      extends Item[String, Array[Float]] {
    }

    // Create the HNSW index
    private val index: HnswIndex[String, Array[Float], IndexItem, lang.Float] =
      HnswIndex
        .newBuilder(dimensions, DistanceFunctions.FLOAT_COSINE_DISTANCE, 1000)
        .build[String, IndexItem]()

    // Map to store documents by ID
    private val documents = scala.collection.mutable.Map[String, Document]()

    override def addDocument(document: Document): IO[String] = for {
      // Generate an ID if not provided
      id <- IO.pure {
        if document.id.nonEmpty then document.id
        else UUID.randomUUID().toString
      }

      // Get or generate embedding
      docWithEmbedding <- document.embedding match {
        case Some(_) => IO.pure(document.copy(id = id))
        case None =>
          embeddingModel.embed(document.content).map(embedding => document.copy(id = id, embedding = Some(embedding)))
      }

      _ = {
        // Add to index
        val item = IndexItem(
          id = id,
          vector = docWithEmbedding.embedding.get.vector,
          document = docWithEmbedding,
          dimensions = dimensions
        )
        index.add(item)
      }

      // Store in map
      _ = documents(id) = docWithEmbedding

    } yield id

    override def addDocuments(documents: Seq[Document]): IO[Seq[String]] = IO {
      documents.map(doc => addDocument(doc).unsafeRunSync()(using IORuntime.global))
    }

    override def search(
        query: Embedding,
        limit: Int
    ): IO[Seq[(Document, Float)]] = IO {
      // Create a dummy item for the query
      val queryItem = IndexItem(
        id = "query",
        vector = query.vector,
        document = Document(id = "query", content = query.text, embedding = Some(query)),
        dimensions = dimensions
      )

      // Search the index
      val results = index.findNearest(queryItem.vector, limit)

      // Convert to (Document, Float) pairs
      results.asScala.map { case SearchResult(item, distance) =>
        (item.document, 1f - distance) // Convert distance to similarity
      }.toSeq
    }

    override def searchByText(
        queryText: String,
        limit: Int
    ): IO[Seq[(Document, Float)]] =
      for {
        embedding <- embeddingModel.embed(queryText)
        results <- search(embedding, limit)
      } yield results
  }
}
