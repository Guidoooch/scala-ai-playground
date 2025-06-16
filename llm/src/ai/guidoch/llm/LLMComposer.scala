package ai.guidoch.llm

import ai.guidoch.llm.model.Document
import cats.effect.IO
import cats.implicits.catsSyntaxFlatMapOps

/** Utility for composing LLM systems
  */
object LLMComposer {

  /** Add a list of texts to a RAG system
    * @param ragSystem
    *   The RAG system to add to
    * @param texts
    *   The texts to add
    * @return
    *   An IO containing the document IDs
    */
  def addTextsToRAG(
      ragSystem: RAGSystem,
      texts: Seq[String]
  ): IO[Seq[String]] = {
    val documents = texts.map(text => Document(id = "", content = text))
    ragSystem.addDocuments(documents)
  }

  /** Helper method to query a RAG system and print the result
    *
    * @param ragSystem
    *   The RAG system
    * @param question
    *   The question to ask
    */
  def queryAndPrint(ragSystem: RAGSystem, question: String): IO[Unit] =
    IO.println(s"===================") >>
    IO.println(s"Question: $question") >>
    IO.println("Thinking...") >>
    ragSystem.query(question).>>=(answer => IO.println(s"Answer: $answer\n"))

  /** Helper method to stream a response from a RAG system and print it
    *
    * @param ragSystem
    *   The RAG system
    * @param question
    *   The question to ask
    */
  def streamQueryAndPrint(ragSystem: RAGSystem, question: String): IO[Unit] = {
    IO.println(s"Question: $question") >>
    IO.println("Answer: ") >>
    // Stream the response and print each token
    ragSystem
      .streamQuery(question)
      .evalTap { token => IO.print(token) }
      .compile
      .drain >>
    IO.println("")
  }
}
