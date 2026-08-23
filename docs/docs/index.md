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

## Look and feel

The application ships one global light design: indigo accents and deep navy text on softly indigo
tinted paper surfaces, with rounded controls and gentle shadows - the same colours and shapes as the
AI Ghost logo and this documentation site. Nothing needs to be configured, the design is applied to
every window.

## AI transparency notice

!!! note "Parts of this software were created with AI"

    Parts of the source code, the tests and this documentation were generated with the help of AI
    coding assistants. Every generated contribution is reviewed, adapted and accepted by a human
    maintainer before it is released; the maintainers remain responsible for the published software.

    This notice is published in the spirit of the transparency requirements of the European Union's
    Artificial Intelligence Act (Regulation (EU) 2024/1689).

## Where to go next

* [Menu and shortcuts](menu.md) - every menu entry and its keyboard shortcut
* [API Docs](dokka/html/index.html) - the generated Dokka API documentation
* [Licences](licences/index.html) - the dependency licence report
