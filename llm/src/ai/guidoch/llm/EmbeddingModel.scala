package ai.guidoch.llm

import cats.effect.IO
import model.Embedding
/**
 * Interface for text embedding models
 */
trait EmbeddingModel {
  /**
   * Generate an embedding for a text
   * @param text The text to embed
   * @return An IO containing the embedding
   */
  def embed(text: String): IO[Embedding]
  
  /**
   * Generate embeddings for multiple texts
   * @param texts The texts to embed
   * @return An IO containing a list of embeddings
   */
  def embedBatch(texts: Seq[String]): IO[Seq[Embedding]]
}
