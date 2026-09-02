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
beyond a JDK is needed. The persisted data - the preferences and the book project with its chapters -
lives in the library `lib/ai-ghost-model`. The preferences are one YAML document; a book project is
an archive holding one `.json` entry per project part. The three parts every project carries - its
meta data, its design and the manuscript - are fields of the project. A part this version cannot read
is kept as the text it was stored as and written back unchanged, so a document written by a newer
version or by a plugin loses nothing here. The meta data names every part stored beside the three
standard ones, so a document that lost an entry is noticed at all. A document missing one of the
three standard parts is reported as corrupt instead of being opened with defaults in its place; a
document that only lost a part beyond them is offered for rescue, naming what would be lost, and is
opened once the user accepts it. Both documents are written with Jackson.

### State of implementation

| Feature                                           | State       |
|---------------------------------------------------|-------------|
| AI Ghost Writer: chapter generation               | Planned     |
| AI Ghost Writer: rewrite of a chapter on feedback | Planned     |
| Context handling (outline, characters, tone)      | Planned     |
| Book project with outline and chapters            | Planned     |
| Export of the finished manuscript                 | Planned     |
| Plugin API (`lib/plugin/ai-ghost-plugin-api`)     | Planned     |
| JavaFX desktop shell (`app/ai-ghost-ui`)          | Implemented |
| JSON data model (`lib/ai-ghost-model`)            | Implemented |
| AI support library (`lib/ai-ghost-ai`)            | Implemented |
| Layout core: line breaking and alignment (`lib/ai-ghost-layouting`) | Implemented |
| Layout blocks from book, design and meta data (`lib/ai-ghost-layouting-model`) | Implemented |
| JavaFX renderer of the layout core (`lib/ai-ghost-layouting-fx`) | Planned     |
| Font identity of a project and report of a substitution | Implemented |
| Prompt input with character limit and token estimate | Implemented |
| ZIP distribution with start scripts and `libs`    | Implemented |
| MVVM UI architecture (MVVM FX)                    | Implemented |
| Internationalisation of the UI (English, German)  | Implemented |
| Menu bar with icons and keyboard shortcuts        | Implemented |
| Editor and preview tabs of the main window        | Implemented |
| Project tree with prolog, chapters, epilog, blurb | Implemented |
| Editor split into project tree and editing area   | Implemented |
| Inspector with collapsible Book and Chapter sections | Implemented |
| Project settings dialog: page format and blank pages (Design section) | Implemented |
| Project settings: typography of the book elements | Planned     |
| Undo/Redo of project changes, with named history dropdown | Implemented |
| Product design shared with logo and documentation | Implemented |
| Light and dark appearance, chosen in the preferences | Implemented |
| Shipped `Ghost Writer` type face                  | Implemented |
| Logging to console and to a rolled over log file  | Implemented |
| Splash screen with a background area for startup jobs | Implemented |
| Dokka API documentation                           | Implemented |
| Dependency licence report                         | Implemented |
| MkDocs documentation site (versioned via mike)    | Implemented |

The user interface carries its own type face, `Ghost Writer`: a geometric, monolinear sans with
rounded terminals drawn after the `AI` lettering of the logo. Its outlines are generated by the
vector generator in `tools/font` and the finished TrueType file is committed, so the application
looks the same on every platform and builds without a Python installation.

## Requirements

* JDK 25 - the Gradle toolchain resolves one automatically via the foojay resolver
* Python 3 - only needed for the documentation tasks and to regenerate the shipped type face
  (`pip install -r tools/font/requirements.txt`, then `./gradlew :app:ai-ghost-ui:generateFont`)

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

## AI transparency notice

Parts of the source code, the tests and the documentation of this project were generated with the
help of AI coding assistants. Every generated contribution is reviewed, adapted and accepted by a
human maintainer before it enters the repository; the maintainers remain responsible for the
published code.

This notice is published in the spirit of the transparency requirements of the European Union's
Artificial Intelligence Act (Regulation (EU) 2024/1689), so that users and contributors know how the
project is produced.

## Licence

See [LICENSE](LICENSE).
