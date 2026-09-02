# Feature Status: Paper Writing Surface

Status: IN_PROGRESS

## Implementation Plans

| ID    | Implementation Plan                       | Status      |
|-------|-------------------------------------------|-------------|
| IP-01 | Font Discovery And Text Measuring         | COMPLETED   |
| IP-22 | Font Identity And Substitution Reporting  | COMPLETED   |
| IP-02 | Design Page Format Model                  | COMPLETED   |
| IP-24 | Optional Parts In The Model               | COMPLETED   |
| IP-03 | Layout Core                               | COMPLETED   |
| IP-04 | Pagination And Page Break Policy          | COMPLETED   |
| IP-25 | Renderer Library Module                   | COMPLETED   |
| IP-26 | Font And Measuring Migration              | COMPLETED   |
| IP-05 | Incremental Layout And Caching            | NOT_STARTED |
| IP-06 | Layout Regression Harness                 | NOT_STARTED |
| IP-07 | Paper Page View                           | COMPLETED   |
| IP-08 | Paper Flow View                           | COMPLETED   |
| IP-27 | Library Styling And Theming API           | NOT_STARTED |
| IP-28 | Standalone Reuse And Documentation        | NOT_STARTED |
| IP-09 | Undo And Redo Infrastructure              | COMPLETED   |
| IP-10 | Book Part Writing Surface                 | NOT_STARTED |
| IP-11 | Paragraph Structure Operations            | NOT_STARTED |
| IP-12 | Inspector Shell And Content Sections      | COMPLETED   |
| IP-13 | Design Style Sections                     | COMPLETED   |
| IP-14 | Project Settings Dialog                   | COMPLETED   |
| IP-15 | Editor Arrangement And Tree Routing       | NOT_STARTED |
| IP-16 | Writing And Preview Modes                 | NOT_STARTED |
| IP-17 | AI Action Port                            | COMPLETED   |
| IP-18 | AI Actions On Paragraph And Heading       | NOT_STARTED |
| IP-19 | AI Part Generation With Provisional State | NOT_STARTED |
| IP-21 | In-Paragraph Sheet Split                  | NOT_STARTED |
| IP-23 | Optional Book Parts In The Tree           | NOT_STARTED |

## Overall Progress

56%

## Notes

IP-01, IP-22, IP-02, IP-24, IP-03, IP-25, IP-14, IP-26, IP-17, IP-04, IP-07, IP-08 and IP-13 are
implemented; IP-06, IP-10, IP-18, IP-19 and IP-27 are unblocked.

IP-13 was built narrower than planned: the family catalogue restriction, the per-family sample text
and the uninstalled-family marking already existed in `StyleDataEditor` (built for IP-14's
`BookPartPageDesignSettings`), so this plan's actual work was a third, always-live `TitledPane`
section "Design" in `Inspector`, next to "Book" and "Chapter", holding four reused `StyleDataEditor`
instances (title, chapter title, chapter title appendix, body text) bound straight to
`project.designProperty.titlePageProperty.titleStyleProperty` and the three matching properties of
`chapterPageProperty`. Unlike the other two sections it does not follow the project tree selection,
only whether a project is bound at all (`InspectorViewModel.designAvailable`); the four editors are
held by `InspectorView` and forwarded into `InspectorViewModel`, the same way
`BookPartPageDesignSettingsView` forwards its three. `StyleDataEditor` gained one small addition,
`release()`, delegating to its already-internal `StyleDataEditorViewModel.release()`, so the section
drops its bindings cleanly when the project is closed instead of leaving them on an orphaned design.
`:app:ai-ghost-ui:compileKotlin`, `compileTestKotlin` and `test --tests "*Inspector*"` are green. The
plan file was removed on completion; this note is the only record. No other plan is unblocked by it.

IP-08 was built as planned: `PaperFlowView` (a plain `javafx.scene.control.Control` with a
`SkinBase`-derived `PaperFlowViewSkin`, no FXML) landed in `lib/layouting-fx`, package
`org.pcsoft.app.aighost.layouting.fx.paper`, beside `PaperPageView`, plus `PaperFlowListener` (six
default-empty callbacks: `onTextChanged`, `onCaretMoved`, `onFocusChanged`, `onSplitRequested`,
`onMergeRequested`, `onRemoveRequested`) and a neutral default stylesheet
(`src/main/resources/.../paper/paper-flow-view.css`). One native `TextArea` per block is built by
grouping the laid out lines of a `DocumentLayout` by `LaidOutLine.blockIndex` and rejoining their
text with a single space; the feature plan's risk list now names that a hard line break inside a
block would not survive this round trip, since none exists in the model today. A page break landing
between two blocks renders a real gap the size of `PaperPageViewSkin`'s own page gap; a break landing
inside one block draws a dashed `paper-flow-view-break-mark` line with the target page number,
positioned by the character-offset fraction of the break, since a `TextArea`'s own text layout is
private and reaching into it would be the toolkit reflection this codebase never does. The reported
`columnWidthProperty` subtracts the text control's own insets from `PageGeometry`'s content width
(`width - innerMargin - outerMargin`). Every user action is only reported, never applied, through
`PaperFlowListener` - shaped after IP-17's `AiAction` request/callback port rather than several
separate listener lists, since it is the strongest existing "report intent, never mutate" precedent in
this codebase. Enter is intercepted into a split request instead of a newline, since a block is a
paragraph, not a multi-line field; Backspace at a block's start and Delete at its end become merge
requests; pasted content always goes in through `insertText` with the clipboard's plain string only.
Column width and break marks recompute debounced 100 ms behind a `PauseTransition` on a resize; text
itself is never debounced. Rebuilding a block's controls from a fresh `DocumentLayout` first memorises
which block carried focus and its caret position and restores both afterwards, so handing in a newly
computed layout - as IP-10's consumer will after applying a reported change - never interrupts typing.
One gap closed along the way, affecting IP-07 as well: the `.paper` package of `lib/layouting-fx` had
never been added to `exports` in `module-info.java` since IP-07 landed, because nothing outside the
module read it yet; it is exported now that a consumer (`app/ui`, from IP-10 on) needs both views.
The component registers no listener outside its own node subtree, so `fx-component-lifecycle`'s
`showingBinding()` pattern does not apply, the same as `PaperPageView`. The CHANGELOG and MkDocs stayed
untouched, since nothing here is wired into an `app/ui` screen yet and so nothing is visible to an end
user. `:lib:ai-ghost-layouting-fx:build` is green, with 8 new developer tests for `PaperFlowViewTest`.
IP-10 is now unblocked together with the already completed IP-09; IP-06 and IP-27 are now unblocked as
well.

IP-07 was built as planned: `PaperPageView` (a plain `javafx.scene.control.Control` with a
`SkinBase`-derived `PaperPageViewSkin`, Canvas-based drawing, no FXML) landed in `lib/layouting-fx`,
package `org.pcsoft.app.aighost.layouting.fx.paper`, alongside a `PagePainter` interface with a
`DefaultPagePainter` implementation, and a neutral default stylesheet
(`src/main/resources/.../paper/paper-page-view.css`). Two gaps found during exploration were closed
with the user before implementation: `Page`/`DocumentLayout` carry no page width/height, so
`PaperPageView` takes its own `pageGeometryProperty` (type `PageGeometry` from `lib/layouting`)
alongside `documentLayoutProperty`, rather than widening the already-completed `Page`; and the "hard
edge" of an inactive page is derived by comparing a page's `active` flag against its immediate
neighbour's in `DocumentLayout.pages`, rather than adding a new field to `Page`. Both leave IP-04
untouched. A justified line (`LaidOutLine.wordSpacing > 0`) is drawn word-by-word to honour the exact
inter-word gap the engine computed; every other line is drawn with a single `fillText` call. Drawing
resolves its `javafx.scene.text.Font` the same way `JavaFxTextMetrics` measured it -
`FontDescription(style.family, style.size.toInt(), style.bold, style.italic)` through
`FontResolver.font(...)`. Virtualisation uses a `ScrollPane` over a full-height `Pane`; a `Canvas` per
page exists only while that page intersects the viewport plus a 400px buffer, recomputed on scroll,
resize and zoom. Style classes (`paper-page-view`, `paper-page-view-scroll-pane`,
`paper-page-view-container`, `paper-page-view-sheet`, `paper-page-view-page-inactive`) mark the chrome
for later override per IP-27; page numbers and the inactive/active boundary are painted, not
CSS-selectable, since a caller swaps the `PagePainter` for a different look instead. The component
registers no listener outside its own node subtree, so `fx-component-lifecycle`'s `showingBinding()`
pattern does not apply. The CHANGELOG and MkDocs stayed untouched, since nothing here is wired into an
`app/ui` screen yet and so nothing is visible to an end user. `:lib:ai-ghost-layouting-fx:build` is
green. IP-08 stays unblocked as before; IP-06 and IP-27 move a step closer, still waiting on IP-08.

IP-04 was built as planned, widened by one explicit user request: the odd/even margin swap only
applies when a new project setting, "Spiegelnde Ränder" (`PageFormat.mirroredMargins`, default
`false`), is turned on - without it every page keeps the same inner/outer margin. The field was added
to `PageFormat` in `lib/model` and mirrored into `PageFormatProperty` in `lib/fx-model` per the
`fx-model` skill, with full property tests; no UI checkbox was wired yet, since that belongs to the
project settings dialog, not to this plan. The title page and the copyright page carry no page number
on the request as well.

`lib/layouting` gained `PageGeometry` (a model-independent mirror of `PageFormat`, the same way
`TextStyle` mirrors a stored style), `Page` and `DocumentLayout`, the `PageBreakPolicy` interface with
its `NonePageBreakPolicy` implementation (greedy, no widow/orphan avoidance, no look-ahead), and
`LayoutEngine` with `layout(...)` for a single part and `layoutBook(...)` for a whole book across
parts. `LayoutEngine` keeps `position` (physical page position, including inactive pages) and
`pageNumber` (`null` where a page is unnumbered) strictly apart, resolves `leftMargin`/`rightMargin`
from `mirroredMargins` and the page's physical position (position 0 = recto), and honours the two
blank-page switches (`Design.startWithEmptyPage`/`endWithEmptyPage`) at the start and end of a book.
A part is guaranteed to start on a page of its own structurally, since `layoutBook` never continues one
part's lines onto the tail of the previous part's last page. `lib/layouting-model` gained
`PageGeometryTranslation`, translating a stored `PageFormat` into a `PageGeometry`, mirroring
`StyleTranslation`. No dependency was added to `lib/layouting` itself, keeping it free of the
manuscript model as before.

The README's `lib/ai-ghost-layouting` row was widened to name pagination alongside line breaking and
alignment; the CHANGELOG stayed untouched, since nothing produced by this plan is visible in the
running application yet - the new preference has no UI control until a later plan wires one in.

IP-17 was built as an interface only, per an explicit constraint clarified by the user: no stub and no
real AI interaction ship anywhere in this feature, not even for testing. `lib/ai` (`ai-ghost-ai`) gained
package `org.pcsoft.app.aighost.ai.action` with `AiActionRequest` (sealed: `Rewrite`, `Expand`,
`Shorten`, `GenerateChapter`), `AiAction` (the port, `execute(request, callback): AiActionHandle`, no
implementation - carries a `TODO` pointing at the future plugin-based provider feature),
`AiActionCallback` (`onChunk`/`onComplete`/`onError`/`onCancelled`), `AiActionHandle` (`cancel()`),
`AiActionError` (sealed: `LimitExceeded`, `Cancelled`, `Failed`), the standalone `AiActionLimits.check`
(character limits from `Preferences.Ai`, via Arrow `Either`, informed by `TokenUtils` for the reported
token estimate) and `ParagraphSplitter` (blank line ends a paragraph, folds a surviving line break into
a space, drops empty paragraphs). `lib/ai` gained a dependency on `lib/model` (`ai-ghost-model`, for
`Preferences.Ai`) and on Arrow (`arrow-core`, for `Either`), both `api` since they appear in the port's
public signatures. Only the two pure, implementation-independent pieces - the limit check and the
paragraph rule - are tested; there is nothing else to test since no `AiAction` implementation exists.
IP-18 and IP-19 were adjusted to match: their action call sites stop at an open `TODO` instead of
calling a stub. The Feature Plan itself was corrected in the same pass - architecture, plan overview,
out-of-scope section, risks and completion criteria all now state that no AI provider, stub or real,
ships with this feature; it arrives only through a later, dedicated plugin-system feature.

IP-14 was built wider than first planned, on the user's request: a master-detail dialog with a
`ProjectSettingsTree` (root hidden) on the left and the section editor on the right. Only the
`General` section is real (`GeneralSettings`: page-size presets, four margins in millimetres, the two
empty-page flags), bound to a working-copy `DesignProperty`. The `Design` node and its four children
are `PlaceholderSettings`; their style editors stay with IP-13. No new model was added - the design
POJOs for prolog/epilog/blurb and a separate title appendix are still open and deferred. The dialog
keeps a deep-copied working project; OK and APPLY write the page geometry and the two flags back,
CANCEL / ESCAPE discard. Buttons are `OK`, `CANCEL`, `APPLY` (`DialogButtons.OK_CANCEL_APPLY`), APPLY
consumed so it stores without closing, OK and APPLY disabled while the input cannot be stored. The
menu item and tool bar button in `MainWindowView` are wired. Styling landed in a new
`styles/component/project-settings.css` (registered in `AiGhostTheme`) instead of `dialog.css`,
because the combo box, the check box and the plain separator are first used here. The plan files were
removed on completion; this table is the only record.

Adjusted afterwards on the user's request: the page-format editor moved from the `General` node onto
the `Design` node, `General` is now an empty placeholder and the dialog opens on `Design`; the
`Design` branch gained two more placeholder children, `Title page` and `Copyright page`, ahead of
`Epilog`. `ProjectSettingsSection` was reshaped from an `enum` into a `sealed interface` mirroring
`ProjectListItem`, and `ProjectSettingsTreeView` now builds its tree explicitly with a named
`ProjectSettingsTreeCell` like `ProjectListView`. Progress is unchanged.

IP-22 was cut along the module boundary rather than kept in `app/ui` as a whole. The measurement is
pure JavaFX and knows no type of this application, so `FontFingerprint` and `FontFingerprints` live
in `lib/layouting-fx`, which exports its first package with them; `lib/layouting` stays untouched,
because the engine never sees a fingerprint. `app/ui` keeps what is application bound: the
translation onto `FontMetricsData`, the comparison in `FontIdentity`, the walk over the design in
`FontIdentityCheck` and the report. `app/ui` therefore depends on `lib/layouting-fx` from now on.
The reference set grew beyond the ASCII plus umlauts the feature plan named: it carries Latin-1,
Latin Extended-A and Cyrillic as well, because a substituted family usually differs in exactly those
letters while plain ASCII still matches. Set and size are fixed from here on. A fingerprint is
written when a project is saved and only where none stands yet, because the design editor of IP-13
does not exist yet and overwriting on every save would make the comparison pointless.

The JavaFX decision of IP-25 was made: `.claude/rules/architecture.md` now allows JavaFX in exactly
one component library under `lib`, and `lib/layouting-fx` is that one. The module builds and its
headless TestFX smoke test runs. It exports nothing yet; the renderer package arrives with IP-26.
No plan is blocked by anything.

IP-26 moved the font foundation of IP-01 into `lib/layouting-fx`: `FontCatalog`, `FontResolver`,
`FontResolution` and `JavaFxTextMetrics` now live in package
`org.pcsoft.app.aighost.layouting.fx.font` beside the IP-22 fingerprint, and their three tests moved
with them onto the module's headless TestFX setup (`:lib:ai-ghost-layouting-fx:test` now runs 34).
The library type that replaces `FontData` in every moved signature is `FontDescription` (family,
`size: Int`, `bold`, `italic`); the size stays a whole point so resolution and the measurement cache
are unchanged. The application-side translation is one extension `FontData.toFontDescription()` in
`app/ui` (`FontTranslation.kt`), modelled on `FontFingerprintTranslation.kt`, decided over a
translator object on the user's request. `FontIdentity` is the only production caller and resolves
through it. No `module-info` needed a structural change - the package was already exported,
`javafx.graphics` already required, `app/ui` already read the library since IP-25 - only comments
were sharpened. `app/ui` keeps `FontIdentity`, `FontIdentityCheck` and the two translation files.
`SplashStageTest` was deleted on the user's request: it failed on the unmodified HEAD in this
headless environment (splash opacity `1.0` instead of `0.0`), unrelated to this plan. The full
`build` and a forced `clean :app:ai-ghost-ui:jlink` are green; the runtime image builds with no
split-package or resolution error. IP-01 stays COMPLETED; IP-07, IP-08 and IP-13 are now unblocked.

Defect found while checking IP-25, outside its scope and not caused by it - it already failed on the
unmodified project - and fixed right away on the user's request: `:app:ai-ghost-ui:createMergedModule`
could not compile the generated descriptor of the merged module, because that compilation only sees
the staging directories of the jlink plugin and neither JavaFX nor the Kotlin standard library was
staged there. `addExtraDependencies("javafx", "kotlin")` in the `jlink` block of `app/ui` stages
them; `jlink` now produces the image with its launcher again.

The renderer became a library of its own after IP-03. Both surfaces and the JavaFX measuring live in
the new module `lib/layouting-fx` (`ai-ghost-layouting-fx`), which depends on `ai-ghost-layouting` and
JavaFX and on nothing else and carries no type of this application in any signature. It is a
reusable JavaFX component library, exactly as `lib/layouting` is a reusable typesetting library.
Four plans carry it: IP-25 creates the module and the rule, IP-26 moves the font and measuring classes
of the completed IP-01 out of `app/ui`, IP-27 makes the appearance overridable, IP-28 proves and
documents the independence. IP-07 and IP-08 were re-scoped onto the library rather than rewritten;
IP-01 stays COMPLETED because IP-26 does the move.

Two things moved between plans with it: page virtualisation from IP-16 into IP-07, because a library
that cannot show a long document is not reusable, and the surface comparison of IP-06 into the
library test source set. `app/ui` keeps the `FontData` translation, the metrics fingerprint of IP-22,
the binding of book and design, undo, inspector and AI.

The layout core is implemented as planned. `LaidOutLine` carries two fields the plan did not name:
`width`, and `wordSpacing` as the gap a justified line is stretched by - without it a justified line
could not be drawn from the result alone. A break opportunity is not always a gap: after a hyphen the
next word follows immediately, so a word carries whether whitespace separated it from the next one.
The gaps above and below a block are not stored in the document and are fixed in `BlockSpacing` of
`lib/layouting-model`.

The scope covers the title page, the copyright page and all written parts. Prolog, epilog and blurb
always stand on pages of their own and always carry their text; the checkbox in the project tree
decides only whether they belong to the book. A switched off part is greyed out, left out of the page
numbering and still writable, which is why IP-24 reworks `Book` so nothing is deleted. IP-23 is the
only plan of this feature that touches `ProjectList`.

IP-24 carries the switch as the field `included` directly on `Prolog`, `Epilog` and `Blurb`. The
shared interface the plan of IP-24 once described was dropped before implementation: no caller
reaches the switch polymorphically, so the interface would have carried no weight. `Prolog.title` and
`Epilog.title` gained the default `""`, because `Book` now builds the three parts itself.

Text is measured with `javafx.scene.text.Font` through a reused hidden `Text` node. The implementation
lives in `app/ui` today and moves into `lib/layouting-fx` with IP-26; `lib/layouting` owns the
`TextMetrics` interface and stays free of any toolkit.

IP-03 was re-cut before implementation. `lib/layouting` is a general purpose typesetting library: it
depends on nothing, not even on `ai-ghost-model`, carries its own style and alignment types and knows
a single block of text plus style - no role, no title page, no heading type. Those blocks differ only
in which text and which style go in, so separate types would have carried no weight. The builders that
read `Book`, `Design` and `Meta` moved into the new module `lib/layouting-model`, named and packaged
after `lib/fx-model`, which is therefore the only module depending on both sides. IP-20 (PDF export) was removed from this feature: export
becomes an own feature, implemented as a plugin on Apache PDFBox, and needs the plugin infrastructure
built first. IP-22 was added when it was decided that no manuscript font is shipped; it is listed with
IP-01 because it belongs to the font foundation. IP-25 to IP-28 were added when the renderer was
decided to be an independent library. The numbering was kept stable instead of renumbering the plans.

Independent starting points: IP-09, IP-12 (completed), IP-17.

Besides the JavaFX decision of IP-25, two questions of the renderer library are open and do not block
it: the naming of module and package, which carries the application name into a reusable library, and
whether the library is published as a real artifact or only built here.

Page format and margins, the front matter, the place of the blurb, the behaviour of a switched off
part and the reference set of the metrics fingerprint are recorded in section 9 of the plan under
"Decisions taken".

No plan raises a model version or migrates an existing user file; a document carrying none of the
new values is read with their defaults.

The remaining implementation plans are written out under `.claude/plans/implementation`, each with
its own status file and with its origin and its dependencies named in it; `FP-001-Overview.md` lists
them in order. The files of a finished plan are removed, so the table above is the only record that
it is done. Every open plan, IP-25 to IP-28 and the library scoped IP-27 included, is written out.

The Feature Plan is orientation only: it carries the objective, the architecture, the plan overview,
the dependency graph, the decisions and the completion criteria. Tasks, constraints and tests of a
plan live in its file under `.claude/plans/implementation`; section 7 of the Feature Plan names that
file per plan and keeps only the reasoning the detailed plan does not carry.

The feature adds three Gradle modules (`lib/layouting`, `lib/layouting-model`, `lib/layouting-fx`) and
needs one new third party dependency: JavaFX in a library module.

The central technical risk is that measuring belongs to the FX thread. It has to be measured in
IP-05, not first noticed in IP-16.

IP-21 is optional and is only started if the break marker of IP-08 proves insufficient in use.
