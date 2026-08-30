# Feature Plan: Paper Writing Surface

## 1. Objective

Writing a book part in ai-ghost should feel like writing on the printed page itself. The user edits
title, headings and paragraphs of prolog, chapters, epilog and blurb directly on a sheet that already
carries the typography, margins and page structure of the finished book, instead of editing raw
fields and imagining the result.

Two forces limit how far the paper metaphor may be taken:

* the appearance is owned by the project design, not by the writer - there is no inline formatting,
* text is partly written by the AI, which works on a part, a heading or a paragraph, not on a caret
  position.

The feature resolves both by making the sheet the only writing surface, moving everything that is not
printed text into a context panel beside it, and attaching the AI to the structural units the model
already knows.

What is shown while writing must be what the preview shows. The feature therefore does not build a
preview beside an editor; it builds one layout engine that decides every line break, every position
and every page break, and lets the writing surface and the preview consume that one result.

The book is written on that surface in the shape the model already has: a **title page** built from
the book title and its further title lines, and the **written parts** - prolog, chapters, epilog - each
with its heading and its text, plus the **blurb**, which is cover text rather than a page.

Prolog, epilog and blurb are **optional parts**: a book has them only if the user wants them. Each
always stands on pages of its own and each is always there to be written on - the checkbox in the
project tree decides whether those pages belong to the book, not whether they exist. A part that is
switched off keeps its text and is shown greyed out, so a prolog can be written, taken out of the book
and put back in without losing a word. The tree keeps its structure and its selection API; the
checkbox is the one thing this feature adds to it.

**Export is not part of this feature.** It is named here only as a constraint: the layout engine
produces a toolkit independent result, so an export built later - as a plugin, on Apache PDFBox -
consumes the same page structure instead of computing its own.

## 2. Current State

**Modules**

* `lib/ai-ghost-plugin-api` - carries `ProjectPart` and `ProjectPartInfo` and nothing else. There is
  no plugin interface, no loader and no service registration; even project part plugins are not
  loaded anywhere, `StorageIo.loadFromZip` is handed its part classes by the caller.
* `lib/ai-ghost-model` - plain POJOs; `Project` holds `meta`, `design`, `book`; `Book` holds
  `title`, `titleAppendix`, `prompts`, `prolog`, `chapters`, `epilog`, `blurb`; `BookPart`
  (`Prolog`, `Chapter`, `Epilog`) carries `title`, `titleAppendix`, `prompts`, `paragraph`.
* `lib/ai-ghost-fx-model` - mirrored JavaFX property models (`BookProperty`, `BookPartProperty`,
  `ChapterProperty`, `DesignProperty`, `StyleDataProperty`, `FontDataProperty`, ...).
* `lib/ai-ghost-ai` - currently only `TokenUtils`; no generation pipeline yet.
* `app/ui` - the only JavaFX module, MVVM FX (`FluentViewLoader`, `FxmlView`, `ViewModel`),
  I18N through `messages/bundle*.properties`, CSS split per component under `styles/component`.
  Modular (JPMS) and shipped as a jlink image.

**UI**

* `MainWindow` -> `Editor` -> `SplitPane`: `ProjectList` (tree, left) and a `StackPane` (right).
* The right side shows `BookEditor` only when the tree selection is `ProjectListItem.Root`; for
  every other selection a hard coded placeholder "Not implemented yet." is shown.
* `BookEditor` is a plain form: `AiTextField` for the title, `AiTextFieldList` for the title
  appendix, two `AiPromptArea` for content and style prompt.
* `EditorViewModel` already exposes `selectedProjectTreeItem` and derives `showBookEditor` from it.
* Components own no data: a property model is handed in through `bindXxx`, bidirectional bindings
  carry every change straight into the model.

**Missing today**

* No editor for prolog, chapter, epilog or blurb - paragraphs cannot be written at all.
* No rendering, no pagination, no preview.
* No design editor; `Design` exists but nothing reads it.
* `Design` carries no page format, no margins and no line spacing.
* `FontData.name` is a free font family name (`Arial` by default); nothing checks whether that family
  exists on the machine and nothing resolves it when it does not.
* No undo/redo of any kind.

## 3. Target State

The editing area right of the tree consists of three zones:

1. **Paper** (centre) - sheets in the page format of the design. The title page of the book, and the
   heading and paragraphs of the selected part, are shown with the fonts, sizes, weights, alignments
   and spacings the design prescribes, inside the real margins, with the real page breaks marked. The
   user writes directly into that text; there is no second preview panel.
2. **Inspector** (right, collapsible) - everything that is not printed text: content and style
   prompt of the selected part, chapter name, book title data, and the design styles. Fixed,
   always identically named sections, so it is never unclear what is being changed.
3. **Mode switch** - `Schreiben` and `Vorschau` over the same content. Preview drops carets and AI
   affordances, renders exactly and shows the whole book instead of one part.

**Fonts.** The application ships no manuscript font and reads no font file. It asks JavaFX which font
families are installed, offers those for selection, and measures text with `javafx.scene.text.Font`
through a hidden `Text` node. A font is therefore never redistributed, never parsed and never opened
by this application.

**Fidelity chain.** One layout engine turns design plus text into placed lines with absolute
coordinates in points. The preview paints those coordinates and decides nothing itself, so writing
surface and preview cannot disagree about where a page ends.

Measuring and drawing go through the same text stack: what the engine measured with
`javafx.scene.text.Font` is what JavaFX later draws. There is no second font implementation that
could answer differently.

The correspondence holds **for one project on machines carrying the same fonts**. A font that is
missing or measures differently elsewhere paginates differently - so the project records the metrics
it was written with, and a mismatch is reported when the project is opened instead of silently
producing different pages.

**Front matter.** The book opens with its title page - title, further title lines and the author -
and directly behind it stands the copyright page carrying author and copyright. Both are styled by
the design that already exists for them.

**Optional parts.** Prolog, epilog and blurb always begin on a page of their own and never share one
with a chapter. Their text exists whether or not they are switched on; the checkbox in the tree
decides whether their pages belong to the book. A part that is switched off keeps its pages visible
but greyed out, is left out of the page numbering, and stays writable - so it can be prepared,
taken out and put back without a word being lost and without a question being asked.

**The blurb** is cover text, not a page of the manuscript. It is always the last sheet, set off by a
hard edge rather than by the dashed marker a page break uses, and it carries no page number.

**AI.** Reachable where its unit of work is: a floating action bar at the focused paragraph or
heading, and a part level action in the inspector fed by the prompts of the part. A generated part
appears on the paper in a provisional state that is accepted or discarded; a rewritten paragraph is
replaced at once and taken back through undo.

**Design.** Changes take effect on the paper immediately, because the paper reads the same
`DesignProperty` the design section writes.

## 4. Requirements

### Functional Requirements

* The title page of the book is shown on the paper, built from the book title, its further title
  lines and the author, and styled by the title design.
* The copyright page follows directly behind the title page and carries author and copyright, styled
  by the author design and the copyright design.
* Prolog, every chapter and epilog can be written: heading, further heading lines and paragraphs.
* Prolog, epilog and blurb always begin on a page of their own.
* The blurb can be written: paragraphs and its single prompt, without a heading; it is the last sheet,
  set off by a hard edge, and carries no page number.
* Prolog, epilog and blurb are switched into and out of the book through a checkbox on their node in
  the project tree. Switching one off keeps its text, shows its pages greyed out and leaves them out
  of the page numbering; it never asks, because nothing is lost.
* A part that is switched off can still be written on.
* Text on the paper is drawn in the design of the book - font family, size, weight, slant, alignment
  and line spacing per element class (title, chapter title, chapter title appendix, body text).
* The font of an element is chosen from the families JavaFX reports as installed.
* Opening a project whose font is missing, or measures differently here, reports that and names the
  substitute in use, instead of changing the pagination silently.
* Paragraphs are the editing unit: create, split by ENTER, join by BACKSPACE at the start, delete,
  reorder.
* Page breaks are shown at the position the printed book breaks at. A break between two paragraphs
  is shown as a real gap between two sheets; a break inside a paragraph is shown as a marker line
  across the sheet, carrying the number of the page that begins there.
* Inline character formatting is not offered anywhere; pasted rich text is reduced to plain text.
* Every text change, structural change and applied AI result can be undone and redone.
* Prompts, part data and design styles are edited in the inspector, page format and margins in the
  project settings dialog - never on the paper.
* AI action per paragraph or per heading on the selection, AI action per part from the inspector.
* Preview mode renders the whole book and scrolls to the part selected in the tree.
* The page a paragraph falls on is the same in the writing surface and in the preview.
* Selecting a node in the tree opens or scrolls to that part; the tree keeps its current API.
* Changing a design value changes the paper without reopening the project.

### Technical Requirements

* Kotlin, Gradle; JavaFX exclusively in `app/ui`.
* Pagination and typography live in a UI free library module that measures through an interface, so a
  later export reuses the engine unchanged.
* **No font file is read, parsed or shipped.** Font families come from `javafx.scene.text.Font`, and
  text is measured with a hidden `javafx.scene.text.Text` node. `Font.loadFont` is not used. The
  Ghost Writer face stays what it is today, a display face of the user interface, and is not a
  manuscript font.
* Because measuring is JavaFX, the measuring implementation lives in `app/ui`; the interface it
  satisfies lives in the layout module. No library module gains a JavaFX or an AWT dependency.
* A project records the metrics fingerprint of the font it used - derived from measurements, not from
  a file - so a substitution is detectable.
* All geometry is stored and computed in points (1/72 inch) as `Double`; millimetres exist only in
  the UI.
* Model changes follow the `fx-model` rule: every POJO change under `lib/model` is mirrored in
  `lib/fx-model` including tests.
* New UI work follows `ui-styling`, `fx-component-lifecycle`, `icons` and `font`.
* No new third party dependency without asking the user. This feature is expected to need none.
* Text of a part stays `List<String>` paragraphs - the storage format of the manuscript is unchanged.
* View state (zoom, split positions, inspector collapsed, mode) belongs to `Preferences`, never to
  the project document.
* The layout result is a plain data structure with no toolkit type in it, so a later export plugin
  can consume it without depending on `app/ui`.
* Headless TestFX tests for every new component, unit tests for engine and layout, golden file tests
  for the page structure.

## 5. Architecture

### Modules

* **`lib/layouting` (`ai-ghost-layouting`)** - the typesetting engine, written as a general purpose
  library. Input: text blocks carrying nothing but their text and a `TextStyle` of the module's own,
  plus a column width. Output: a `DocumentLayout` - pages, each holding placed lines with absolute
  `x`, `y`, baseline, text run, resolved style and the index of the source block. Decides line
  breaking, alignment, spacing and page breaks. Owns the `TextMetrics` interface it measures through
  and ships a deterministic implementation for tests. Knows neither a toolkit nor `ai-ghost-model`:
  `Design`, `Book`, `BookPart` and `Meta` never reach it, which is what makes it reusable outside this
  application.
* **`lib/layouting-model` (`ai-ghost-layouting-model`)** - the bridge between the manuscript and the
  engine, named and packaged after `lib/fx-model`. It holds the builders that turn `Book`, `Design`
  and `Meta` into the engine's text blocks - the title page, the copyright page, a written part and
  the blurb - and translates `StyleData` plus the line spacing of `Design` into a `TextStyle`. It is
  the only module depending on both `ai-ghost-model` and `ai-ghost-layouting`.
* **`lib/ai-ghost-ai`** - gains the action port the UI calls (`rewrite`, `expand`, `shorten`,
  `generatePart`), plus a stub implementation until a provider integration exists.
* **`app/ui`** - the font side and the components. A small package holds the font catalogue, the
  resolution of a `FontData` to a `javafx.scene.text.Font`, the fallback chain and the JavaFX backed
  `TextMetrics` implementation; JavaFX therefore stays where the architecture rule puts it.

### Model extension

* `Design` gains `PageFormat` (page size, inner/outer/top/bottom margins) and line spacing
  per element class, mirrored to `PageFormatProperty` and `DesignProperty`.
* `FontData` gains the metrics fingerprint of the font actually used, so a substitution is
  detectable.
* `Preferences` gains the view state of the editor.

### UI components (`app/ui`, package `...app.ui.component`)

* `PaperPageView` - exact, read-only renderer. Paints a `DocumentLayout` sheet by sheet at the given
  coordinates. Used for preview.
* `PaperFlowView` - the writing sheet. Same page geometry and margins, but the blocks are native
  JavaFX text controls; page breaks are drawn as a sheet gap between paragraphs or as a marker line
  inside a paragraph, at the position the engine reports.
* `BookPartEditor` - caret, typing and binding of headings and paragraphs on `PaperFlowView`.
* `Inspector` - context panel with fixed sections; absorbs the fields of today's `BookEditor`.
* `AiActionBar` - floating action bar of the block in focus.
* `Editor` - arranger of the three zones and of the mode switch; `EditorViewModel` routes
  `ProjectList.selectedItem` to the part shown.

### Data flow

```
ProjectProperty
  ├─ designProperty ─┬─> Inspector (style sections)
  │                  └─> LayoutEngine ─┬─> PaperFlowView   (break positions)
  │                                    └─> PaperPageView   (exact painting)
  ├─ bookProperty ──> BookPartEditor <──(bidirectional)──> headings / paragraphs
  └─ (selection) ProjectList.selectedItem ──> EditorViewModel ──> shown part
                                                   ▲
                                    TextMetrics ───┘ (JavaFX Text, on the FX thread)
```

## 6. Implementation Plan Overview

IP-22 belongs to the font foundation and is listed with IP-01; the numbering is kept stable rather
than renumbered.

| ID    | Implementation Plan                       | Objective                                                             | Dependencies         |
|-------|-------------------------------------------|-----------------------------------------------------------------------|----------------------|
| IP-01 | Font Discovery And Text Measuring         | Installed families, resolution, fallback, measuring through JavaFX    | -                    |
| IP-22 | Font Identity And Substitution Reporting  | Record the metrics used, detect and report a substitution             | IP-01                |
| IP-02 | Design Page Format Model                  | `Design` v2 with page format, margins and spacing, mirrored           | -                    |
| IP-24 | Optional Parts In The Model               | `Book` v2 with prolog, epilog and blurb always present and switchable | -                    |
| IP-03 | Layout Core                               | Resolved styles, line breaking, alignment, placed lines               | IP-01, IP-02, IP-24  |
| IP-04 | Pagination And Page Break Policy          | Page filling, breaks, odd/even margins, policy hook                   | IP-03                |
| IP-05 | Incremental Layout And Caching            | Per paragraph invalidation so typing stays responsive                 | IP-04                |
| IP-06 | Layout Regression Harness                 | Golden page structures and the surface comparison                     | IP-04, IP-07, IP-08  |
| IP-07 | Paper Page View                           | Exact read-only renderer of a document layout                         | IP-04                |
| IP-08 | Paper Flow View                           | Writing sheet with page geometry and break marks                      | IP-04                |
| IP-09 | Undo And Redo Infrastructure              | One undo stack over model changes of the editor                       | -                    |
| IP-10 | Book Part Writing Surface                 | Caret, typing and binding of headings and paragraphs                  | IP-08, IP-09         |
| IP-11 | Paragraph Structure Operations            | Split, join, delete and reorder paragraphs                            | IP-10                |
| IP-12 | Inspector Shell And Content Sections      | Context panel with book and part sections, absorbs `BookEditor`       | -                    |
| IP-13 | Design Style Sections                     | Editing the styles in the inspector with live effect                  | IP-01, IP-02, IP-12  |
| IP-14 | Project Settings Dialog                   | Page format, margins and empty pages in a dialog                      | IP-02                |
| IP-15 | Editor Arrangement And Tree Routing       | Three zones, routing of every tree node, view state persisted         | IP-11, IP-12         |
| IP-16 | Writing And Preview Modes                 | Mode switch, whole book preview, scrolling, virtualisation            | IP-05, IP-07, IP-15  |
| IP-17 | AI Action Port                            | Action interface in `lib/ai` with a stub implementation               | -                    |
| IP-18 | AI Actions On Paragraph And Heading       | Floating action bar, replace with undo                                | IP-10, IP-17         |
| IP-19 | AI Part Generation With Provisional State | Generating a part, provisional display, accept or discard             | IP-12, IP-17         |
| IP-21 | In-Paragraph Sheet Split                  | Real sheet gap inside a paragraph while writing (optional)            | IP-11                |
| IP-23 | Optional Book Parts In The Tree           | Checkbox switching prolog, epilog and blurb into and out of the book  | IP-15, IP-24         |

## 7. Implementation Plans

### IP-01: Font Discovery And Text Measuring

**Objective**

Know which fonts the machine offers, resolve a design font to one of them, and measure text with
JavaFX - without ever opening a font file.

**Scope**

In scope: the font package in `app/ui`; the family list from `javafx.scene.text.Font`; resolution of a
`FontData` to a `javafx.scene.text.Font` including weight and slant; a deterministic fallback chain
and a reportable "family not installed" result; the JavaFX backed `TextMetrics` implementation
measuring word widths, space width and line metrics through a reused hidden `Text` node; the
measurement cache; caching of the family list with an explicit rebuild; headless TestFX tests. Out of
scope: the `TextMetrics` interface and its deterministic implementation, which belong to the layout
module and arrive with IP-03; the storage of the fingerprint, which is IP-22.

**Affected Areas**

`app/ui` (new font package), `module-info.java`, `lib/ai-ghost-model` (read only).

**Dependencies**

None.

**Expected Result**

The application knows which families exist on this machine, resolves a `FontData` deterministically
to a JavaFX font, and answers the width of a piece of text in a given style.

**Technical Considerations**

No font file is read, parsed or shipped, and `Font.loadFont` is not used: families come from
`Font.getFamilies()` and every measurement from a hidden `javafx.scene.text.Text` node - set the font,
set the text, set `wrappingWidth` to zero and read `prefWidth(-1)`.

Two departures from the usual form of that helper are deliberate. The helper node is **created once
and reused**, because building a `Text` per measurement is far too expensive for a per keystroke
layout. And the result is **not rounded up**: rounding to whole pixels is right for the preferred
width of a control, but it makes line breaking coarse and size dependent, so the fractional value is
kept and rounding happens at painting time only.

A `Text` node belongs to the FX thread, which makes measuring an FX thread operation and therefore
constrains the layout engine - see IP-05. The measurer is not thread safe and must say so.

The engine breaks lines itself, so what is measured are **words**, a space, and the line metrics -
not whole paragraphs. That keeps the cache small and reusable across paragraphs and is why the cache
is part of this plan rather than an optimisation later.

### IP-22: Font Identity And Substitution Reporting

**Objective**

Make a font substitution visible instead of letting it change the pagination in silence.

**Scope**

In scope: `FontData` extended by a metrics fingerprint - derived from measuring a fixed reference
character set at a reference size, plus the line metrics; writing that fingerprint when a font is
chosen; comparing it when a project is opened; the report naming the affected elements, the missing or
differing font and the substitute now in use; the deterministic fallback taking effect; mirrored FX
properties and their tests.

**Affected Areas**

`lib/model` (`common/FontData`), `lib/fx-model`, `app/ui` font package and the report through the
existing dialogs, message bundles, storage compatibility.

**Dependencies**

IP-01.

**Expected Result**

Opening a project on a machine that measures its font differently says so, names the substitute, and
the pagination change is a known consequence rather than a surprise.

**Technical Considerations**

The fingerprint comes from measurements, not from the file - which is not only what the font rule
allows, it is the better signal: it captures exactly what influences the layout and stays quiet about
differences that do not. The reference set and size must be fixed for all time, or every project
written before a change reports a false mismatch.

`FontData` is written by every existing project, so the new field needs a default and an absent
fingerprint must mean "not recorded", never "mismatch". The `fx-model` skill governs the mirroring.

### IP-02: Design Page Format Model

**Objective**

Give the design everything a page needs.

**Scope**

In scope: `PageFormat` POJO (page width and height, inner, outer, top and bottom margin), line
spacing as a factor per element class, defaults for a document carrying none of these values,
mirrored FX properties and their tests. Out of scope: any UI editing those values, any rendering, any
compatibility handling - `Design.version` stays as it is.

**Affected Areas**

`lib/model` (`project/design`, `common`), `lib/fx-model` (mirrored packages).

**Dependencies**

None.

**Expected Result**

A document carrying no page format opens with the default one; the values are readable and writable
through `DesignProperty`.

**Technical Considerations**

Margins are inner/outer, not left/right, because a printed book needs the gutter and the engine knows
the page number anyway - retrofitting this later touches model, FX model, storage and engine at once.
All values in points as `Double`. Line spacing as a factor survives a font size change, an absolute
value does not. The `fx-model` skill governs the mirroring and the tests.

### IP-24: Optional Parts In The Model

**Objective**

Let an optional part keep its text while it is not part of the book.

**Scope**

In scope: `Book.prolog`, `Book.epilog` and `Book.blurb` becoming parts that are always there instead
of `null`; a switch on each of them saying whether it belongs to the book; mirrored FX properties and
their tests. Out of scope: the tree checkbox, which is IP-23, anything about how a switched off part
is shown, and any compatibility handling - `Book.version` stays as it is.

**Affected Areas**

`lib/model` (`project/book`), `lib/fx-model` (mirrored packages).

**Dependencies**

None.

**Expected Result**

A prolog written, switched off and switched on again carries the same text it had before, and a
document carrying none of the optional parts opens with their defaults.

**Technical Considerations**

The switch belongs on the part, not on the book, so the state travels with the text it describes. It
is the field `included` on `Prolog`, `Epilog` and `Blurb`, declared on each of them rather than in a
shared interface: no caller reaches it polymorphically, and `BookPart` must not gain a switch a
chapter can never use. `Prolog.title` and `Epilog.title` gain the default `""`, because `Book` builds
the three parts itself from now on.

This is what makes the deferral of a part harmless: nothing is deleted, so IP-23 needs no confirmation
dialog and no undo entry that restores lost text.

A document that carries none of the optional parts is read with their defaults, so no migration step
is needed. The `fx-model` skill governs the mirroring and the tests.

### IP-03: Layout Core

**Objective**

Turn design plus text into placed lines with absolute coordinates.

**Scope**

In scope: new module `lib/layouting`; the `TextMetrics` interface and a deterministic implementation
for tests; the module's own style type (family, size, weight, slant, alignment, line spacing, spacing
before and after) and its own alignment enum; one generic text block carrying text and style, with no
role and no notion of what it once was; line breaking against a given column width; horizontal
alignment including justified text; mapping of every placed line back to its source block and
character range; new module `lib/layouting-model` with the builders that turn `Book`, `Design` and
`Meta` into those blocks - title page, copyright page, written part and blurb - and translate
`StyleData` and the line spacing of `Design` into the style type; unit tests of both modules against
the deterministic metrics. Out of scope: pages and page breaks, caching, any toolkit.

**Affected Areas**

`settings.gradle.kts`, new module `lib/layouting` without any dependency of its own, new module
`lib/layouting-model` depending on `lib/ai-ghost-model` and `lib/layouting`, and the implementation of
`TextMetrics` written in IP-01.

**Dependencies**

IP-01, IP-02, IP-24.

**Expected Result**

A part plus a design produce a reproducible sequence of placed lines; the same input always yields
the same numbers.

**Technical Considerations**

The engine owns the measuring interface and knows nothing about who satisfies it - that is what keeps
JavaFX out of a library module while the only real implementation is a JavaFX one.

The engine carries no knowledge of this application's model either. A block is text plus style; the
title page, a heading and a paragraph differ only in which text and which style go in, so they need
neither a type nor a role of their own. Everything that reads `Book`, `Design` or `Meta` lives in
`lib/layouting-model`, which keeps the engine reusable and keeps the model out of the typesetting
code.

The mapping from placed line back to source character range is what lets IP-10 put a caret and IP-18
address a paragraph - it is not an afterthought. Hyphenation is out of scope and changes line breaking
when it arrives, so the breaking step is kept behind an interface.

The result type is plain data with no toolkit type in it. That is what keeps a later export plugin
free of a dependency on `app/ui`, and it costs nothing to honour now.

### IP-04: Pagination And Page Break Policy

**Objective**

Distribute placed lines onto pages the way the printed book does.

**Scope**

In scope: page filling from the page format of IP-02, inner and outer margin depending on odd or even
page, page breaks, the title page as the page the book opens with and the copyright page directly
behind it, an optional part always beginning on a page of its own, the pages of a switched off part
carried as inactive and left out of the page numbering, the blurb as the last sheet outside the
numbering, the empty page at the start and end of the book, a `PageBreakPolicy` interface with a
`NONE` implementation, `DocumentLayout` as the result type, layout of a whole book, snapshot tests of
the resulting page structure. Out of scope: widow and orphan handling
(the policy hook exists, no implementation ships), caching, rendering.

**Affected Areas**

`lib/layouting`.

**Dependencies**

IP-03.

**Expected Result**

`LayoutEngine.layout(design, part)` and `LayoutEngine.layout(design, book)` return a page structure
that every consumer of this feature reads.

**Technical Considerations**

The policy interface is added now although nothing implements it, because widows and orphans change
where a page ends and would otherwise reshape the engine later. Odd and even pages must be counted
across the whole book, so a part laid out alone needs to be told which page it starts on.

A page therefore carries two numbers that must not be confused: its position in the layout, which
every switched off part occupies as well, and its page number in the book, which only an active page
receives. Switching a prolog on renumbers everything behind it without any page appearing or
disappearing - which is exactly why the distinction is in the result type rather than in the caller.

An optional part always starts a new page, so a chapter never begins on the sheet a prolog ended on.
That is a typesetting rule, not a convenience: it also means switching a part on or off can never
reflow the text of a neighbouring part, only renumber it.

The blurb is cover text. It is laid out against the same design, is always the last sheet, and
receives no page number at all.

### IP-05: Incremental Layout And Caching

**Objective**

Keep typing responsive on a book sized document, given that measuring belongs to the FX thread.

**Scope**

In scope: per paragraph layout cache keyed by text, resolved style and column width; invalidation of
exactly the paragraphs that changed; cheap recomputation of page boundaries after an invalidation; the
split between measuring, which needs the FX thread, and arranging, which does not; measurements
proving the behaviour on a synthetic book. Out of scope: UI level debouncing, which belongs to IP-08
and IP-16.

**Affected Areas**

`lib/layouting`, `app/ui` font package.

**Dependencies**

IP-04.

**Expected Result**

Changing one paragraph relayouts that paragraph and re-flows the page boundaries, instead of laying
out the document again, and a long document does not freeze the user interface.

**Technical Considerations**

A design change invalidates everything and must stay correct - the cache key carries the resolved
style for exactly that reason.

The FX thread constraint of IP-01 is the reason this plan matters more than a usual optimisation. Two
things make it bearable: only **words** are measured, so a book of ordinary prose needs far fewer
distinct measurements than it has characters; and once the words of a paragraph are in the cache, the
arranging of lines and pages is pure arithmetic that may run anywhere. Laying out a whole book is
therefore a warm-up on the FX thread followed by work that does not block it.

This is the plan that decides whether the feature feels fast; it comes before the surfaces, not after
them.

### IP-06: Layout Regression Harness

**Objective**

Make the page structure something the build defends, and prove that both surfaces agree about it.

**Scope**

In scope: checked in golden page structures for a set of representative projects (short part, long
part, justified text, several designs, odd and even pages), all produced against the deterministic
metrics of IP-03; a test asserting that the break positions `PaperFlowView` shows are the pages
`PaperPageView` renders; a Gradle task running both; documentation of how a golden file is
regenerated.

**Affected Areas**

`lib/layouting` test source set, `app/ui` test source set, CI (`ci-pipeline` skill).

**Dependencies**

IP-04 for the golden page structures, IP-07 and IP-08 for the comparison of the two surfaces. The
golden files can therefore be built as soon as the engine paginates; the surface comparison waits for
both surfaces to exist.

**Expected Result**

A change altering the page structure fails the build unless the golden files are updated on purpose,
and writing surface and preview cannot drift apart unnoticed.

**Technical Considerations**

The golden files are snapshots of numbers, not images - a numeric diff says which line moved, an
image diff only says that something did.

They are produced against the deterministic metrics, never against a font that happens to be installed
on the build machine; otherwise the build result differs per developer and per CI runner. That is why
IP-03 ships that implementation, and it is also why this plan needs no font of its own.

### IP-07: Paper Page View

**Objective**

Show a document layout as exactly rendered sheets.

**Scope**

In scope: `PaperPageView` component (MVVM FX), sheet background, shadow, page gap, painting of every
placed line at its coordinates, the greyed out look of a page belonging to a switched off part, the
hard edge that sets the blurb off from the book, page numbers on the active pages only, zoom and fit
to width, scrolling, scroll to a given page or block, CSS under `styles/component`, headless TestFX
tests. Out of scope: editing, AI, virtualisation of a
whole book, which belongs to IP-16.

**Affected Areas**

`app/ui` component package, styles, message bundles, `module-info.java`.

**Dependencies**

IP-04.

**Expected Result**

A document layout is displayed as printed pages; nothing on it is editable.

**Technical Considerations**

This component decides nothing about typography - it only paints coordinates, which is what makes it
agree with the writing surface by construction rather than by luck. Painting is kept behind a small
drawing interface so the same routine could later be driven by an export; that costs nothing now.

It draws with the same `javafx.scene.text.Font` the engine measured with, so there is no second font
implementation that could place a glyph elsewhere.

### IP-08: Paper Flow View

**Objective**

Build the writing sheet: page geometry and true break positions around native text controls.

**Scope**

In scope: `PaperFlowView` with the page width, the margins and the sheet look of IP-07; a slot per
block that a native control is placed into; the sheet gap where a break falls between two paragraphs;
the marker line with the page number where a break falls inside a paragraph; the greyed out look of a
switched off part; the hard edge before the blurb, which is a different thing from a page break and
must not look like one; debounced recomputation of break positions; tests. Out of scope: the text controls and their behaviour, which belong to
IP-10; splitting a control at a break, which is IP-21.

**Affected Areas**

`app/ui` component package, styles, message bundles, icons if the marker carries one.

**Dependencies**

IP-04.

**Expected Result**

A part is shown on sheets of the correct geometry with break positions taken from the engine, ready
to receive editable blocks.

**Technical Considerations**

The break marker is drawn on an overlay above the block, so the control below stays one piece.
Debouncing belongs on the break recomputation, not on the text - a typed character must appear at
once, a marker may move a moment later.

The native control wraps its own text, but it wraps it with the same JavaFX text stack the engine
measured with, so its line breaks and the engine's agree except for what the control's own insets and
padding add. Those insets have to be taken out of the column width the engine is given, or the sheet
wraps a word earlier than the printed page does.

### IP-09: Undo And Redo Infrastructure

**Objective**

One place that takes back a change, whoever caused it.

**Scope**

In scope: an undo stack over model changes with named, coalescable entries; coalescing of consecutive
typing; scope per project; the actions and their keyboard shortcuts; tests. Out of scope: wiring it
into the surfaces, which the consuming plans do.

**Affected Areas**

`app/ui` (controller or a small support package), `MainWindow` menu and tool bar, icons, message
bundles.

**Dependencies**

None.

**Expected Result**

A recorded change is taken back and reapplied, and consecutive typing collapses into one step.

**Technical Considerations**

It has to be built before the first surface records into it, otherwise editing and AI each grow their
own mechanism. Structural operations (IP-11) and applied AI results (IP-18, IP-19) are ordinary
entries on the same stack - that is the whole point of doing it separately.

### IP-10: Book Part Writing Surface

**Objective**

Make the sheet the place the text is written.

**Scope**

In scope: `BookPartEditor` placing an auto-growing text control per block into `PaperFlowView`;
caret and typing; focus movement between blocks with the arrow keys; bidirectional binding to
`BookPartProperty` for heading, heading lines and paragraphs; plain text paste; recording into the
undo stack; the title page and the copyright page bound to `BookProperty` and `MetaProperty`; the
prolog, chapter and epilog variants through the one `BookPartProperty` they share; the blurb variant,
which has no heading; writing on a part that is switched off; tests. Out of scope: the tree checkbox,
which is IP-23; structural operations, AI, inspector.

**Affected Areas**

`app/ui` component package, `lib/fx-model` usage of `BookPartProperty`, styles, message bundles.

**Dependencies**

IP-08, IP-09.

**Expected Result**

Selecting a chapter opens its text on the sheet and every keystroke lands in the model.

**Technical Considerations**

Rich text on the clipboard is reduced to plain text, otherwise the ownership of the appearance by the
design breaks immediately. Editing must survive a design change while the caret sits inside a
paragraph, so the caret is stored as a paragraph index plus a character offset, never as a coordinate.

Prolog, chapter and epilog are one editor, not three: they are the same `BookPart`, so the editor
binds `BookPartProperty` and the differences stay in what the tree hands it. The blurb is the
exception that has to be built - no heading, one prompt instead of two - and it is the reason this
plan names variants at all.

A switched off part stays writable. Greying it out says that it is not in the book, not that it is
locked - a prolog that could only be written after being switched on would make the preserved text
half a feature.

The title page and the copyright page are the one place the paper shows data that is not the
manuscript: the author comes from `Meta`, and the two pages are read-only on the sheet, because both
values are edited in the inspector.

### IP-11: Paragraph Structure Operations

**Objective**

Let the paragraph list be shaped from the sheet.

**Scope**

In scope: ENTER splitting a paragraph at the caret, BACKSPACE at the start joining with the previous,
deleting an empty paragraph, moving a paragraph up and down, creating the first paragraph of an empty
part; caret restoration after every operation; each operation as one undo entry; tests. Out of scope:
AI, drag and drop reordering unless it falls out for free.

**Affected Areas**

`app/ui` `BookPartEditor`, undo stack, message bundles.

**Dependencies**

IP-10.

**Expected Result**

The paragraph list of a part is fully editable from the sheet, and every operation is undoable in one
step.

**Technical Considerations**

Every operation changes the block list, which changes the layout, which moves the caret target - the
three have to be handled as one transaction. Splitting mid-paragraph is the case that exposes an
off-by-one in the character range mapping of IP-03, so it deserves its own tests.

### IP-12: Inspector Shell And Content Sections

**Objective**

Move everything that is not printed text off the sheet into one panel.

**Scope**

In scope: `Inspector` component with fixed, always identically named collapsible sections; the book
section carrying title and title appendix taken over from `BookEditor`, plus author and copyright from
`Meta`, which the title page and the copyright page show; the part section carrying chapter name and
the two prompts; empty states when a section does not apply to the selection;
collapse state; tests. Out of scope: the design sections, the AI actions, the arrangement in
`Editor`.

**Affected Areas**

`app/ui` (`BookEditor` is reduced to sections of the inspector), styles, message bundles.

**Dependencies**

None.

**Expected Result**

Prompts and part data are edited beside the sheet, and the centre carries text only.

**Technical Considerations**

Sections stay fixed and identically named on purpose: the inspector mixes part scoped and project
scoped data, and a panel whose content silently changes shape makes it unclear what is being edited.
The existing bidirectional binding behaviour of `BookEditor` must be preserved as it moves.

### IP-13: Design Style Sections

**Objective**

Edit the typography of the book and see the effect while editing it.

**Scope**

In scope: inspector sections for the styles of title, chapter title, chapter title appendix and body
text - family chosen from the families reported by IP-01, size, bold, italic, alignment, line
spacing; a preview of a family in the picker; the marking of a family that is not installed here;
live effect on the sheet; tests. Out of scope: page format and margins, which are IP-14.

**Affected Areas**

`app/ui` inspector and font package, `DesignProperty` binding, message bundles, styles.

**Dependencies**

IP-01, IP-02, IP-12.

**Expected Result**

Changing a style value changes the sheet at once, without reopening the project.

**Technical Considerations**

The section writes the same `DesignProperty` the layout reads, which is what makes the live update
work without extra plumbing. Only families the machine actually has are offered, because a family
that cannot be resolved cannot be measured and therefore cannot be laid out.

Rendering each family in its own face makes the picker expensive on a machine with many fonts, so the
sample is built lazily per visible row.

### IP-14: Project Settings Dialog

**Objective**

Set the values that are chosen once per project, away from the writing flow.

**Scope**

In scope: a dialog on the existing project settings icon holding page format with presets, the four
margins in millimetres, and the empty page at start and end; validation against impossible geometry;
tests. Out of scope: styles, which live in the inspector.

**Affected Areas**

`app/ui` dialog package, `DesignProperty`, message bundles, styles, icons.

**Dependencies**

IP-02.

**Expected Result**

The page geometry of a project is set in one place, and the sheet takes the new geometry over.

**Technical Considerations**

Millimetres are shown, points are stored - the conversion lives in the dialog only. A margin sum
exceeding the page must be refused, otherwise the engine gets a negative column width.

### IP-15: Editor Arrangement And Tree Routing

**Objective**

Bring tree, sheet and inspector into one arrangement and route every tree node to it.

**Scope**

In scope: rework of `EditorView.fxml` and `EditorViewModel`; exhaustive routing of every
`ProjectListItem` to the surface and the inspector sections it opens, including the nodes whose part
does not exist yet; three zone layout with a collapsible inspector; view state (split positions,
inspector collapsed) persisted in `Preferences`; removal of the placeholder; tests. Out of scope: the
mode switch, AI.

**Affected Areas**

`app/ui` `Editor*`, `lib/model` and `lib/fx-model` for the preferences view state, message bundles,
styles.

**Dependencies**

IP-11, IP-12.

**Expected Result**

Every node of the tree opens the matching surface, and the tree itself is unchanged.

**Technical Considerations**

`ProjectList` keeps its API - all routing lives in `EditorViewModel`, as an exhaustive `when` over
`ProjectListItem`, so a node that gains a meaning later is a compiler error rather than a search
through the code. The view state goes into
`Preferences`, never into the project document, so two people working on one project do not overwrite
each other's window layout. The preferences change follows the `fx-model` rule.

### IP-16: Writing And Preview Modes

**Objective**

Turn the same content into a preview of the whole book.

**Scope**

In scope: the `Schreiben` / `Vorschau` switch; preview rendering the whole book through
`PaperPageView`; page virtualisation so only visible sheets exist in the scene graph; laying out a
whole book without freezing the window; scrolling to the part selected in the tree and keeping the
position across a mode change; page count and current page in the status bar; the mode remembered in
`Preferences`; tests.

**Affected Areas**

`app/ui` `Editor*`, `PaperPageView`, `MainWindow` status bar and tool bar, icons, message bundles.

**Dependencies**

IP-05, IP-07, IP-15.

**Expected Result**

The whole book is previewable in its design, scrolls smoothly, and the mode change keeps the reading
position.

**Technical Considerations**

This is where the FX thread constraint of IP-01 becomes visible to the user: the whole book has to be
measured before its page count is known, and measuring belongs to the FX thread. The split of IP-05 -
measure words on the FX thread, arrange anywhere - is what keeps the window responsive; a progress
indication for the first layout of a long book has to be planned for rather than hoped away.

The position is kept as a paragraph reference, not as a scroll offset, because the two modes have
different geometry.

### IP-17: AI Action Port

**Objective**

Give the UI something to call before a provider integration exists.

**Scope**

In scope: the action interface in `lib/ai-ghost-ai` (`rewrite`, `expand`, `shorten`,
`generatePart`), its request and result types including a streaming result, cancellation, error
reporting, the character and token limits reusing `TokenUtils`, and a deterministic stub
implementation for development and tests. Out of scope: any real provider, which is a feature of its
own.

**Affected Areas**

`lib/ai-ghost-ai`, `app/ui` dependency wiring.

**Dependencies**

None.

**Expected Result**

An AI action is callable, cancellable and testable without a network.

**Technical Considerations**

Streaming is in the interface from the start, because retrofitting it changes every caller. A result
of several paragraphs has a defined split rule - a blank line ends a paragraph - so a generated
chapter does not land in one paragraph.

### IP-18: AI Actions On Paragraph And Heading

**Objective**

Put the AI on the sheet where its unit of work is.

**Scope**

In scope: `AiActionBar` floating at the focused paragraph or heading; rewrite, expand and shorten;
the busy state of the affected block while the action runs; cancelling; the result replacing the text
as one undo entry; error reporting through the existing dialogs; tests. Out of scope: generating a
whole part.

**Affected Areas**

`app/ui` `BookPartEditor`, new `AiActionBar`, `lib/ai-ghost-ai`, icons, message bundles, styles.

**Dependencies**

IP-10, IP-17.

**Expected Result**

A paragraph is rewritten from the sheet, and the result is taken back with one undo.

**Technical Considerations**

A paragraph level result replaces directly because undo already exists - a comparison step here would
cost more than it protects. The action must not block the FX thread; only the affected block is
marked busy while the rest of the sheet stays usable.

### IP-19: AI Part Generation With Provisional State

**Objective**

Generate a whole part from its prompts without destroying what is there.

**Scope**

In scope: the part level action in the inspector, fed by content and style prompt; streaming the
result onto the sheet as it arrives; the provisional state of the generated blocks with its own
marking and an accept-or-discard bar; discarding restoring the previous text; accepting recorded as
one undo entry; tests. Out of scope: paragraph level actions.

**Affected Areas**

`app/ui` inspector and `BookPartEditor`, `lib/ai-ghost-ai`, icons, message bundles, styles.

**Dependencies**

IP-12, IP-17.

**Expected Result**

A part is generated from its prompts, appears while it is written, and is accepted or discarded
without a dialog.

**Technical Considerations**

Generating a part overwrites a lot, which is why it gets a provisional state instead of a direct
replacement - the size of the destruction, not the origin of the text, decides how explicit the
confirmation is. A provisional part is not saved; leaving the part or closing the project has to
resolve the state.

Streamed text arrives off the FX thread and is both measured and shown on it, so the arriving chunks
have to be handed over in batches rather than per token, or the sheet relayouts itself to a standstill.

### IP-21: In-Paragraph Sheet Split

**Objective**

Show a real sheet gap even when the page break falls inside a paragraph.

**Scope**

In scope: splitting the text control of a paragraph at the break into two controls that stay one
logical paragraph; caret movement across the split; selection across the split; undo across the
split; recomputing the split while typing without losing focus; tests. Out of scope: everything else -
this plan is optional and is only started if the marker line of IP-08 proves insufficient in use.

**Affected Areas**

`app/ui` `PaperFlowView`, `BookPartEditor`.

**Dependencies**

IP-11.

**Expected Result**

Writing looks like a stack of sheets in every case, not only when the break falls between paragraphs.

**Technical Considerations**

The split point moves with every keystroke, so controls are merged and split while the caret sits
inside them - that, not the splitting itself, is the hard part. This is deliberately last: the feature
is complete and usable without it, and the effort is only justified once the marker line has been
lived with.

### IP-23: Optional Book Parts In The Tree

**Objective**

Let the user decide which optional parts belong to the book, from the place the book is structured.

**Scope**

In scope: a checkbox on the prolog, epilog and blurb nodes of `ProjectList`, bound to the switch of
IP-24; the paper following the change at once - greying, page numbering, page count; the checkbox
staying in step with a switch changed elsewhere; the node making clear that unticked means "not in the
book", not "empty"; each change as one undo entry; tests. Out of scope: the writing surfaces
themselves, which IP-10 delivers.

**Affected Areas**

`app/ui` `ProjectList`, `ProjectListCell`, `ProjectListItem`, `Editor*`, `lib/fx-model` usage of
`BookProperty`, message bundles, styles, `styles/component/tree-view.css`.

**Dependencies**

IP-15, IP-24.

**Expected Result**

Prolog, epilog and blurb are switched into and out of the book from the tree, the paper greys and
renumbers accordingly, and no text is ever lost by it.

**Technical Considerations**

This is the one plan that changes the project tree, which every other plan of this feature leaves
alone. The change is deliberately narrow: the tree keeps its structure and its `selectedItem` API, and
gains a checkbox on exactly three nodes. A `CheckBoxTreeItem` applies its tick to the whole subtree by
default, which is not wanted here - the three nodes carry no children, but the behaviour has to be
constrained rather than inherited.

Because IP-24 keeps the text, clearing a tick destroys nothing: there is no question to ask and no
text to restore. It is still one undo entry, because renumbering a book by accident should be taken
back with the same key as everything else.

Ticking a prolog renumbers every page behind it, which is exactly the distinction IP-04 draws between
a page's position and its page number; the paper has to follow without the project being reopened.

## 8. Dependency Graph

```text
IP-01 ─┬─> IP-22
       ├─> IP-13   (with IP-02, IP-12)
       └─┐
IP-02 ─┬─┴─> IP-03 ──> IP-04 ─┬─> IP-05 ──────────────┐
IP-24 ─┤    │                 │                       │
       └─> IP-14              ├─> IP-07 ─┬────────────┤
                              │          ├─> IP-06    │
                              └─> IP-08 ─┘            │
                                    │                 │
                                    └─> IP-10 ────────┼──> IP-18   (with IP-17)
                                    (with IP-09)      │
                                              └─> IP-11 ─┬─> IP-15 ─┬─> IP-16
                                                         │          └─> IP-23   (with IP-24)
                                                         └─> IP-21
IP-09 ──> IP-10, IP-18, IP-19
IP-12 ─┬─> IP-13
       ├─> IP-15
       └─> IP-19   (with IP-17)
IP-17 ─┬─> IP-18
       └─> IP-19
IP-05, IP-07, IP-15 ──> IP-16
```

Independent starting points: **IP-01**, **IP-02**, **IP-24**, **IP-09**, **IP-12**, **IP-17** can all
begin at once.

## 9. Risks and Open Questions

* **Measuring belongs to the FX thread.** A `javafx.scene.text.Text` node may only be used there, so
  every measurement the engine needs is an FX thread operation. IP-05 answers it by measuring words
  rather than paragraphs and by separating measuring from arranging, but laying out a long book for
  the first time stays a cost that IP-16 has to show rather than hide. This is the central technical
  risk of the feature and needs measuring early, in IP-05, not in IP-16.
* **Default page format and margins** need a decision before IP-02; the proposal is A5 with presets
  and 20 / 15 / 15 / 20 mm inner, outer, top, bottom.
* **The default font of a new project** is open. `FontData` defaults to `Arial` today, which is not
  installed everywhere; a default resolving through the fallback chain of IP-01 is needed instead of
  a hard coded name.
* **The insets of the native text control** shift its wrapping against the engine's. IP-08 takes them
  out of the column width; if a control turns out to wrap differently for another reason, the sheet
  and the preview disagree on a word and IP-06 catches it.
* **The correspondence is per machine.** With no font shipped, a project opened elsewhere may resolve
  another font or measure it differently and therefore paginate differently. IP-22 makes this
  visible; it cannot make it go away.
* **The project tree changes after all.** The original constraint was that it stays untouched; the
  checkbox for the optional parts supersedes it for those three nodes. Everything else about the tree -
  its structure, its cells, its `selectedItem` API - stays as it is, and IP-23 is the only plan that
  touches it.
* **Widows, orphans, hyphenation** are excluded; the hook exists, no implementation ships.
* **Resolving a provisional AI part** on part change or project close (IP-19) is undecided.
* **Two new Gradle modules** (`lib/layouting`, `lib/layouting-model`) require a check against the
  `ci-pipeline` skill.

### Decisions taken

These were open while the plan was written and are settled; they are recorded here because the plans
build on them.

* **Page format** is A5 (148 x 210 mm) by default, with presets for A4, 12,5 x 19 cm, 13,5 x 21,5 cm
  and 6 x 9 inch. Default margins are 20 mm inner, 15 mm outer, 15 mm top, 20 mm bottom.
* **Front matter** is the title page carrying title, further title lines and the author, followed
  directly by the copyright page carrying author and copyright.
* **The blurb** is always the last sheet, set off by a hard edge rather than by the dashed page break
  marker, and carries no page number.
* **Optional parts** always begin on a page of their own, keep their text when switched off, are shown
  greyed out and stay writable.
* **The metrics fingerprint** of IP-22 is taken over the printable ASCII range plus the German
  umlauts and the sharp s, measured at 12 pt, together with ascent, descent and leading. The set and
  the size are fixed from that point on - a later change would report a mismatch for every project
  written before it.

### Deliberately out of scope

* **Export in any form.** PDF export is decided to be an own feature: implemented as a plugin, on
  Apache PDFBox. It is not planned here and no plan of this feature produces it. What this feature
  owes it is a toolkit independent `DocumentLayout`, which IP-03 and IP-04 deliver.

  Anything a font file has to be read for belongs to that feature and is PDFBox's job there - which
  is why nothing in this feature reads one. `TextMetrics` is an interface with two implementations
  after IP-01 and IP-03, one measuring through JavaFX and one deterministic for tests; a third, backed
  by PDFBox, slots in later without touching the engine. Whether the export then paginates through
  JavaFX measurements or through PDFBox's own is a decision of that feature, and the seam is there for
  either answer.

* **Plugin infrastructure.** `ai-ghost-plugin-api` carries only `ProjectPart` and `ProjectPartInfo`
  today; there is no plugin interface, no loader and no service registration, and the application is
  shipped as a closed jlink image. A plugin based export therefore needs that infrastructure built
  first - which is a feature of its own and a prerequisite of the export feature, not of this one.

## 10. Feature Completion Criteria

* The title page, prolog, every chapter, epilog and blurb can be written completely inside the
  application; the hard coded "Not implemented yet." placeholder is gone.
* The title page carries the author, and the copyright page follows directly behind it.
* Prolog, epilog and blurb are switched into and out of the book through their checkbox in the tree;
  a switched off part keeps its text, is shown greyed out, is left out of the page numbering and can
  still be written on.
* The blurb is the last sheet, set off by a hard edge, and carries no page number.
* A document written before this feature opens with exactly the parts it used to have.
* Text is written in the typography, margins and page structure of the book, with page breaks marked
  at the position the printed book breaks at.
* The page a paragraph falls on is the same in the writing surface and in the preview, and the build
  fails when the two drift apart.
* The font of an element is chosen from the families installed on the machine; the application ships
  no manuscript font and opens no font file.
* Opening a project whose font is missing or measures differently reports it and names the substitute
  in use.
* Changing a design value changes the appearance of the open text without reopening the project.
* Writing in a book sized document stays responsive, and the first layout of a long book does not
  appear as a frozen window.
* Prompts, part data and design are reachable beside the sheet and never interrupt the text.
* Every text change, structural change and applied AI result can be undone and redone.
* An AI action can be triggered for a paragraph, a heading and a whole part; a paragraph result is
  applied directly, a part result is accepted or discarded.
* The layout result carries no toolkit type, so a later export plugin can consume it unchanged.
* JavaFX appears in no module but `app/ui`.
* The project tree keeps its structure and its selection API; the checkbox on the three optional
  nodes is the only thing this feature added to it.
* Build and tests are green, documentation and changelog are updated per the `project-docs` skill.
