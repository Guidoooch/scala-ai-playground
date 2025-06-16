package ai.guidoch.llm.model

/**
 * Represents a response from an LLM
 * @param text The generated text
 * @param tokenUsage Information about token usage
 */
case class LLMResponse(text: String, tokenUsage: TokenUsage)
