# Feature-Status: Paper Writing Surface

Status: IN_PROGRESS

## Implementierungspläne

| ID    | Implementierungsplan                             | Status      |
|-------|---------------------------------------------------|-------------|
| IP-01 | Font Discovery And Text Measuring (teilw. abgelöst) | COMPLETED   |
| IP-02 | Design Page Format Model                       | COMPLETED   |
| IP-24 | Optional Parts In The Model                    | COMPLETED   |
| IP-09 | Undo And Redo Infrastructure                   | COMPLETED   |
| IP-12 | Inspector Shell And Content Sections           | COMPLETED   |
| IP-13 | Design Style Sections                          | COMPLETED   |
| IP-14 | Project Settings Dialog                        | COMPLETED   |
| IP-17 | AI Action Port                                 | COMPLETED   |
| IP-19 | AI Part Generation (button only)               | COMPLETED   |
| IP-29 | simPlay Integration                            | COMPLETED   |
| IP-30 | Book To simPlay Document Builder               | COMPLETED   |
| IP-34 | Font Discovery And Metric Fingerprint On simPlay | COMPLETED   |
| IP-35 | Page Numbering And Page Modes On simPlay 0.3.0 | COMPLETED   |
| IP-31 | Writing Surface On PaperSheetView (Ergebnis abgelöst) | COMPLETED (abgelöst) |
| IP-36 | Model Umstellung Auf Anker-Struktur             | NOT_STARTED |
| IP-37 | Dokument-Persistenz Und Migration               | NOT_STARTED |
| IP-38 | Buch-Dokument Als Alleinige Basis               | NOT_STARTED |
| IP-39 | PaperSheetView Dauerhaft Im Zentrum             | NOT_STARTED |
| IP-32 | Paragraph Structure Operations On Document     | NOT_STARTED |
| IP-33 | Undo On Immutable Document Swap                | NOT_STARTED |
| IP-18 | AI Actions On Paragraph And Heading            | NOT_STARTED |
| IP-23 | Optional Book Parts In The Tree                | NOT_STARTED |

## Abgelöste Pläne (simPlay-Abweichung)

Diese Pläne waren umgesetzt, ihre Ergebnisse werden durch simPlay ersetzt. Sie zählen nicht mehr zum
Fortschritt. Begründung im Feature-Plan, Abschnitt 6, „Abgelöste Pläne (simPlay)“.

| ID    | Früherer Plan                              | Ersetzt durch                        |
|-------|--------------------------------------------|---------------------------------------|
| IP-03 | Layout Core                                | `simplay-engine` (IP-29)              |
| IP-04 | Pagination And Page Break Policy           | `simplay-engine`; Restpolitik = TODO  |
| IP-05 | Incremental Layout And Caching             | `PaperSheetView` / `measure` (IP-31)  |
| IP-06 | Layout Regression Harness                  | entfällt; dünne Prüfung in IP-39      |
| IP-07 | Paper Page View                            | `PaperSheetView SELECTABLE` (IP-39)   |
| IP-08 | Paper Flow View                            | `PaperSheetView EDITABLE` (IP-31/39)  |
| IP-10 | Book Part Writing Surface                  | IP-31                                 |
| IP-11 | Paragraph Structure Operations             | IP-32                                 |
| IP-22 | Font Identity And Substitution Reporting   | IP-34                                 |
| IP-25 | Renderer Library Module                    | entfällt (IP-29 nimmt Regel zurück)   |
| IP-26 | Font And Measuring Migration               | entfällt; Schrift-Stack zu `app/ui`   |

## Abgelöste Pläne (TextAnchor-Abweichung)

Zweite Abweichung, vom Nutzer bestätigt. Diese Pläne sind vollständig abgelöst, ihre Dateien per
`git rm` entfernt; Begründung im Feature-Plan, Abschnitt 6, „Abgelöste Pläne (TextAnchor)“. IP-31 bleibt
als abgeschlossener Plan stehen, aber sein Ergebnis (Dokument-Tausch je Baumauswahl) ist mit abgelöst;
nur seine reinen Funktionen und sein Sync-Muster gehen in IP-39 über.

| ID    | Früherer Plan                              | Ersetzt durch                                              |
|-------|-----------------------------------------------|----------------------------------------------------------|
| IP-15 | Editor Arrangement And Tree Routing        | IP-39 (Navigation über `TextAnchor` statt Dokument-Tausch) |
| IP-16 | Writing And Preview Modes                  | IP-39 (`mode`-Umschaltung auf demselben `Document`)        |

## Gesamtfortschritt

59 % (13 von 22 zählenden Plänen abgeschlossen; IP-31 zählt als abgeschlossen, aber sein Ergebnis ist
abgelöst und wird von IP-39 neu erbracht)

## Anmerkungen

**Erste Abweichung (simPlay):** Das gesamte Text-Layouting und FX-Rendering wurde nach simPlay
(`org.pcsoft.framework:simplay-*`, GitHub Packages) ausgelagert. Vom Nutzer bestätigt:

* simPlay vollständig übernehmen; `lib/ai-ghost-layouting` und `lib/ai-ghost-layouting-fx` per
  `git rm` entfernen; `lib/ai-ghost-layouting-model` als Übersetzer behalten und auf das
  simPlay-Rohmodell umstellen.
* ai-ghost-Seitenpolitik (Nummerierung, gespiegelte Ränder, inaktive/leere Seiten,
  Klappentext-Kante) fügt der Nutzer nachträglich in simPlay ein; bis dahin trägt IP-30 nur eine
  Zwischenlösung, und die Lücke ist als TODO markiert.
* Schrift-Fingerabdruck (früher IP-22) bleibt, kommt seit simPlay 0.2.1 aber direkt aus
  `simplay-fx`s `FxFontProbe`, nicht aus eigenem JavaFX-Messcode. Nur die Ersatzfamilie-Meldung und
  die Familienliste für die Auswahl ziehen nach `app/ui` (IP-34).

IP-29 abgeschlossen: simPlay-Repository und exakte Version (`0.2.1`) im Wurzel-Build, `mavenLocal()`
als tokenloser Weg; `lib/ai-ghost-layouting` und `lib/ai-ghost-layouting-fx` per `git rm` entfernt;
CI-Job `regression-test` (samt beider `regressionTest`-Gradle-Tasks) entfernt, `GITHUB_TOKEN` in alle
Gradle-Jobs gezogen; Architekturregel auf `app/ui` als einzigen JavaFX-Ort verengt.

IP-30 abgeschlossen: `lib/ai-ghost-layouting-model` übersetzt `Book`/`Design`/`Meta` in ein simPlay-
`Document` (`BookDocumentBuilder`, vier Block-Builder, `StyleTranslation`, `PageLayoutTranslation`).
Auf simPlay 0.2.2 gehoben (Artefakt-Umbenennung `simplay-engine-jvm`); `licensee` läuft ohne Ausnahme
grün. Die fehlende ai-ghost-Seitenpolitik ist durchgehend als `TODO(simPlay page policy)` markiert.

IP-34 abgeschlossen: `app/ui`s Schrift-Stack läuft vollständig über `simplay-fx`s `FxFontProbe`.
`FontData.metrics: FontMetricsData?` entfällt zugunsten von `FontData.fingerprint: String?`;
`FontMetricsData`/`FontMetricsDataProperty` per `git rm` entfernt. `FontIdentity`/`FontIdentityCheck`/
`FontTranslation` auf simPlays Typen umgestellt, `FontSubstitution` ergänzt die Ersatzfamilie.

IP-31 abgeschlossen: `BookPartEditor` schrieb auf einer `PaperSheetView` je Teil ein eigenes
`Document` (`SinglePage` für Titel-/Copyright-Seite, `FlowPage` sonst); `paperSheetView.mode` wechselte
`EDITABLE`/`SELECTABLE` auf derselben Komponente. Bearbeitungen wurden über einen
`documentProperty`-`ChangeListener` erkannt und index-weise zurückgeschrieben.
**In simPlay 0.2.2 gefunden und dem Maintainer gemeldet:** `TextBlock.toString()` fügt ein Leerzeichen
vor einem `TextWord` ein, das direkt auf ein Symbol ohne echtes Leerzeichen folgt, und verschluckt ein
alleinstehendes angehängtes Leerzeichen beim Retokenisieren; beides bringt `DocumentEditor.splice()`s
`caretIndex` gegenüber dem gespeicherten Text aus dem Takt. **IP-32 braucht den Fix upstream**, da es
echte Trennzeichen tippt - unverändert gültig, unabhängig von der zweiten Abweichung.

IP-35 abgeschlossen: `simplayVersion` auf `0.3.0` gehoben. `Design` trägt `pageNumbering:
PageNumberDesign`, gespiegelt in `lib/fx-model`. `BookDocumentBuilder` übersetzt das nach
`Document.numbering` und baut jede Seite mit einer festen, stabilen `id` (`title`, `copyright`,
`prolog`, `chapter-<n>`, `epilog`, `blurb`). `PageMode.DISABLED` für einen ausgeschalteten optionalen
Teil wurde NICHT verdrahtet, weil `BookPartEditor` je Auswahl nur ein Dokument mit einer Seite zeigte;
diese Verdrahtung war auf die künftige Buchvorschau vertagt und findet jetzt direkt in IP-23 statt
(siehe unten). `PaperSheetMode.READONLY` wurde zugleich in `PaperSheetMode.SELECTABLE` umbenannt.

Die entfernten Plandateien `FP-001-IP-21-SeitentrennungImAbsatz.md`, `FP-001-IP-27-
BibliotheksStyling.md` und `FP-001-IP-28-EigenstaendigeNutzung.md` wurden per `git rm` gelöscht: IP-21
ist in `PaperSheetView` nativ, IP-27 gehört zu simPlay (jetzt in IP-31/IP-39), IP-28 entfällt, da die
Bibliothek nicht mehr im ai-ghost-Repository liegt.

**Zweite Abweichung (TextAnchor, ab simPlay 0.3.1), vom Nutzer bestätigt am 2026-09-14:** Es gibt kein
pro Buchteil gebautes `Document` mehr. `PaperSheetView` zeigt dauerhaft das ganze Buch als ein
einziges, frei editierbares `Document`; eine Baumauswahl navigiert darin, statt ein anderes `Document`
einzusetzen. Das Modell wird umgekehrt: `Book.document: Document` ist der gespeicherte Zustand, nicht
mehr abgeleitet. `Book`, `BookPart` (`Prolog`/`Epilog`), `Chapter` und `Copyright` verlieren ihre
Fließtext-Felder; `Chapter` bekommt eine gespeicherte `id: UUID` als Anker. Statische Teile behalten
die festen Kennungen, die `BookDocumentBuilder` bereits als Seiten-`id` vergibt (`title`, `copyright`,
`prolog`, `epilog`, `blurb`). Der `TextStyle` im `Document` wird beim Laden verworfen und durch den aus
`Design` berechneten Stil ersetzt.

Damit sind IP-15 und IP-16 (beide `NOT_STARTED`) vollständig abgelöst und ihre Dateien entfernt.
IP-31s Ergebnis (Dokument-Tausch je Auswahl) ist ebenfalls abgelöst, der Plan selbst zählt aber als
abgeschlossen weiter, da seine reinen Funktionen (`splitParagraph` &c.) und sein
`documentProperty`-Sync-Muster unverändert in IP-39 übergehen. IP-29, IP-30, IP-34, IP-35 sind von der
zweiten Abweichung inhaltlich nicht betroffen; ihre Bausteine (`BookDocumentBuilder` und die
Teil-Builder) werden von IP-38 weiterverwendet.

Neue Pläne: IP-36 (Modell-Umstellung), IP-37 (Dokument-Persistenz und Migration), IP-38 (Buch-Dokument
als alleinige Basis, `TextAnchor`-Verdrahtung), IP-39 (`PaperSheetView` dauerhaft im Zentrum, ersetzt
IP-15/IP-16). IP-32, IP-33, IP-18, IP-23 bleiben inhaltlich bestehen, mit IP-39 statt IP-31/IP-15 als
Abhängigkeit; IP-23 verdrahtet `PageMode.DISABLED` jetzt sofort, ohne auf eine gesonderte Buchvorschau
zu warten.

Offene Fragen, vor Start von IP-38/IP-39 zu klären: genaue API von `TextAnchor` (simPlay 0.3.1 steht
noch aus), Jackson-Verträglichkeit des simPlay-`Document` in der bestehenden ZIP-Persistenz (IP-37),
Rückfrage-Verhalten beim Entfernen eines ganzen Kapitels über den Baum. Details im Feature-Plan,
Abschnitt 9.

Nächster Schritt: IP-36 (Modell-Umstellung auf Anker-Struktur).
