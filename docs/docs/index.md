# AI Ghost

**AI Ghost writes your book for you.** At its core sits an AI agent - the *AI Ghost Writer* - that
does the actual writing. You give it the idea, the direction and the feedback; it produces the prose.

The book is not generated in one shot. The AI Ghost Writer works **chapter by chapter**: you tell it
what the next chapter should contain, it writes it in full, and you keep, refine or let it rewrite the
chapter before the next one begins.

## The AI Ghost Writer

* **Writes the chapters.** Prose, dialogue and scenes are produced by the agent, not by a template.
* **Knows the book so far.** Outline, previous chapters, characters, places and tone are handed to the
  agent as context, so chapter twelve still matches chapter one.
* **Takes direction.** A short instruction per chapter is enough - "bring the sister back, end on the
  storm" - and the agent turns it into finished text.
* **Rewrites on demand.** Not happy with a chapter? Send it back with a note instead of starting over.
* **Leaves you the last word.** Nothing is final until you accept it; every chapter stays editable.

## Around the agent

The book project holds the outline, all chapters and the context material the agent relies on. The
application itself is a JavaFX desktop UI written in Kotlin, shipped as a self-contained ZIP archive
that carries the start scripts and every required JAR.

## Where to go next

* [API Docs](dokka/html/index.html) - the generated Dokka API documentation
* [Licences](licences/index.html) - the dependency licence report
