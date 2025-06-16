package ai.guidoch.llm.model

/**
 * Information about token usage in an LLM interaction
 * @param promptTokens Number of tokens in the prompt
 * @param completionTokens Number of tokens in the completion
 * @param totalTokens Total number of tokens used
 */
case class TokenUsage(promptTokens: Int, completionTokens: Int, totalTokens: Int)

object TokenUsage {
  
  def guessTokenUsage(prompt: String, systemPrompt: String, text: String): TokenUsage =
    // Gemini API doesn't provide token usage information in the same way as OpenAi
    // We'll estimate token usage based on a simple heuristic (4 characters ~ 1 token)
    val promptTokens = (prompt.length + systemPrompt.length) / 4
    val completionTokens = text.length / 4
    
    TokenUsage(
      promptTokens = promptTokens,
      completionTokens = completionTokens,
      totalTokens = promptTokens + completionTokens
    )
}
