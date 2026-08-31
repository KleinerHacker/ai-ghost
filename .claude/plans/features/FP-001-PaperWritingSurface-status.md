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
| IP-04 | Pagination And Page Break Policy          | NOT_STARTED |
| IP-25 | Renderer Library Module                   | COMPLETED   |
| IP-26 | Font And Measuring Migration              | NOT_STARTED |
| IP-05 | Incremental Layout And Caching            | NOT_STARTED |
| IP-06 | Layout Regression Harness                 | NOT_STARTED |
| IP-07 | Paper Page View                           | NOT_STARTED |
| IP-08 | Paper Flow View                           | NOT_STARTED |
| IP-27 | Library Styling And Theming API           | NOT_STARTED |
| IP-28 | Standalone Reuse And Documentation        | NOT_STARTED |
| IP-09 | Undo And Redo Infrastructure              | NOT_STARTED |
| IP-10 | Book Part Writing Surface                 | NOT_STARTED |
| IP-11 | Paragraph Structure Operations            | NOT_STARTED |
| IP-12 | Inspector Shell And Content Sections      | NOT_STARTED |
| IP-13 | Design Style Sections                     | NOT_STARTED |
| IP-14 | Project Settings Dialog                   | NOT_STARTED |
| IP-15 | Editor Arrangement And Tree Routing       | NOT_STARTED |
| IP-16 | Writing And Preview Modes                 | NOT_STARTED |
| IP-17 | AI Action Port                            | NOT_STARTED |
| IP-18 | AI Actions On Paragraph And Heading       | NOT_STARTED |
| IP-19 | AI Part Generation With Provisional State | NOT_STARTED |
| IP-21 | In-Paragraph Sheet Split                  | NOT_STARTED |
| IP-23 | Optional Book Parts In The Tree           | NOT_STARTED |

## Overall Progress

19%

## Notes

IP-01, IP-22, IP-02, IP-24, IP-03 and IP-25 are implemented; IP-04 and IP-26 are unblocked.

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

Independent starting points: IP-25 (once unblocked), IP-09, IP-12, IP-17.

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
it is done. Every open plan, IP-25 to IP-28 and the library scoped IP-07 and IP-08 included, is
written out.

The Feature Plan is orientation only: it carries the objective, the architecture, the plan overview,
the dependency graph, the decisions and the completion criteria. Tasks, constraints and tests of a
plan live in its file under `.claude/plans/implementation`; section 7 of the Feature Plan names that
file per plan and keeps only the reasoning the detailed plan does not carry.

The feature adds three Gradle modules (`lib/layouting`, `lib/layouting-model`, `lib/layouting-fx`) and
needs one new third party dependency: JavaFX in a library module.

The central technical risk is that measuring belongs to the FX thread. It has to be measured in
IP-05, not first noticed in IP-16.

IP-21 is optional and is only started if the break marker of IP-08 proves insufficient in use.
