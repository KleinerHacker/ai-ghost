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
| IP-36 | Model Umstellung Auf Anker-Struktur             | COMPLETED   |
| IP-37 | Dokument-Persistenz Und Migration               | COMPLETED   |
| IP-38 | Buch-Dokument Als Alleinige Basis               | COMPLETED   |
| IP-39 | PaperSheetView Dauerhaft Im Zentrum             | COMPLETED   |
| IP-32 | Paragraph Structure Operations On Document     | NOT_STARTED |
| IP-33 | Undo On Immutable Document Swap                | NOT_STARTED |
| IP-18 | AI Actions On Paragraph And Heading            | NOT_STARTED |
| IP-23 | Optional Book Parts In The Tree                | COMPLETED   |

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

82 % (18 von 22 zählenden Plänen abgeschlossen; IP-31 zählt als abgeschlossen, aber sein Ergebnis ist
abgelöst und wurde von IP-39 neu erbracht)

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

IP-36 abgeschlossen: `title`/`titleAppendix`/`paragraph` aus `BookPart`/`Book`/`Copyright` entfernt,
`Chapter.id: UUID` als unveränderlicher Anker ergänzt. FX-Modelle entsprechend verschlankt,
`ChapterProperty.idProperty` neu. Ein reines Lese-Modell für eine künftige Migration
(`lib/model/.../book/legacy/LegacyBookText.kt`) liegt bereit, wird aber nirgends produktiv genutzt, da
der Nutzer Migration für Teil B/C ausdrücklich abgelehnt hat (siehe IP-37).

IP-37 abgeschlossen, mit vom Nutzer bestätigter Abweichung: `Book.document: Document` neu, als JSON
kodiert (`DocumentCodec`) und über ein `documentPayload`-Feld in die bestehende Jackson-ZIP-Persistenz
eingebettet - kein eigener Jackson-Serializer für `Document` selbst. `lib/model` bekam dafür
`kotlinx-serialization-json` und `simplay-engine-jvm` als neue, vom Nutzer genehmigte Abhängigkeiten.
**Abweichung:** keine Migration alter Projekte gebaut; ein Altprojekt ohne `document`-Feld wird beim
Laden nicht behandelt (offen für später). Ein beschädigtes `documentPayload` wird korrekt als
`ProjectStorage.Error.Corrupt` erkannt, wie jeder andere unlesbare Standardteil.

IP-38 abgeschlossen: `BookPartBuilder`/`TitlePageBuilder`/`CopyrightPageBuilder` lesen ihre Blöcke seit
IP-38 über den Anker aus `Book.document`, statt leer zu bleiben; ein Teil ohne eigene Seite bekommt
einen Seed-Block mit nur dem `${anchorId}`-Token. `BookDocumentBuilder`s Kapitel-Seiten-`id` läuft
jetzt über `chapter.id.toString()` statt `chapter-<index>` (Seiten-`id` = Anker-`id`, vom Nutzer
bestätigt). Neue Klasse `DocumentStyleRefresher` tauscht nach Laden und nach jeder Design-Änderung nur
den `TextStyle` der Blöcke aus, Text/Anker bleiben unangetastet. `PartTarget.AnchorBlock` neu, Prolog/
Kapitel/Epilog sind damit im `BookPartEditor` erstmals wirklich editierbar - zuvor zeigten sie nichts.
Kapitel anlegen/umbenennen/entfernen im Projektbaum komplett neu gebaut (`ProjectListViewModel`,
Kontextmenü in `ProjectListCell`), Entfernen fragt vorher nach Bestätigung (`AiGhostDialog.
showWarningConfirm`), da der Text sonst unwiderruflich verloren geht.

**In simPlay 0.3.1 gefunden, vom Nutzer behoben:** `DocumentEditor.splice()`
(`ui/common/.../DocumentEditor.kt`) baute jeden von einer Bearbeitung berührten Block ausschließlich
aus dem sichtbaren, ankerbereinigten linearen Text (`DocumentTextIndex.text`) neu auf - ein
`TextAnchor` trägt dort laut Design 0 Zeichen bei und hatte deshalb keine Chance, eine Bearbeitung zu
überleben; jede Bearbeitung eines ankertragenden Blocks löschte dessen Anker ersatzlos. Zunächst lokal
als `1.0-SNAPSHOT` getestet, inzwischen als reguläres Release `0.3.2` veröffentlicht;
`simplayVersion` im Wurzel-Build steht darauf.

IP-39 abgeschlossen: `PaperSheetView` zeigt seither dauerhaft das ganze Buch als einziges `Document`
(`BookPartEditorController.buildWholeDocument`); eine Baumauswahl navigiert nur noch über
`caretModel.moveToAnchor(anchorId)`, baut kein neues `Document` mehr. Rückschreiben läuft generalisiert
über alle Seiten gleichzeitig (`handleDocumentChanged`) und schließt seit dieser Umsetzung auch Titel-
und Copyright-Seite ein - beide waren bereits vor IP-39 ankerbasiert aus `Book.document` lesbar, nur
bisher nie editierbar geschaltet; der Klappentext-Anker wird beim Zurückschreiben abgestreift, damit
`Book.blurb.paragraph` sauber bleibt. Neue Werkzeugleiste in `EditorView.fxml` mit einem
Schreiben/Vorschau-`ToggleButton` (`WritingMode`-Enum, neu in `lib/model`, da `PaperSheetMode` als
JavaFX-Typ dort nicht zulässig ist); Modus und zuletzt angesteuerte Anker-Position werden in
`Preferences.editor` (`writingMode`, `lastAnchorId`) gemerkt und beim Öffnen wiederhergestellt. Der
erste Dokumentaufbau nach dem Öffnen eines Projekts wird um einen `Platform.runLater`-Takt verzögert,
damit ein `ProgressIndicator` vor dem blockierenden `simplay-engine.measure`-Aufruf sichtbar wird.

**Abweichungen:** Kein Icon auf dem Umschalter - der `icon-creator`-Agent verfügt in dieser Umgebung nur
über `Read`/`Glob`/`Grep`, keine Bilderzeugung; bleibt TODO. Keine echte Fortschritts-%-Anzeige, nur ein
unbestimmter Spinner, da `simplay-engine.measure()` keine inkrementelle API bietet; die Kosten eines
langen Buches wurden mangels Testprojekt nicht real gemessen. `Editor.inspectorCollapsed` liegt im
Modell bereit, ist aber nicht an die Oberfläche angebunden; Splitter-Positionen werden nicht gemerkt -
beides bleibt offenes TODO, da `Inspector`/`EditorView` noch keinen Gesamt-Einklappmechanismus besitzen.

IP-23 abgeschlossen: Checkbox auf Prolog-, Epilog- und Klappentext-Knoten (`ProjectListCell`,
Zustand/Schreiben über `ProjectListViewModel.set{Prolog,Epilog,Blurb}Included`, undo-fähig über
`UndoStack.record` auf der jeweiligen `includedProperty`). Abweichung vom Plan: kein
`CheckBoxTreeItem` (dessen automatische Teilbaum-Vererbung ungenutzt geblieben wäre, da die drei
Knoten keine Kinder tragen), sondern eine schlichte `CheckBox` im Zellen-`graphic`. Wichtigere
Abweichung: `PageMode.DISABLED` sitzt nicht im simPlay-Engine-Modell (`Page` trägt kein solches Feld),
sondern ist ein transientes UI-Konzept auf `PaperSheetView.pageModes: Map<String, PageMode>`
(`simplay-common`/`simplay-fx`) - `BookPartEditorViewModel` pflegt diese Map je Seiten-`id`, reagiert
auf einen Listener auf `BookProperty.prologProperty`/`epilogProperty`/`blurbProperty` und wendet sie
nach jedem `pushWholeDocument()` erneut an, da `pageModes` bei jedem von außen zugewiesenen `document`
geleert wird. `BookDocumentBuilder` blieb unverändert. Vor der Umsetzung `simplayVersion` auf `0.4.0`
gehoben (einzige Breaking Change ohne Codebezug). Beim Testen zusätzlich einen stillen Bug in
`lib/fx-model`s `BeanFields` gefunden und behoben: der `ChangeListener`, der ein verschachteltes
Property-Modell an sein gekapseltes Objekt band, wurde von JavaFX unterdrückt, wenn zwei
aufeinanderfolgende Objekte `equals()`-gleich, aber nicht dieselbe Instanz waren (z. B. zwei frische
`Book()`) - jetzt ein referenz-idempotentes `rebind()` mit `InvalidationListener`, in `BeanFields.kt`
und rund 20 Property-Modell-Klassen.

Nächster Schritt: IP-32, IP-33 oder IP-18.
