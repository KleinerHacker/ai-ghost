# Feature Plan: Paper Writing Surface

> Orientation only. Every task, constraint and test belongs to the implementation plans under
> `.claude/plans/implementation`; `FP-001-Overview.md` lists them, and section 7 names the file of
> each one.

## 1. Objective

Writing a book part feels like writing on the printed page: title, headings and paragraphs are edited
on a sheet that already carries the typography, margins and page structure of the finished book.

Two forces bound the metaphor: the appearance is owned by the project design, not by the writer, and
text is partly written by the AI, which works on a part, a heading or a paragraph rather than on a
caret position. The feature answers both by making the sheet the only writing surface, moving
everything that is not printed text into a context panel, and attaching the AI to the structural units
the model already knows.

What is shown while writing is what the preview shows: one layout engine decides every line break and
every page break, and both surfaces consume that one result. Engine and renderer are general purpose
libraries; `app/ui` is one consumer of them.

Export is not part of this feature. It is named only as a constraint: the layout result stays toolkit
independent so a later export - a plugin on Apache PDFBox - consumes the same page structure.

## 2. Current State

* `lib/ai-ghost-model`, `lib/ai-ghost-fx-model` - manuscript POJOs and their mirrored FX properties.
* `lib/ai-ghost-layouting` - the layout core, implemented (IP-03): styles, line breaking, placed
  lines, the `TextMetrics` interface and a deterministic implementation. No toolkit, no app.
* `lib/ai-ghost-layouting-model` - the bridge from `Book`, `Design` and `Meta` to the core,
  implemented (IP-03).
* `lib/ai-ghost-ai` - only `TokenUtils`; no generation pipeline.
* `app/ui` - the only JavaFX module, MVVM FX, jlink image. Carries the font work of IP-01
  (`FontCatalog`, `FontResolver`, `FontResolution`, `JavaFxTextMetrics`).
* `.claude/rules/architecture.md` allows JavaFX in `app/ui` only.

Missing: any editor for prolog, chapter, epilog and blurb; rendering, pagination and preview; a
JavaFX renderer and a module allowed to hold one; a design editor; undo/redo; the font fingerprint.

## 3. Target State

Three zones right of the project tree: the **paper** in the centre, a collapsible **inspector** at the
right carrying everything that is not printed text, and a **mode switch** between `Schreiben` and
`Vorschau` over the same content.

* **Fidelity chain.** One engine turns design plus text into placed lines in points; both surfaces
  paint those coordinates and decide nothing themselves. Measuring and drawing use the same JavaFX
  text stack, so no second font implementation can answer differently. The correspondence holds per
  machine; a project records the metrics it was written with, and a mismatch is reported.
* **Fonts.** No font file is read, parsed or shipped. Families come from `javafx.scene.text.Font`,
  measuring runs through a hidden `Text` node.
* **A reusable renderer.** `lib/layouting-fx` (`ai-ghost-layouting-fx`) is a JavaFX component library
  holding the measuring, the exact page view and the writable flow. It depends on
  `ai-ghost-layouting` and JavaFX and on nothing else, owns no document and applies no change.
  `app/ui` keeps everything that names the application: the `FontData` translation, the fingerprint,
  the binding of book and design, undo, inspector and AI.
* **Front matter.** Title page with title, further title lines and author, followed directly by the
  copyright page.
* **Optional parts.** Prolog, epilog and blurb always begin on a page of their own, always keep their
  text, stay writable when switched off, are greyed out and left out of the page numbering. The
  checkbox in the tree decides membership in the book, not existence.
* **The blurb** is cover text: always the last sheet, set off by a hard edge, without a page number.
* **AI** sits where its unit of work is: a floating bar at the focused block, a part level action in
  the inspector. A generated part is provisional and is accepted or discarded; a rewritten paragraph
  is replaced at once and taken back through undo.
* **Design** changes take effect on the paper immediately.

## 4. Requirements

### Functional Requirements

* Title page, copyright page, prolog, every chapter, epilog and blurb are written on the paper.
* Prolog, epilog and blurb are switched into and out of the book by a checkbox in the tree, without
  losing text and without a confirmation.
* Text is drawn in the design of the book, per element class: title, chapter title, chapter title
  appendix, body text.
* Fonts are chosen from the families installed on the machine; a missing or differently measuring
  font is reported with the substitute in use.
* Page breaks are shown where the printed book breaks: as a sheet gap between paragraphs, as a marker
  line with the page number inside a paragraph.
* Paragraphs are the editing unit: create, split, join, delete, reorder.
* No inline character formatting anywhere; pasted rich text is reduced to plain text.
* Every text change, structural change and applied AI result is undoable and redoable.
* Prompts, part data and design styles live in the inspector, page format and margins in the project
  settings dialog - never on the paper.
* Preview renders the whole book and scrolls to the part selected in the tree; a paragraph falls on
  the same page in both surfaces.
* The renderer draws a document layout without knowing what a book is; sheet, gap, marker and
  background are styled through the library's stylesheet and overridden by the application.
* A demo renders a document with the library without any ai-ghost module on its path.

### Technical Requirements

* Kotlin and Gradle. JavaFX exclusively in `app/ui` and in `lib/layouting-fx`; the build fails when
  the library gains any other dependency or a type of the application in a signature.
* Pagination and typography stay in a toolkit free module and measure through an interface; the
  layout result carries no toolkit type.
* No font file is read, parsed or shipped; `Font.loadFont` is not used. The Ghost Writer face stays a
  display face of the user interface.
* Measuring is an FX thread operation and says so in the API.
* All geometry in points as `Double`; millimetres exist in the UI only.
* Model changes follow `fx-model`; UI work follows `ui-styling`, `fx-component-lifecycle`, `icons`
  and `font`; tests follow `testing`; documentation follows `project-docs`; workflows follow
  `ci-pipeline`.
* Text of a part stays `List<String>`; view state belongs to `Preferences`, never to the project
  document.
* No new third party dependency without asking the user. JavaFX in a library module is the one such
  decision this feature needs.

## 5. Architecture

```text
lib/ai-ghost-layouting          (no toolkit, no app)
        ▲                            ▲
        │                            │
lib/ai-ghost-layouting-model   lib/ai-ghost-layouting-fx   (JavaFX component library)
        ▲                            ▲
        └─────────── app/ui ─────────┘        (ai-ghost specific glue)
```

* **`lib/layouting`** - typesetting engine: text blocks plus a style of its own in, a `DocumentLayout`
  of pages and placed lines out. Owns `TextMetrics`. Knows no toolkit and no manuscript model.
* **`lib/layouting-model`** - the only module depending on both sides: builders from `Book`, `Design`
  and `Meta` into the engine's blocks and styles.
* **`lib/layouting-fx`** - measuring (font catalogue, resolution, JavaFX `TextMetrics`) and drawing
  (`PaperPageView`, `PaperFlowView`) in one module, packages `...layouting.fx.font`, `.control`,
  `.skin` plus its own stylesheet.
* **`lib/ai-ghost-ai`** - the action port (`rewrite`, `expand`, `shorten`, `generatePart`) and a stub.
* **`app/ui`** - `BookPartEditor`, `Inspector`, `AiActionBar`, the reworked `Editor` and its routing,
  undo, the `FontData` translation and the metrics fingerprint.

**Model extension.** `Design` gains `PageFormat` and line spacing per element class; `FontData` gains
the metrics fingerprint; `Book` carries prolog, epilog and blurb always, each with its `included`
switch; `Preferences` gains the editor view state. Each is mirrored per `fx-model`.

```text
ProjectProperty
  ├─ designProperty ─┬─> Inspector (style sections)
  │                  └─> LayoutEngine ─┬─> PaperFlowView   (break positions)
  │                                    └─> PaperPageView   (exact painting)
  ├─ bookProperty ──> BookPartEditor <──(change events)──> PaperFlowView
  └─ (selection) ProjectList.selectedItem ──> EditorViewModel ──> shown part
                                                   ▲
                                    TextMetrics ───┘ (layouting-fx, JavaFX Text, on the FX thread)
```

## 6. Implementation Plan Overview

IP-22 belongs to the font foundation and is listed with IP-01; IP-25 to IP-28 carry the renderer
library and were added after IP-01 to IP-03 were completed. IP-20 was removed with the export. The
numbering is kept stable rather than renumbered.

| ID    | Implementation Plan                       | Objective                                                             | Dependencies         |
|-------|-------------------------------------------|-----------------------------------------------------------------------|----------------------|
| IP-01 | Font Discovery And Text Measuring ✅       | Installed families, resolution, fallback, measuring through JavaFX     | -                    |
| IP-22 | Font Identity And Substitution Reporting ✅| Record the metrics used, detect and report a substitution             | IP-01                |
| IP-02 | Design Page Format Model ✅                | Page format, margins and spacing in `Design`, mirrored                | -                    |
| IP-24 | Optional Parts In The Model ✅             | Prolog, epilog and blurb always present and switchable                | -                    |
| IP-03 | Layout Core ✅                             | Resolved styles, line breaking, alignment, placed lines               | IP-01, IP-02, IP-24  |
| IP-04 | Pagination And Page Break Policy          | Page filling, breaks, odd/even margins, policy hook                   | IP-03                |
| IP-25 | Renderer Library Module ✅                 | New JavaFX library module, JPMS, TestFX, CI, architecture rule        | -                    |
| IP-26 | Font And Measuring Migration ✅            | Catalogue, resolution and metrics move into the library               | IP-01, IP-25         |
| IP-05 | Incremental Layout And Caching            | Per paragraph invalidation so typing stays responsive                 | IP-04                |
| IP-06 | Layout Regression Harness                 | Golden page structures and the surface comparison                     | IP-04, IP-07, IP-08  |
| IP-07 | Paper Page View                           | Exact read-only renderer of a document layout, in the library         | IP-04, IP-26         |
| IP-08 | Paper Flow View                           | Writing sheet with page geometry and break marks, in the library      | IP-04, IP-26         |
| IP-27 | Library Styling And Theming API           | Own stylesheet, style classes, override by the ai-ghost palette       | IP-07, IP-08         |
| IP-28 | Standalone Reuse And Documentation        | Demo without ai-ghost, published artifact, docs, dependency check     | IP-27                |
| IP-09 | Undo And Redo Infrastructure ✅            | One undo stack over model changes of the editor                       | -                    |
| IP-10 | Book Part Writing Surface                 | Caret, typing and binding of headings and paragraphs                  | IP-08, IP-09         |
| IP-11 | Paragraph Structure Operations            | Split, join, delete and reorder paragraphs                            | IP-10                |
| IP-12 | Inspector Shell And Content Sections ✅    | Context panel with book and part sections, absorbs `BookEditor`       | -                    |
| IP-13 | Design Style Sections                     | Editing the styles in the inspector with live effect                  | IP-02, IP-12, IP-26  |
| IP-14 | Project Settings Dialog ✅                 | Page format, margins and empty pages in a dialog                      | IP-02                |
| IP-15 | Editor Arrangement And Tree Routing       | Three zones, routing of every tree node, view state persisted         | IP-11, IP-12         |
| IP-16 | Writing And Preview Modes                 | Mode switch, whole book preview, scrolling, virtualisation            | IP-05, IP-07, IP-15  |
| IP-17 | AI Action Port                            | Action interface in `lib/ai` with a stub implementation               | -                    |
| IP-18 | AI Actions On Paragraph And Heading       | Floating action bar, replace with undo                                | IP-10, IP-17         |
| IP-19 | AI Part Generation With Provisional State | Generating a part, provisional display, accept or discard             | IP-12, IP-17         |
| IP-21 | In-Paragraph Sheet Split                  | Real sheet gap inside a paragraph while writing (optional)            | IP-11                |
| IP-23 | Optional Book Parts In The Tree           | Checkbox switching prolog, epilog and blurb into and out of the book  | IP-15, IP-24         |

## 7. Implementation Plans

Each open plan is written out under `.claude/plans/implementation` with its tasks, its constraints
and its tests. Named here are only the boundary of the plan and the reasoning behind it that the
detailed plan itself does not carry.

### IP-01: Font Discovery And Text Measuring ✅

Completed, in `app/ui`. The helper `Text` node is created once and reused, and widths are not rounded
up: rounding is right for a control's preferred width but makes line breaking coarse and size
dependent. Words are measured, not paragraphs, which keeps the cache small and reusable. IP-26 moves
the reusable half into the library, which is why nothing here is rewritten.

### IP-22: Font Identity And Substitution Reporting ✅

Plan: `FP-001-IP-22-SchriftIdentitaetUndErsatzmeldung.md`

Completed, split along the module boundary. The fingerprint comes from measurements, not from the
file: it captures exactly what influences the layout and stays quiet about the rest. Reference set
and size are fixed for all time, or every older project reports a false mismatch; the set grew to
Latin-1, Latin Extended-A and Cyrillic, because a substituted family usually differs in exactly
those letters while plain ASCII still matches. The measuring is pure JavaFX and knows no type of
this application, so it sits in `lib/layouting-fx`, which exports its first package with it, while
`app/ui` keeps what records it: the translation onto `FontMetricsData`, the comparison and the
report. A fingerprint is written when a project is saved and only where none stands yet - the design
editor of IP-13 does not exist yet, and overwriting on every save would make the comparison
pointless.

### IP-02: Design Page Format Model ✅

Completed. Margins are inner/outer rather than left/right, because a printed book needs the gutter
and retrofitting it would touch model, FX model, storage and engine at once. Line spacing as a factor
survives a font size change.

### IP-24: Optional Parts In The Model ✅

Completed. The switch `included` sits on `Prolog`, `Epilog` and `Blurb`, not on `Book` and not in a
shared interface: no caller reaches it polymorphically, and a chapter can never use it. Because
nothing is deleted, IP-23 needs no confirmation and no restoring undo entry.

### IP-03: Layout Core ✅

Completed. The engine owns the measuring interface and knows neither a toolkit nor this application's
model; a block is text plus style, so title page, heading and paragraph need no type of their own.
The mapping from placed line back to source character range is what lets IP-10 place a caret and
IP-18 address a paragraph. Hyphenation is out of scope, so the breaking step stays behind an
interface.

### IP-04: Pagination And Page Break Policy

Plan: `FP-001-IP-04-SeitenumbruchUndPaginierung.md`

The policy interface exists although nothing implements it, because widows and orphans change where a
page ends and would otherwise reshape the engine later. A page carries two numbers that must not be
confused - its position in the layout, which an inactive page occupies too, and its page number in
the book. That distinction is what makes switching a part on a pure renumbering. An optional part
always starts a new page, so switching can never reflow a neighbouring part. A page also carries
whether it is inactive, numbered and set apart; the library learns "apart" from the result, never
that apart means blurb.

### IP-25: Renderer Library Module

Plan: `FP-001-IP-25-RendererBibliotheksmodul.md`

JavaFX in a library module needs the user's confirmation before the dependency is added. The
architecture rule is not deleted but sharpened - JavaFX belongs to `app/ui` and to a module that is
itself a JavaFX component library - otherwise every following plan violates the rules. The jlink
image must keep working, and the TestFX setup of `app/ui` is a model without precedent outside it.

### IP-26: Font And Measuring Migration ✅

Plan: `FP-001-IP-26-SchriftUndMessungUmzug.md`

The split runs along `FontData`: what names the application stays behind, the rest moves. Behaviour
does not change - the tests that pass before the move must pass after it, which is what makes the
move safe while later plans build on the classes. The FX thread constraint travels with the class,
because a consumer outside this repository has no plan to read.

Built as planned, with one shape decided on the user's request. `FontCatalog`, `FontResolver`,
`FontResolution` and `JavaFxTextMetrics` moved into `lib/layouting-fx` package
`org.pcsoft.app.aighost.layouting.fx.font`, alongside the fingerprint of IP-22; their three tests
moved with them and run on the module's own headless TestFX setup. The library type that replaces
`FontData` in every moved signature is `FontDescription` - family, `size: Int`, `bold`, `italic` -
kept a whole-point size so resolution and the measurement cache stay bit-for-bit as before. The
translation on the application side is a single extension `FontData.toFontDescription()` in
`app/ui` (`FontTranslation.kt`), modelled on `FontFingerprintTranslation.kt`, not a translator
object. `FontIdentity` is the only production caller and now resolves through it. Neither
`module-info` needed a structural change: the target package was already exported, `javafx.graphics`
already required, and `app/ui` already read `ai-ghost-layouting-fx` since IP-25 - only the comments
were sharpened. `SplashStageTest` was deleted on the user's request: it failed on the unmodified
HEAD in this headless environment (splash opacity `1.0` instead of `0.0`), outside the scope of this
plan. The full `build` and a forced `jlink` of `app/ui` are green.

### IP-05: Incremental Layout And Caching

Plan: `FP-001-IP-05-InkrementellesLayout.md`

This is the plan that decides whether the feature feels fast, and it comes before the surfaces. Two
things make the FX thread constraint bearable: only words are measured, so ordinary prose needs far
fewer measurements than it has characters, and once the words are cached, arranging lines and pages
is arithmetic that may run anywhere.

### IP-06: Layout Regression Harness

Plan: `FP-001-IP-06-LayoutRegressionsPruefstand.md`

Golden files are snapshots of numbers, not images: a numeric diff says which line moved. They are
produced against the deterministic metrics, never against a font installed on the build machine,
otherwise the result differs per developer and per runner. The surface comparison belongs to the
library, because both surfaces do.

### IP-07: Paper Page View

Plan: `FP-001-IP-07-SeitenAnsicht.md`

The component decides nothing about typography; it paints coordinates, which is what makes it agree
with the writing surface by construction. Virtualisation lands here rather than in IP-16: a library
that cannot show a long document is not reusable.

### IP-08: Paper Flow View

Plan: `FP-001-IP-08-SchreibblattAnsicht.md`

The native control wraps with the same text stack the engine measured with, so the breaks agree once
the control's insets are taken out of the column width. Debouncing belongs on the break recomputation,
not on the text. The component owns the caret, the consumer owns the text - that is what keeps the
component reusable and undo working. Applying a change, the binding to `BookPartProperty` and the
ai-ghost behaviour belong to IP-10; splitting a control at a break is IP-21.

### IP-27: Library Styling And Theming API

Plan: `FP-001-IP-27-BibliotheksStyling.md`

Only the chrome is styleable; the text is styled by the layout result. A stylesheet that could change
a font size would quietly reopen the fidelity chain, so the boundary has to be explicit in the API
documentation.

### IP-28: Standalone Reuse And Documentation

Plan: `FP-001-IP-28-EigenstaendigeNutzung.md`

A library stays reusable only as long as something fails when it stops being so, which is why the
dependency check is automated. The demo is a sample in its own source set, not a product.

### IP-09: Undo And Redo Infrastructure ✅

Plan removed, was `FP-001-IP-09-UndoRedoInfrastruktur.md`.

It has to exist before the first surface records into it, otherwise editing and AI each grow their
own mechanism. It stays in `app/ui`, because the library applies no change.

Built as `UndoEntry`/`PropertyUndoEntry`/`UndoStack` under `app/ui/.../undo`, owned by
`MainWindowViewModel` and cleared on `newProject()`/`openProject()`. Beyond the original scope, the
user asked for a named tooltip per entry and a history dropdown on the Undo/Redo tool bar buttons
(`SplitMenuButton`, styled like a browser back button) that jumps several steps at once via
`undoUntil`/`redoUntil`; `UndoStack.visibleEntryCount` bounds how many entries the dropdown exposes,
as a plain property rather than a persisted preference. The `icon-creator` agent had no image-writing
tool available in this environment, so `undo@32.png`/`redo@32.png` were drawn with a small Pillow
script instead, matching the existing icon palette - confirmed with the user.

### IP-10: Book Part Writing Surface

Plan: `FP-001-IP-10-Schreibflaeche.md`

Prolog, chapter and epilog are one editor over one `BookPartProperty`, not three; the blurb is the
exception that has to be built. The caret is a paragraph index plus a character offset, never a
coordinate, so editing survives a design change. A switched off part stays writable - greying says it
is not in the book, not that it is locked. Everything ai-ghost specific is answered by an API of the
library, never by a dependency back into the application.

### IP-11: Paragraph Structure Operations

Plan: `FP-001-IP-11-AbsatzOperationen.md`

Block list, layout and caret target change together and are one transaction. Splitting mid-paragraph
is the case that exposes an off-by-one in the character range mapping of IP-03. The library requests
the operation, the application performs it.

### IP-12: Inspector Shell And Content Sections ✅

Plan: `FP-001-IP-12-InspectorGrundgeruest.md` (removed on completion)

Sections stay fixed and identically named, because the inspector mixes part scoped and project scoped
data and a panel that silently changes shape makes it unclear what is being edited.

Built as planned: a new `Inspector` MVVM-FX trio (`Inspector`, `InspectorView`, `InspectorViewModel`,
`InspectorView.fxml`) with two fixed `TitledPane` sections, "Book" and "Chapter", each with its own
runtime-only `expandedProperty` in the view model. The `BookEditor` trio, its FXML, CSS and tests were
removed entirely via `git rm` rather than merely trimmed - once title, title lines, author, copyright
and the book-level prompts moved into the inspector's "Book" section, nothing user-facing was left in
`BookEditor`. `Editor`/`EditorView`/`EditorViewModel` now wire `Inspector` into the split pane instead.
One addition beyond the original scope: `lib/fx-model` gained a public factory
`ChapterProperty.of(chapter: Chapter): ChapterProperty` (in the same package as `ChapterProperty`,
using its existing internal no-arg constructor plus `set(chapter)`), because building a bindable
`ChapterProperty` from a plain `Chapter` selected in the project tree had no prior counterpart; its
KDoc was corrected accordingly and a developer test (`ChapterPropertyOfTest`) was added.

### IP-13: Design Style Sections

Plan: `FP-001-IP-13-DesignStilAbschnitte.md`

The section writes the same `DesignProperty` the layout reads, which is what makes the live update
work without extra plumbing. Only installed families are offered, because an unresolvable family
cannot be measured.

### IP-14: Project Settings Dialog ✅

Plan: `FP-001-IP-14-ProjekteinstellungenDialog.md` (removed on completion)

Millimetres are shown, points are stored. A margin sum exceeding the page must be refused, or the
engine receives a negative column width.

Built wider than first planned, on the user's request: the dialog is a master-detail shell with a
`ProjectSettingsTree` (root hidden) on the left and the section editor on the right. Only the
`General` section is real - `GeneralSettings` with page-size presets, the four margins and the two
empty-page flags, bound to a working-copy `DesignProperty`. The `Design` node and its four children
(`Epilog`, `Chapter`, `Prolog`, `Blurb`) are `PlaceholderSettings` panels; their real style editors
stay with IP-13. No new model was added - the design POJOs for prolog/epilog/blurb and a separate
title appendix are still open and were deferred to a later step.

The dialog keeps a working copy (a detached `ProjectProperty` whose design is a deep copy of the
target); OK and APPLY write the page geometry and the two flags back into the real `DesignProperty`,
CANCEL / ESCAPE discard. Buttons are the standard `OK`, `CANCEL`, `APPLY`
(`DialogButtons.OK_CANCEL_APPLY`); APPLY is consumed so it stores without closing, and OK and APPLY
are disabled while the input cannot be stored. The menu item and the tool bar button in
`MainWindowView` are now wired; the action is always available because a project always carries a
design. Styling landed in a new `styles/component/project-settings.css` (registered in
`AiGhostTheme`) rather than in `dialog.css`, because the combo box, the check box and the plain
separator are first used here. User docs: `docs/docs/project-settings.md`.

Adjusted afterwards on the user's request: the page-format editor moved from the `General` node to
the `Design` node, so `Design.implemented` is now the real editor and `General` is an empty
placeholder; the dialog opens on `Design`. The `Design` branch gained two more placeholder children,
`Title page` and `Copyright page`, ahead of `Epilog`. `ProjectSettingsSection` was reshaped from an
`enum` into a `sealed interface` with `data object` cases, mirroring `ProjectListItem`, and
`ProjectSettingsTreeView` now builds its tree from explicit `TreeItem` fields with a named
`ProjectSettingsTreeCell`, the same way `ProjectListView` does - so a section that later stands for a
single book part can become a `data class` without changing how the tree is built.

### IP-15: Editor Arrangement And Tree Routing

Plan: `FP-001-IP-15-EditorAufteilungUndBaumRouting.md`

`ProjectList` keeps its API; the routing is an exhaustive `when` in `EditorViewModel`, so a node that
gains a meaning later is a compiler error. View state goes to `Preferences`, never into the project
document.

### IP-16: Writing And Preview Modes

Plan: `FP-001-IP-16-SchreibUndVorschauModus.md`

This is where the FX thread constraint becomes visible to the user: the whole book has to be measured
before its page count is known. A progress indication for the first layout has to be planned for
rather than hoped away. The reading position is a paragraph reference, not a scroll offset, because
the modes have different geometry.

### IP-17: AI Action Port

Plan: `FP-001-IP-17-AiAktionsPort.md`

Streaming is in the interface from the start, because retrofitting it changes every caller. A defined
split rule keeps a generated chapter from landing in one paragraph.

### IP-18: AI Actions On Paragraph And Heading

Plan: `FP-001-IP-18-AiAktionenAmAbsatz.md`

A paragraph result replaces directly because undo already exists; a comparison step would cost more
than it protects. The action bar is an overlay of the application above the library's node and finds
its block through the focus events of IP-08 - the library gains no notion of an AI.

### IP-19: AI Part Generation With Provisional State

Plan: `FP-001-IP-19-AiTeilGenerierung.md`

Generating a part overwrites a lot, so the size of the destruction, not the origin of the text,
decides how explicit the confirmation is. Streamed text arrives off the FX thread and is measured and
shown on it, so chunks are handed over in batches rather than per token.

### IP-21: In-Paragraph Sheet Split

Plan: `FP-001-IP-21-SeitentrennungImAbsatz.md`

The split point moves with every keystroke, so controls are merged and split while the caret sits
inside them - that is the hard part. Deliberately last: the feature is usable without it, and the
effort is justified only once the marker line has been lived with. It is a rendering concern and
lands in the library.

### IP-23: Optional Book Parts In The Tree

Plan: `FP-001-IP-23-OptionaleTeileImBaum.md`

The one plan that changes the project tree, and deliberately narrowly: structure and `selectedItem`
API stay, a checkbox is added on exactly three nodes. `CheckBoxTreeItem` applies its tick to the
subtree by default, which has to be constrained. Because IP-24 keeps the text, clearing a tick
destroys nothing - it is still one undo entry.

## 8. Dependency Graph

```text
IP-01✅┬─> IP-22✅
       └─┐
IP-25✅┬─┴─> IP-26✅─┬─> IP-07 ─┬─> IP-27 ──> IP-28
                    ├─> IP-08 ─┘
                    └─> IP-13   (with IP-02, IP-12✅)
IP-02✅┬───> IP-03✅ ──> IP-04 ─┬─> IP-05 ──────────────┐
IP-24✅┤  │                   │                       │
       └─> IP-14✅              ├─> IP-07 ─┬────────────┤
                                │          ├─> IP-06    │
                                └─> IP-08 ─┘            │
                                      │                 │
                                      └─> IP-10 ────────┼──> IP-18   (with IP-17)
                                      (with IP-09✅)     │
                                                └─> IP-11 ─┬─> IP-15 ─┬─> IP-16
                                                           │          └─> IP-23   (with IP-24)
                                                           └─> IP-21
IP-09✅ ──> IP-10, IP-18, IP-19
IP-12✅┬─> IP-13
       ├─> IP-15
       └─> IP-19   (with IP-17)
IP-17 ─┬─> IP-18
       └─> IP-19
IP-05, IP-07, IP-15 ──> IP-16
IP-07, IP-08 ──> IP-06, IP-27
```

The graph is drawn as two trees that grow from different roots and meet only at one seam.

**Upper tree - the renderer library `lib/layouting-fx`.** Roots: IP-01 and IP-25. It builds the
reusable JavaFX renderer that carries no type of this application: font discovery and text measuring
(IP-01), the font identity and substitution report (IP-22), the library module with its JPMS, TestFX
and CI setup (IP-25), the move of catalogue, resolution and measuring into it (IP-26), the two views
- exact page and writing flow (IP-07, IP-08) -, the styling and theming API (IP-27) and the
standalone-reuse proof with its documentation (IP-28). The tree owns everything a consumer outside
this repository would also get.

**Lower tree - the writing surface in `app/ui`.** Roots: IP-02 and IP-24, plus the independent
strands IP-09 ✅, IP-12 ✅ and IP-17. It builds the editor feature on top of the library: the design page
format model (IP-02) and the always-present optional parts (IP-24), the toolkit-free layout core
(IP-03), pagination and the page-break policy (IP-04), incremental layout and caching (IP-05), the
project settings dialog (IP-14), the layout regression harness (IP-06), the editing surface and
paragraph operations (IP-10, IP-11), editor arrangement with write and preview modes (IP-15, IP-16),
undo and redo (IP-09 ✅), the inspector shell and its content sections (IP-12 ✅), the AI action port and
the actions built on it (IP-17, IP-18, IP-19), the optional parts in the project tree (IP-23) and
the in-paragraph sheet split (IP-21).

**The seam.** The lower tree consumes IP-07 and IP-08 of the upper one - the app draws its pages
with the library views. IP-13 (design style sections) is the second link: it needs IP-26 of the
upper tree together with IP-02 and IP-12 ✅ of the lower one. Nothing else crosses between the two.

Completed: **IP-01** ✅, **IP-22** ✅, **IP-02** ✅, **IP-24** ✅, **IP-03** ✅, **IP-25** ✅, **IP-26** ✅,
**IP-09** ✅, **IP-12** ✅.
Independent starting points: **IP-09** ✅, **IP-12** ✅, **IP-17**.

## 9. Risks and Open Questions

* **JavaFX in a library module** was confirmed and is settled. `.claude/rules/architecture.md` now
  names the JavaFX component library under `lib` as an allowed place for the toolkit. No decision
  blocks a plan any more.
* **Naming of the renderer library.** `ai-ghost-layouting-fx` keeps the convention of the sibling
  modules but carries the application name into a library meant for reuse. Open.
* **Publication of the renderer library** as a real artifact or only inside this repository. Open;
  IP-28 assumes the way the other library modules are handled.
* **TestFX in a library module** has no precedent here; part of IP-25 for that reason.
* **Scope of `PaperFlowView`.** A full text editing component and the part most likely to want
  ai-ghost specific behaviour later. Every such need is answered by an API, never by a dependency
  back into the application.
* **Measuring belongs to the FX thread.** The central technical risk. IP-05 answers it and has to
  measure it; IP-16 has to show the cost rather than hide it.
* **The default font of a new project** is open: `FontData` defaults to `Arial`, which is not
  installed everywhere; a default resolving through the fallback chain is needed.
* **The insets of the native text control** shift its wrapping against the engine's. IP-08 takes them
  out of the column width; IP-06 catches a remaining drift.
* **The correspondence is per machine.** IP-22 makes a substitution visible; it cannot make it go
  away.
* **Kerning across word boundaries is lost.** Words are measured one by one, so a justified line is
  marginally too wide. Engine and renderer are wrong in the same way, so the fidelity chain holds. To
  be checked in IP-26.
* **Widows, orphans, hyphenation** are excluded; the hook exists, no implementation ships.
* **Resolving a provisional AI part** on part change or project close (IP-19) is undecided.
* **Three new Gradle modules** require a check against the `ci-pipeline` skill.

### Rejected third party libraries

Recorded with the reason, because the question returns otherwise.

* **Apache PDFBox** carries no layout engine at all - line breaking, flow, alignment and pagination
  are the caller's arithmetic. It contributes font parsing, embedding and PDF writing, which the
  export feature needs and this one does not. It stays the decided choice there.
* **Apache FOP** has a real engine, but its input is XSL-FO XML - unusable for incremental layout -
  its placed result has no stable public API, it measures with its own font handling, and it is a
  heavy non-modular dependency against a jlink image.
* **`java.awt.font.TextLayout` / `LineBreakMeasurer`** measure through Java2D while Prism paints.
  Break opportunities already come from `java.text.BreakIterator`, hit testing from `Text.hitTest`,
  and `requires java.desktop` would pull one of the largest JDK modules into the image.
* **SWT `TextLayout`** matches in capability but needs a `Display` with its own event loop, so the
  application would have to become an SWT application. Native artifact per platform, and EPL-2.0 is
  not on the allowlist.

The common cause is not incidental: a layout engine is a measurement plus a breaking algorithm, so
every candidate with real layout brings its own measurement. Taking the layout while keeping the
JavaFX measurement is not a combination that exists, and the fidelity chain rules all four out.

### Decisions taken

* **Page format** A5 by default, presets A4, 12,5 x 19 cm, 13,5 x 21,5 cm, 6 x 9 inch; margins 20 mm
  inner, 15 mm outer, 15 mm top, 20 mm bottom.
* **Front matter** is the title page followed directly by the copyright page.
* **The blurb** is always the last sheet, set off by a hard edge, without a page number.
* **Optional parts** always begin on a page of their own, keep their text, are greyed out and stay
  writable.
* **The metrics fingerprint** is taken over printable ASCII plus umlauts and sharp s at 12 pt, with
  ascent, descent and leading. Set and size are fixed from that point on.
* **The renderer is a library**, not a component of `app/ui`.

### Deliberately out of scope

* **Export in any form.** An own feature, implemented as a plugin on Apache PDFBox. What this feature
  owes it is a toolkit independent `DocumentLayout` and a third `TextMetrics` implementation that
  slots in without touching the engine. Anything a font file has to be read for belongs there.
* **Plugin infrastructure.** `ai-ghost-plugin-api` carries only `ProjectPart` and `ProjectPartInfo`;
  there is no plugin interface, no loader and no service registration. A plugin based export needs
  that built first - a feature of its own and a prerequisite of the export, not of this one.

## 10. Feature Completion Criteria

* Title page, prolog, every chapter, epilog and blurb can be written completely in the application;
  the "Not implemented yet." placeholder is gone, and the copyright page follows the title page.
* Prolog, epilog and blurb are switched from the tree; a switched off part keeps its text, is greyed
  out, is left out of the numbering and stays writable. The blurb is the last sheet without a number.
* A document written before this feature opens with exactly the parts it used to have.
* Text is written in the typography, margins and page structure of the book, with page breaks marked
  where the printed book breaks.
* A paragraph falls on the same page in the writing surface and in the preview, and the build fails
  when the two drift apart.
* Fonts come from the installed families; no manuscript font is shipped and no font file is opened. A
  missing or differently measuring font is reported with its substitute.
* A changed design value changes the open text without reopening the project.
* Writing in a book sized document stays responsive, and the first layout of a long book does not
  appear as a frozen window.
* Prompts, part data and design are reachable beside the sheet and never interrupt the text.
* Every text change, structural change and applied AI result can be undone and redone.
* An AI action runs for a paragraph, a heading and a whole part; a paragraph result is applied
  directly, a part result is accepted or discarded.
* The layout result carries no toolkit type, so a later export plugin consumes it unchanged.
* `lib/layouting-fx` builds, tests and is covered as a JavaFX library module; its dependency set is
  `ai-ghost-layouting` plus JavaFX and the build fails when it grows. Both surfaces and the measuring
  live there, exist nowhere twice, and a demo runs without any ai-ghost module on its path.
* The library ships its own stylesheet and takes on the ai-ghost palette through overridden classes.
* JavaFX appears in no module but `app/ui` and `lib/layouting-fx`, and the architecture rule says so.
* The project tree keeps its structure and its selection API; the checkbox on three nodes is the only
  addition to it.
* Build and tests are green, documentation and changelog are updated per the `project-docs` skill.
