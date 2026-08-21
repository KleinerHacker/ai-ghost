# AI Ghost

<p align="center">
  <img src="docs/docs/assets/images/logo.png" alt="AI Ghost logo" width="320">
</p>

**AI Ghost writes your book for you.** At its core sits an AI agent - the *AI Ghost Writer* - that
does the actual writing. You give it the idea, the direction and the feedback; it produces the prose.

The book is not generated in one shot. The AI Ghost Writer works **chapter by chapter**: you tell it
what the next chapter should contain, it writes it in full, and you keep, refine or let it rewrite the
chapter before the next one begins.

## The AI Ghost Writer

* **Writes the chapters.** Prose, dialogue and scenes are produced by the agent, not by a template.
* **Knows the book so far.** Outline, previous chapters, characters, places and tone are handed to the
  agent as context, so chapter twelve still matches chapter one.
* **Takes direction.** A short instruction per chapter is enough and the agent turns it into finished
  text.
* **Rewrites on demand.** Not happy with a chapter? Send it back with a note instead of starting over.
* **Leaves you the last word.** Nothing is final until you accept it; every chapter stays editable.

## Around the agent

The book project holds the outline, all chapters and the context material the agent relies on. The
application itself is a JavaFX desktop UI written in Kotlin (`app/ai-ghost-ui`), distributed as a
self-contained ZIP archive carrying the start scripts and every required JAR, so no installation
beyond a JDK is needed.

### State of implementation

| Feature                                           | State       |
|---------------------------------------------------|-------------|
| AI Ghost Writer: chapter generation               | Planned     |
| AI Ghost Writer: rewrite of a chapter on feedback | Planned     |
| Context handling (outline, characters, tone)      | Planned     |
| Book project with outline and chapters            | Planned     |
| Export of the finished manuscript                 | Planned     |
| JavaFX desktop shell (`app/ai-ghost-ui`)          | Implemented |
| ZIP distribution with start scripts and `libs`    | Implemented |
| MVVM UI architecture (MVVM FX)                    | Implemented |
| Internationalisation of the UI (English, German)  | Implemented |
| Menu bar with icons and keyboard shortcuts        | Implemented |
| Dokka API documentation                           | Implemented |
| Dependency licence report                         | Implemented |
| MkDocs documentation site (versioned via mike)    | Implemented |

## Requirements

* JDK 25 - the Gradle toolchain resolves one automatically via the foojay resolver
* Python 3 - only needed for the documentation tasks

## Check out and build

```bash
git clone https://github.com/KleinerHacker/ai-ghost.git
cd ai-ghost
./gradlew build
```

## Run

```bash
./gradlew :app:ai-ghost-ui:run
```

## Consuming the artifacts

The release artifacts are attached to every
[GitHub Release](https://github.com/KleinerHacker/ai-ghost/releases), one ZIP per platform:
`ghost-ui-<version>-<platform>.zip`.

Build the same archive locally with:

```bash
./gradlew packageDist
```

It lands in `app/ui/build/distributions` and contains:

```text
ghost-ui.bat
ghost-ui.sh
libs/
```

`libs` holds the application JAR and every dependency. Unpack the archive anywhere and start the
application with `ghost-ui.sh` on Linux and macOS or `ghost-ui.bat` on Windows.

## Documentation

* [MkDocs documentation](https://kleinerhacker.github.io/ai-ghost/latest/) - the published site on
  GitHub Pages
* [API documentation](https://kleinerhacker.github.io/ai-ghost/latest/dokka/html/index.html) - generated
  with Dokka
* [Licence report](https://kleinerhacker.github.io/ai-ghost/latest/licences/index.html) - the
  dependency licences

Build the documentation locally:

```bash
./gradlew runDocs     # serve the documentation
./gradlew buildDocs   # build it strictly, acts as a generation test
```

## Licence

See [LICENSE](LICENSE).
