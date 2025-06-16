package ai.guidoch.llm.model

/**
 * Represents a text embedding (vector representation of text)
 * @param vector The embedding vector
 * @param text The original text that was embedded
 */
case class Embedding(vector: Array[Float], text: String) {
  // Calculate cosine similarity between this embedding and another
  def cosineSimilarity(other: Embedding): Float = {
    val dotProduct = (this.vector zip other.vector).map { case (a, b) => a * b }.sum
    val magnitudeA = math.sqrt(this.vector.map(x => x * x).sum).toFloat
    val magnitudeB = math.sqrt(other.vector.map(x => x * x).sum).toFloat
    dotProduct / (magnitudeA * magnitudeB)
  }
}