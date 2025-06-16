package ai.guidoch.llm.model

case class Prompt(text: String, systemPrompt: Option[String] = None)
