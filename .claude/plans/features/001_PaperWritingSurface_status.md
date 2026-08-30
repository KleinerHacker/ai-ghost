# Feature Status: Paper Writing Surface

Status: NOT_STARTED

## Implementation Plans

| ID    | Implementation Plan                       | Status      |
|-------|-------------------------------------------|-------------|
| IP-01 | Font Discovery And Text Measuring         | COMPLETED   |
| IP-22 | Font Identity And Substitution Reporting  | NOT_STARTED |
| IP-02 | Design Page Format Model                  | NOT_STARTED |
| IP-24 | Optional Parts In The Model               | NOT_STARTED |
| IP-03 | Layout Core                               | NOT_STARTED |
| IP-04 | Pagination And Page Break Policy          | NOT_STARTED |
| IP-05 | Incremental Layout And Caching            | NOT_STARTED |
| IP-06 | Layout Regression Harness                 | NOT_STARTED |
| IP-07 | Paper Page View                           | NOT_STARTED |
| IP-08 | Paper Flow View                           | NOT_STARTED |
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

0%

## Notes

Feature Plan created. No implementation plan has been started yet.

The scope covers the title page, the copyright page and all written parts. Prolog, epilog and blurb
always stand on pages of their own and always carry their text; the checkbox in the project tree
decides only whether they belong to the book. A switched off part is greyed out, left out of the page
numbering and still writable, which is why IP-24 reworks `Book` so nothing is deleted. IP-23 is the
only plan of this feature that touches `ProjectList`.

Text is measured with `javafx.scene.text.Font` through a reused hidden `Text` node, so the measuring
implementation lives in `app/ui` and `lib/font` was dropped; `lib/layout` owns the `TextMetrics`
interface and stays free of any toolkit. IP-20 (PDF export) was removed from this feature: export
becomes an own feature, implemented as a plugin on Apache PDFBox, and needs the plugin infrastructure
built first. IP-22 was added when it was decided that no manuscript font is shipped; it is listed with
IP-01 because it belongs to the font foundation. The numbering was kept stable instead of renumbering
the plans.

Independent starting points: IP-01, IP-02, IP-24, IP-09, IP-12, IP-17.

No decision is left open. Page format and margins, the front matter, the place of the blurb, the
behaviour of a switched off part and the reference set of the metrics fingerprint are recorded in
section 9 of the plan under "Decisions taken".

IP-24 is the only plan that migrates an existing user file; `Book` goes to version 2.

The 23 implementation plans are written out under `.claude/plans/implementation`, each with its own
status file and with its origin and its dependencies named in it; `000_Uebersicht.md` lists them in
order.

The feature is expected to need no new third party dependency and adds one Gradle module
(`lib/layout`).

The central technical risk is that measuring belongs to the FX thread. It has to be measured in
IP-05, not first noticed in IP-16.

IP-21 is optional and is only started if the break marker of IP-08 proves insufficient in use.
