# Feature-Plan: Paper Writing Surface

> Nur zur Orientierung. Jede Aufgabe, Einschränkung und jeder Test gehört zu den
> Implementierungsplänen unter `.claude/plans/implementation`; `FP-001-Overview.md` listet sie, und
> Abschnitt 7 benennt die Datei jedes einzelnen.

> **Große Planabweichung (simPlay).** Das gesamte Text-Layouting und das FX-Rendering werden nach
> `simPlay` (`org.pcsoft.framework:simplay-*`, GitHub Packages) ausgelagert. Der Eigenbau
> `lib/ai-ghost-layouting` (Satz-Engine) und `lib/ai-ghost-layouting-fx` (JavaFX-Renderer) entfallen
> vollständig. `lib/ai-ghost-layouting-model` bleibt als Übersetzer und wird auf das Rohmodell von
> simPlay umgestellt. Abschnitt 2, 3, 5 bis 10 sind auf diesen Stand geschrieben; die früher als
> abgeschlossen geführten Pläne IP-03 bis IP-08, IP-10, IP-11, IP-22, IP-25 und IP-26 sind damit
> abgelöst (Abschnitt 6, „Abgelöste Pläne“).

## 1. Ziel

Das Schreiben eines Buchteils fühlt sich an wie das Schreiben auf der gedruckten Seite: Titel,
Überschriften und Absätze werden auf einem Blatt bearbeitet, das bereits die Typografie, die Ränder
und die Seitenstruktur des fertigen Buches trägt.

Zwei Kräfte begrenzen die Metapher: Das Erscheinungsbild gehört dem Projektdesign, nicht der
schreibenden Person, und Text wird teilweise von der KI geschrieben, die an einem Teil, einer
Überschrift oder einem Absatz arbeitet statt an einer Cursorposition. Das Feature antwortet auf
beides, indem es das Blatt zur einzigen Schreibfläche macht, alles, was kein gedruckter Text ist, in
ein Kontextfenster verlagert und die KI an die strukturellen Einheiten hängt, die das Modell bereits
kennt.

Was beim Schreiben gezeigt wird, ist das, was die Vorschau zeigt: Eine Layout-Engine entscheidet
jeden Zeilenumbruch und jeden Seitenumbruch, und beide Oberflächen verbrauchen dieses eine Ergebnis.
Diese Engine ist ab dieser Planabweichung **simPlay**, kein Eigenbau; `app/ui` ist ihr Verbraucher.

Der Export ist nicht Teil dieses Features. Er wird nur als Einschränkung benannt: Das Rohdokument von
simPlay bleibt toolkit-unabhängig und serialisierbar, sodass ein späterer Export – ein Plugin auf
Apache PDFBox oder das simPlay-Modul `j-pdf` – dieselbe Struktur verbraucht.

## 2. Aktueller Stand

* `lib/ai-ghost-model`, `lib/ai-ghost-fx-model` – Manuskript-POJOs und ihre gespiegelten
  FX-Eigenschaften. `Design` trägt `PageFormat` (Größe, Ränder innen/außen/oben/unten,
  `mirroredMargins`) und Zeilenabstände je Elementklasse (IP-02). `Prolog`, `Epilog`, `Blurb` tragen
  `included` und immer ihren Text (IP-24). `Preferences` trägt die Gruppe `Editor`
  (`paragraphMergePauseMillis`).
* `lib/ai-ghost-layouting` – Eigenbau-Satz-Engine (frühere IP-03/IP-04/IP-05/IP-06). **Wird mit
  dieser Abweichung per `git rm` entfernt.**
* `lib/ai-ghost-layouting-model` – Übersetzer von `Book`, `Design`, `Meta` auf die Engine. **Bleibt,
  wird auf das simPlay-Rohmodell umgestellt** (IP-30).
* `lib/ai-ghost-layouting-fx` – Eigenbau-JavaFX-Renderer: `FontCatalog`, `FontResolver`,
  `FontResolution`, `JavaFxTextMetrics`, `FontFingerprint(s)`, `PaperPageView`, `PaperFlowView`,
  `PaperFlowListener`, Regressionsprüfstand (frühere IP-07/IP-08/IP-25/IP-26; IP-06 zur Hälfte).
  **Wird mit dieser Abweichung per `git rm` entfernt.** Die Schriftermittlung und der Fingerabdruck
  ziehen nach `app/ui` (IP-34).
* `lib/ai-ghost-ai` – der KI-Aktions-Port aus IP-17 (`AiAction`, `AiActionRequest`, Callbacks,
  `AiActionLimits`, `ParagraphSplitter`), ohne Implementierung. Unverändert.
* `app/ui` – das einzige JavaFX-Modul, MVVM FX, jlink-Image. Trägt heute `FontIdentity`,
  `FontIdentityCheck`, `FontTranslation`, `FontFingerprintTranslation`, `BookPartEditor` (bettet
  `PaperFlowView` ein), `Inspector` mit den Abschnitten „Book“/„Chapter“/„Design“ (IP-12/IP-13), den
  Projekteinstellungsdialog (IP-14), Undo/Redo (IP-09), die KI-Schaltfläche „Kapitel generieren“
  (IP-19). `BookPartEditor` und die Absatz-Operationen hängen an `PaperFlowView` und werden mit
  IP-31/IP-32 auf `PaperSheetView` umgestellt.
* `.claude/rules/architecture.md` erlaubt JavaFX in `app/ui` und in der einen
  JavaFX-Komponentenbibliothek unter `lib`. Die zweite Erlaubnis entfällt mit der Entfernung von
  `lib/ai-ghost-layouting-fx`.
* Build-Infrastruktur: Wurzel-`build.gradle.kts` mit `repositories { mavenCentral() }`,
  `app.cash.licensee`, `com.github.jk1.dependency-license-report`, `org.cyclonedx.bom` auf allen
  Modulen. Kein GitHub-Packages-Repository konfiguriert.

Fehlt: die simPlay-Anbindung (Repository, Abhängigkeit, Lizenz-Allowlist, CI-Token); die Umstellung
des Übersetzers auf das simPlay-Rohmodell; die Schreibfläche und die Vorschau auf `PaperSheetView`;
die Absatz-Operationen auf dem simPlay-Dokument; Undo auf dem unveränderlichen Dokument-Tausch; der
Fingerabdruck auf der simPlay-Messung; die Editor-Aufteilung und das Baum-Routing (IP-15); der
Schreib-/Vorschau-Umschalter (IP-16); die schwebende KI-Leiste (IP-18); das Kontrollkästchen im
Projektbaum (IP-23).

## 3. Zielzustand

Drei Zonen rechts des Projektbaums: das **Papier** in der Mitte (eine `PaperSheetView` von simPlay),
ein einklappbarer **Inspector** rechts, der alles trägt, was kein gedruckter Text ist, und ein
**Modus-Schalter** zwischen `Schreiben` und `Vorschau` über demselben Inhalt.

* **Treuekette über simPlay.** `simplay-engine` verwandelt ein Rohdokument (`Document` aus `Page`,
  `TextBlock`, `TextStyle`) in ein `MeasuredDocument` mit absoluten Positionen; `simplay-fx` malt
  dieses Ergebnis in `PaperSheetView`. Schreibfläche und Vorschau sind dieselbe Komponente in den
  Modi `EDITABLE` und `READONLY` über demselben `Document`, sodass ein Absatz in beiden auf dieselbe
  Seite fällt – strukturell, nicht durch einen Vergleich.
* **Messung.** simPlay misst über einen `FontMeasureCalculator`; für JavaFX stellt `simplay-fx` ihn
  bereit (falls nicht exponiert: ein kleiner lokaler Rechner auf `javafx.scene.text` in `app/ui`,
  IP-34). Keine Schriftdatei wird gelesen, geparst oder ausgeliefert. Familien kommen aus
  `javafx.scene.text.Font`.
* **Übereinstimmung pro Maschine.** Die Messung läuft lokal über JavaFX; ein Projekt zeichnet die
  Metriken auf, mit denen es geschrieben wurde, und eine Abweichung (aktualisierte oder ersetzte
  Familie) wird mit dem verwendeten Ersatz gemeldet (IP-34, Fingerabdruck auf simPlay-Messung).
* **Der Übersetzer bleibt ai-ghost.** `lib/ai-ghost-layouting-model` baut aus `Book`, `Design` und
  `Meta` ein simPlay-`Document`: ein `FlowPage` je Buchteil in Reihenfolge, Titelseite und
  Copyright-Seite als eigene Seiten, der Klappentext als letztes Blatt. `PageLayout` (Größe, Ränder)
  je Seite wird aus `PageFormat` berechnet.
* **ai-ghost-Seitenpolitik ist ein TODO an simPlay.** simPlay liefert Zeilen-/Seitenumbruch,
  Ausrichtung, `FlowPage`/`SinglePage`. Es liefert **nicht**: Seitennummerierung (ungezählte
  Titel-/Copyright-Seite, Neunummerierung beim Schalten eines optionalen Teils), gespiegelte Ränder
  (recto/verso), inaktive Seiten eines ausgeschalteten Teils, führende/abschließende Leerseiten, die
  harte Kante des Klappentexts. Diese Politik wird **vom Nutzer nachträglich in simPlay eingefügt**;
  bis dahin trägt ai-ghost nur das Nötigste als Zwischenlösung und markiert die Lücke als TODO
  (Abschnitt 9).
* **Vorwerk.** Titelseite mit Titel, weiteren Titelzeilen und Autor, gefolgt direkt von der
  Copyright-Seite.
* **Optionale Teile.** Prolog, Epilog und Klappentext beginnen immer auf einer eigenen Seite, behalten
  immer ihren Text, bleiben beschreibbar, wenn ausgeschaltet, sind ausgegraut und – sobald die
  simPlay-Seitenpolitik steht – aus der Seitennummerierung genommen. Das Kontrollkästchen im Baum
  entscheidet über die Zugehörigkeit zum Buch, nicht über die Existenz.
* **KI** ist in diesem Feature nur eine Schaltfläche ohne Wirkung: eine schwebende Leiste am
  fokussierten Block (über simPlay `FloatingOverlay`, Trigger `PARAGRAPH_HOVER`/`CARET`) mit
  Umschreiben, Ausbauen und Kürzen, dazu eine Aktion auf Teil-Ebene im Inspector. Jede Schaltfläche
  ruft im `*View`-Controller eine leere, parameterlose Methode, deren einziger Rumpf
  `TODO("AI action: …")` ist. Der Aktions-Port aus IP-17 liegt fertig in `lib/ai`, wird von diesem
  Feature aber nicht verdrahtet und bleibt für eine spätere Wiederverwendung stehen; ein Provider
  kommt erst mit dem späteren Plugin-System-Feature.
* **Design**-Änderungen wirken sofort auf dem Papier: Der Übersetzer baut das `Document` neu, simPlay
  misst neu, `PaperSheetView` zeichnet neu.

## 4. Anforderungen

### Funktionale Anforderungen

* Titelseite, Copyright-Seite, Prolog, jedes Kapitel, Epilog und Klappentext werden auf dem Papier
  geschrieben.
* Prolog, Epilog und Klappentext werden über ein Kontrollkästchen im Baum in das Buch ein- und
  ausgeschaltet, ohne Text zu verlieren und ohne Bestätigung.
* Text wird im Design des Buches gezeichnet, je Elementklasse: Titel, Kapiteltitel,
  Kapiteltitel-Anhang, Fließtext – abgebildet auf `TextStyle` je `TextBlock`.
* Schriften werden aus den auf der Maschine installierten Familien gewählt; eine fehlende oder anders
  messende Schrift wird mit dem verwendeten Ersatz gemeldet.
* Seitenumbrüche werden dort gezeigt, wo das gedruckte Buch umbricht. `PaperSheetView` stellt einen
  Blattstapel dar; ein Umbruch innerhalb eines Absatzes ist eine echte Blattgrenze, keine Markierung.
* Absätze sind die Bearbeitungseinheit: erstellen, teilen, verbinden, löschen, umsortieren – jede
  Operation erzeugt ein neues `Document` und ersetzt das alte.
* Nirgends Inline-Zeichenformatierung; eingefügter Rich-Text wird auf reinen Text reduziert.
* Jede Textänderung und jede Strukturänderung ist undo- und redo-fähig; Undo tauscht die
  unveränderliche `Document`-Instanz zurück.
* Prompts, Teildaten und Designstile leben im Inspector, Seitenformat und Ränder im
  Projekteinstellungsdialog – nie auf dem Papier.
* Die Vorschau rendert das ganze Buch (`PaperSheetView` im Modus `READONLY`) und scrollt zu dem im
  Baum gewählten Teil; ein Absatz fällt in beiden Modi auf dieselbe Seite, da beide dasselbe
  `Document` und dieselbe Messung verwenden.

### Technische Anforderungen

* Kotlin und Gradle. JavaFX ausschließlich in `app/ui` (`simplay-fx` bringt es transitiv mit). Kein
  Bibliotheksmodul unter `lib` trägt JavaFX mehr; die Architekturregel wird entsprechend
  zurückgenommen.
* simPlay ist eine neue Drittanbieter-Abhängigkeit (vom Nutzer bestätigt): `simplay-engine` in
  `lib/ai-ghost-layouting-model`, `simplay-fx` in `app/ui`, bezogen aus GitHub Packages
  (`https://maven.pkg.github.com/KleinerHacker/simPlay`) mit Token lokal und in der CI.
* Lizenzen von simPlay und seinen transitiven Abhängigkeiten (u. a. kotlinx-serialization, JavaFX)
  werden in die Allowlist von `app.cash.licensee` aufgenommen; fehlende Lizenzen werden dem Nutzer
  vorgelegt.
* Das Rohdokument von simPlay bleibt frei von Toolkit-Typen und serialisierbar; das gemessene
  Ergebnis ist transient und wird nie gespeichert.
* Die Messung ist eine FX-Thread-Operation. `simplay-engine.measure(document)` läuft synchron; das
  erste Layout eines langen Buches wird mit einer Fortschrittsanzeige begleitet (IP-16).
* Modelländerungen folgen `fx-model`; UI-Arbeit folgt `ui-styling`, `fx-component-lifecycle`, `icons`
  und `font`; Tests folgen `testing`; Dokumentation folgt `project-docs`; Workflows folgen
  `ci-pipeline`.
* Der Text eines Teils bleibt `List<String>` im Projektdokument; View-Zustand gehört zu
  `Preferences`, nie zum Projektdokument. Das simPlay-`Document` ist eine abgeleitete Sicht, kein
  gespeicherter Zustand.

## 5. Architektur

```text
                       org.pcsoft.framework:simplay-engine   (Multiplatform, JVM, kein Toolkit)
                                   ▲                    ▲
                                   │                    │
lib/ai-ghost-layouting-model ──────┘                    │
   (Book/Design/Meta -> simPlay Document, PageLayout)   │
                                   ▲                    │
                                   │        org.pcsoft.framework:simplay-fx  (JavaFX, PaperSheetView)
                                   │                    ▲
                                   └──────── app/ui ────┘   (ai-ghost-spezifischer Kleber)
```

* **`simplay-engine`** – Satz-Engine von simPlay. Rohmodell (`Document`, `Page` als `FlowPage` oder
  `SinglePage`, `PageLayout` mit `Size`/`Margins`, `TextBlock.of(text, style)`, `TextStyle` mit Font,
  Zeilenabstand und Ausrichtung `LEFT`/`RIGHT`/`CENTER`/`JUSTIFY`), `SimpLayEngine.builder(
  FontMeasureCalculator).build().measure(document)` liefert ein `MeasuredDocument`. Umbrecher:
  `GreedyWordLineBreakerStrategy` (Vorgabe), `CharacterLineBreakerStrategy`,
  `NoWrapLineBreakerStrategy`. Deterministisch: gleiches Dokument plus gleicher Callback ergibt
  dasselbe Ergebnis.
* **`simplay-fx`** – JavaFX-Anbindung von simPlay. `CanvasDocumentRenderer` (unterste Ebene, Canvas
  ohne Chrome), `PaperSheetView` (scrollbar, zoombar, Seitenvirtualisierung, Textauswahl mit Maus,
  `PaperSheetMode.READONLY`/`EDITABLE`, `CaretModel`, `TextSelectionModel`, `FloatingOverlay` mit
  Triggern `SELECTION`/`PARAGRAPH_HOVER`/`PAGE_HOVER`/`CARET`, FXML-fähig). Stilklasse
  `paper-sheet-view`, Pseudoklassen `:readonly`/`:focused`, zehn `-fx-`-Eigenschaften für das
  Chrome; die Textdarstellung ist nicht über CSS änderbar. JavaFX wird transitiv exportiert.
* **`lib/ai-ghost-layouting-model`** – der einzige ai-ghost-Übersetzer: `BookPartBuilder`,
  `TitlePageBuilder`, `CopyrightPageBuilder`, `BlurbBuilder` bauen aus `Book`, `Design`, `Meta` ein
  simPlay-`Document`; `PageGeometryTranslation` wird zu einer `PageLayout`-Berechnung aus
  `PageFormat`; `BlockSpacing` bleibt. Hängt von `ai-ghost-model` und `simplay-engine` ab, von keinem
  Toolkit.
* **`lib/ai-ghost-ai`** – der KI-Aktions-Port aus IP-17, unverändert und in diesem Feature nicht
  verdrahtet.
* **`app/ui`** – `BookPartEditor` bettet `PaperSheetView` (`EDITABLE`) ein und hält den Draht
  zwischen dem `Document` und den `List<String>`-Absätzen des Modells; `Inspector`; der
  Schreib-/Vorschau-Umschalter mit einer zweiten `PaperSheetView` (`READONLY`) über dem ganzen Buch;
  die schwebende KI-Leiste über `FloatingOverlay`; Undo/Redo auf dem `Document`-Tausch;
  `FontCatalog`/`FontResolver`/`FontResolution` (aus `lib/ai-ghost-layouting-fx` hierher gezogen);
  der Metrik-Fingerabdruck (`FontIdentity`, `FontIdentityCheck`) auf der simPlay-Messung; die
  Überschreibung der `paper-sheet-view`-`-fx-`-Eigenschaften mit der ai-ghost-Palette.

**Modellstand.** Keine Modellerweiterung mehr nötig: `PageFormat`, die Zeilenabstände, die
`included`-Schalter, die `Editor`-Präferenzgruppe bestehen (IP-02/IP-24/IP-10). Der Editor-View-Zustand
(Splitter, Einklappzustand, Modus, Leseposition) kommt mit IP-15/IP-16 zu `Preferences`. Der
Metrik-Fingerabdruck bleibt ein Feld von `FontData` (aus IP-22).

```text
ProjectProperty
  ├─ designProperty ─┬─> Inspector (Stilabschnitte)
  │                  └─> DocumentBuilder ─> simPlay Document ─┬─> PaperSheetView EDITABLE (Schreiben)
  │                                                           └─> PaperSheetView READONLY (Vorschau)
  ├─ bookProperty ──> BookPartEditor <──(Dokument-Tausch bei Bearbeitung)──> PaperSheetView
  └─ (Auswahl) ProjectList.selectedItem ──> EditorViewModel ──> gezeigter Teil
                                                   ▲
                          FontMeasureCalculator ───┘ (simplay-fx oder lokal, auf dem FX-Thread)
```

## 6. Übersicht der Implementierungspläne

Die Nummerierung bleibt stabil. IP-01, IP-02, IP-09, IP-12, IP-13, IP-14, IP-17, IP-19 und IP-24 sind
abgeschlossen und von der Abweichung nicht betroffen. IP-15, IP-16, IP-18 und IP-23 bleiben offen und
sind auf simPlay umgeschrieben. IP-29 bis IP-34 sind neu. Die Pläne IP-03, IP-04, IP-05, IP-06,
IP-07, IP-08, IP-10, IP-11, IP-22, IP-25 und IP-26 sind abgelöst (siehe „Abgelöste Pläne“).

| ID    | Implementierungsplan                          | Ziel                                                                          | Abhängigkeiten        |
|-------|-----------------------------------------------|------------------------------------------------------------------------------|-----------------------|
| IP-01 | Font Discovery And Text Measuring ✅ (Teilabl.) | Installierte Familien, Auflösung, Fallback; Messung wandert zu simPlay/IP-34  | -                     |
| IP-02 | Design Page Format Model ✅                     | Seitenformat, Ränder, Abstände in `Design`, gespiegelt                       | -                     |
| IP-24 | Optional Parts In The Model ✅                  | Prolog, Epilog, Klappentext immer vorhanden und schaltbar                    | -                     |
| IP-09 | Undo And Redo Infrastructure ✅                 | Undo-Stack über die Modelländerungen des Editors                            | -                     |
| IP-12 | Inspector Shell And Content Sections ✅         | Kontextfenster mit Buch- und Teilabschnitten                                | -                     |
| IP-13 | Design Style Sections ✅                        | Bearbeiten der Stile im Inspector mit Live-Wirkung                          | IP-02, IP-12          |
| IP-14 | Project Settings Dialog ✅                      | Seitenformat, Ränder und Leerseiten in einem Dialog                         | IP-02                 |
| IP-17 | AI Action Port ✅                               | Aktionsschnittstelle in `lib/ai`, keine Implementierung                     | -                     |
| IP-19 | AI Part Generation (nur Schaltfläche) ✅        | KI-Schaltfläche im Inspector, ruft leere `*View`-Methode mit `TODO(...)`     | IP-12                 |
| IP-29 | simPlay Integration                            | Repository, Abhängigkeit, Lizenz-Allowlist, CI-Token; Eigenbaumodule löschen | -                     |
| IP-30 | Book To simPlay Document Builder               | `lib/ai-ghost-layouting-model` auf das simPlay-Rohmodell umstellen           | IP-29, IP-02, IP-24   |
| IP-34 | Font Discovery And Metric Fingerprint On simPlay | Katalog/Auflösung nach `app/ui`; Fingerabdruck auf simPlay-Messung          | IP-29                 |
| IP-31 | Writing Surface On PaperSheetView              | `BookPartEditor` bettet `PaperSheetView` (`EDITABLE`) ein; Dokument-Sync     | IP-30, IP-09, IP-34   |
| IP-32 | Paragraph Structure Operations On Document     | Absätze teilen, verbinden, löschen, umsortieren auf dem `Document`          | IP-31                 |
| IP-33 | Undo On Immutable Document Swap                | Undo-Einträge auf den `Document`-Tausch umstellen                          | IP-31                 |
| IP-15 | Editor Arrangement And Tree Routing            | Drei Zonen, Routing jedes Baumknotens, View-Zustand persistiert            | IP-31, IP-12          |
| IP-16 | Writing And Preview Modes                      | Modus-Schalter, Vorschau des ganzen Buches über `PaperSheetView READONLY`   | IP-30, IP-31, IP-15   |
| IP-18 | AI Actions On Paragraph And Heading            | Schwebende KI-Leiste über `FloatingOverlay`; Schaltflächen mit `TODO(...)`   | IP-31                 |
| IP-23 | Optional Book Parts In The Tree               | Kontrollkästchen schaltet Prolog, Epilog und Klappentext ins Buch          | IP-15, IP-24          |

### Abgelöste Pläne

Mit dem Grund festgehalten, da die Dateien entfernt sind und die Frage sonst wiederkehrt.

* **IP-03 Layout Core** – ersetzt durch `simplay-engine` (Rohmodell, `SimpLayEngine`,
  `LineBreakerStrategy`, Ausrichtung). `lib/ai-ghost-layouting` wird per `git rm` entfernt (IP-29).
* **IP-04 Pagination And Page Break Policy** – `FlowPage`/`SinglePage` und der Seitenfluss kommen aus
  `simplay-engine`. Seitennummerierung, gespiegelte Ränder, inaktive/leere Seiten und die
  Klappentext-Kante liefert simPlay nicht; diese Politik fügt der Nutzer nachträglich in simPlay ein
  (Abschnitt 9, TODO). IP-30 trägt bis dahin nur eine Zwischenlösung.
* **IP-05 Incremental Layout And Caching** – `simplay-engine.measure` ist ein deterministischer
  Einzelaufruf; `PaperSheetView` hält die Tipp-Reaktion im `EDITABLE`-Modus selbst. Das
  Antwortverhalten wird in IP-31/IP-16 gemessen statt eigen gecacht.
* **IP-06 Layout Regression Harness** – simPlay besitzt die Layout-Korrektheit. Schreibfläche und
  Vorschau sind dieselbe Komponente über demselben `Document`, sodass sie nicht auseinanderdriften
  können; die Golden-File- und Oberflächenvergleich-Tests (`*RT`) und die `regressionTest`-Aufgaben
  entfallen mit den Modulen. Eine dünne Prüfung „gleicher Absatz, gleiche Seite“ liegt bei IP-16.
* **IP-07 Paper Page View** – ersetzt durch `PaperSheetView` im Modus `READONLY` (IP-16).
* **IP-08 Paper Flow View** – ersetzt durch `PaperSheetView` im Modus `EDITABLE` (IP-31);
  `PaperFlowListener` weicht `CaretModel`/`TextSelectionModel` plus eigenen Tastenhandlern.
* **IP-10 Book Part Writing Surface** – neu als IP-31 auf `PaperSheetView`.
* **IP-11 Paragraph Structure Operations** – neu als IP-32 auf dem simPlay-`Document`.
* **IP-22 Font Identity And Substitution Reporting** – neu als IP-34: der Fingerabdruck wird aus der
  simPlay-Messung gebildet (derselbe `FontMeasureCalculator`), nicht aus eigenem JavaFX-Messcode. Das
  `FontData`-Feld und der Vergleich in `FontIdentity` bleiben.
* **IP-25 Renderer Library Module** – hinfällig: kein neues Bibliotheksmodul. Die JavaFX-Erlaubnis
  für ein `lib`-Modul in `.claude/rules/architecture.md` wird von IP-29 zurückgenommen.
* **IP-26 Font And Measuring Migration** – hinfällig: `lib/ai-ghost-layouting-fx` wird entfernt statt
  bestückt; die Schriftklassen ziehen nach `app/ui` (IP-34).

## 7. Implementierungspläne

Jeder offene Plan ist unter `.claude/plans/implementation` ausgeschrieben, mit seinen Aufgaben, seinen
Einschränkungen und seinen Tests. Hier benannt sind nur die Grenze des Plans und die Begründung
dahinter.

### IP-01: Font Discovery And Text Measuring ✅ (teilweise abgelöst)

Abgeschlossen, in `app/ui`. Familienermittlung, Auflösung und Fallback bleiben gültig und ziehen mit
IP-34 in ein eigenes Paket. Der Messteil (`JavaFxTextMetrics`) ist durch den `FontMeasureCalculator`
von simPlay abgelöst.

### IP-02: Design Page Format Model ✅

Abgeschlossen. Ränder sind innen/außen statt links/rechts; `mirroredMargins` (Vorgabe `false`) steht
im Modell. IP-30 rechnet daraus je Seite ein `PageLayout` (`Size`, `Margins`) für simPlay.

### IP-24: Optional Parts In The Model ✅

Abgeschlossen. `included` sitzt auf `Prolog`, `Epilog`, `Blurb`. Da nichts gelöscht wird, braucht
IP-23 keine Bestätigung und keinen wiederherstellenden Undo-Eintrag.

### IP-09: Undo And Redo Infrastructure ✅

Abgeschlossen, in `app/ui/.../undo` (`UndoEntry`, `PropertyUndoEntry`, `UndoStack`, im Besitz von
`MainWindowViewModel`), mit benanntem Tooltip je Eintrag und Verlaufs-Dropdown. IP-33 stellt die
Einträge der Schreibfläche auf den `Document`-Tausch um; die Infrastruktur selbst bleibt.

### IP-12: Inspector Shell And Content Sections ✅

Abgeschlossen. Feste `TitledPane`-Abschnitte „Book“, „Chapter“, „Design“. `ChapterProperty.of(
chapter)` als öffentliche Factory in `lib/fx-model`.

### IP-13: Design Style Sections ✅

Abgeschlossen. Der Abschnitt „Design“ schreibt dieselbe `DesignProperty`, die IP-30 liest, sodass das
Live-Update ohne zusätzliche Verkabelung wirkt: Stiländerung → `Document` neu → simPlay misst neu →
`PaperSheetView` zeichnet neu.

### IP-14: Project Settings Dialog ✅

Abgeschlossen. Master-Detail-Hülle; der echte Editor sitzt am `Design`-Knoten (Seitengröße, vier
Ränder in Millimetern, zwei Leerseiten-Flags), gebunden an eine Arbeitskopie-`DesignProperty`.

### IP-17: AI Action Port ✅

Abgeschlossen, in `lib/ai` (`org.pcsoft.app.aighost.ai.action`). Der Port bleibt unverändert und wird
in diesem Feature nicht verdrahtet.

### IP-19: AI Part Generation (nur Schaltfläche) ✅

Abgeschlossen. Schaltfläche „Kapitel generieren“ im Inspector-Abschnitt „Chapter“, per FXML
`onAction="#generatePart"` an `InspectorView.generatePart()` mit Rumpf `TODO("AI action:
generate-part")`.

### IP-29: simPlay Integration

Plan: `FP-001-IP-29-SimPlayIntegration.md`

Die eine Stelle, an der die neue Drittanbieter-Abhängigkeit eingeführt wird. Das GitHub-Packages-
Repository braucht ein Token lokal und in der CI; ohne diesen Plan scheitert jeder folgende. Die
Eigenbaumodule werden hier per `git rm` entfernt, damit kein toter Code stehen bleibt, und die
Architekturregel wird im selben Zug zurückgenommen. Die Lizenz-Allowlist wächst um simPlay und seine
transitiven Abhängigkeiten; fehlende Einträge werden dem Nutzer vorgelegt.

### IP-30: Book To simPlay Document Builder

Plan: `FP-001-IP-30-BuchZuSimPlayDokument.md`

Das einzige ai-ghost-Modul, das beide Seiten kennt: `Book`/`Design`/`Meta` hinein, ein simPlay-
`Document` heraus. Ein `FlowPage` je Buchteil hält den Teilbeginn auf einer eigenen Seite, ohne dass
der Übersetzer paginieren muss. Die Seitenpolitik, die simPlay heute nicht trägt (Nummerierung,
gespiegelte Ränder, inaktive/leere Seiten, Klappentext-Kante), bleibt bewusst dünn und als TODO
markiert – der Nutzer bringt sie nach simPlay ein, und der Übersetzer übernimmt sie dann von dort.

### IP-34: Font Discovery And Metric Fingerprint On simPlay

Plan: `FP-001-IP-34-SchriftUndFingerabdruckAufSimPlay.md`

Der Fingerabdruck erfasst genau das, was das Layout beeinflusst, und wird aus derselben Messung
gebildet, die simPlay verwendet – nicht aus einem zweiten Messpfad, der anders antworten könnte.
Referenzzeichensatz (ASCII, Latin-1, Latin Extended-A, Kyrillisch) und Größe (12 pt) bleiben für alle
Zeiten fest, sonst meldet jedes ältere Projekt eine falsche Abweichung. Familienermittlung und
Fallback sind JavaFX-Wissen und ziehen nach `app/ui`, bis simPlay sie exponiert; dann können sie
dorthin wandern (TODO, Abschnitt 9).

### IP-31: Writing Surface On PaperSheetView

Plan: `FP-001-IP-31-SchreibflaecheAufPaperSheetView.md`

`PaperSheetView` besitzt Cursor, Auswahl und die Bearbeitung; der Verbraucher besitzt den Text. Eine
Bearbeitung ersetzt `document` durch eine neue Instanz – die alte bleibt unangetastet, was Undo
(IP-33) zu einem reinen Instanz-Tausch macht. Der Draht zwischen dem `Document` und den
`List<String>`-Absätzen des Modells liegt hier; alles ai-ghost-Spezifische wird über simPlay-API
beantwortet, nie über eine Abhängigkeit zurück aus simPlay. Die Überschreibung der
`paper-sheet-view`-`-fx-`-Eigenschaften mit der ai-ghost-Palette gehört ebenfalls hierher (früher
IP-27).

### IP-32: Paragraph Structure Operations On Document

Plan: `FP-001-IP-32-AbsatzOperationenAufDokument.md`

Teilen, Verbinden, Löschen und Umsortieren sind Operationen auf der `TextBlock`-Liste eines `Page`;
das Ergebnis ist ein neues `Document`. `CaretModel` adressiert Blöcke, Wörter und Symbole und ersetzt
die `onSplitRequested`/`onMergeRequested`/`onMoveRequested`-Callbacks des früheren
`PaperFlowListener`. Enter teilt, Backspace am Blockanfang verbindet, Strg+Umschalt+Pfeil sortiert um
– als eigene Tastenhandler über der Komponente, da ein Block ein Absatz ist, kein mehrzeiliges Feld.

### IP-33: Undo On Immutable Document Swap

Plan: `FP-001-IP-33-UndoAufDokumentTausch.md`

Der Tausch der `Document`-Instanz ist die natürliche Undo-Einheit. Ein Textänderungs-Eintrag und ein
struktureller Eintrag (früher `ParagraphListUndoEntry`) merken sich Vorher- und Nachher-`Document`
plus das Caret-Ziel und spielen den Tausch in beide Richtungen ab. Merge-Schlüssel und Tipp-Pause aus
IP-09/IP-10 bleiben.

### IP-15: Editor Arrangement And Tree Routing

Plan: `FP-001-IP-15-EditorAufteilungUndBaumRouting.md`

`ProjectList` behält seine API; das Routing ist ein erschöpfendes `when` in `EditorViewModel`, sodass
ein Knoten, der später eine Bedeutung bekommt, ein Compilerfehler ist. View-Zustand geht zu
`Preferences`, nie in das Projektdokument. Von der Abweichung unberührt.

### IP-16: Writing And Preview Modes

Plan: `FP-001-IP-16-SchreibUndVorschauModus.md`

Die Vorschau ist eine zweite `PaperSheetView` im Modus `READONLY` über dem `Document` des ganzen
Buches; die Seitenvirtualisierung bringt die Komponente mit. Ein Absatz fällt in beiden Modi auf
dieselbe Seite, weil beide dasselbe `Document` und dieselbe Messung verwenden – kein Vergleich nötig.
Die FX-Thread-Kosten des ersten `measure`-Aufrufs eines langen Buches werden mit einer
Fortschrittsanzeige begleitet, nicht weggehofft. Die Leseposition ist eine Absatzreferenz, kein
Scroll-Versatz.

### IP-18: AI Actions On Paragraph And Heading

Plan: `FP-001-IP-18-AiAktionenAmAbsatz.md`

Die schwebende Leiste wird als simPlay-`FloatingOverlay` gebaut (Trigger `PARAGRAPH_HOVER`, im
`EDITABLE`-Modus zusätzlich `CARET`), sodass simPlay das Anzeigen, Positionieren und Verbergen
übernimmt. Umschreiben, Ausbauen und Kürzen sind Schaltflächen mit Icons und Hover-/Fade-Verhalten,
jede per FXML `onAction` an eine parameterlose `*View`-Methode mit Rumpf `TODO("AI action: …")`. Keine
Verdrahtung an den Port aus IP-17, kein Stub, kein Provider.

### IP-23: Optional Book Parts In The Tree

Plan: `FP-001-IP-23-OptionaleTeileImBaum.md`

Der eine Plan, der den Projektbaum ändert, und bewusst eng: Struktur und `selectedItem`-API bleiben,
ein Kontrollkästchen wird auf genau drei Knoten hinzugefügt. `CheckBoxTreeItem` wendet seinen Haken
standardmäßig auf den Teilbaum an, was eingeschränkt werden muss. Das Ausgrauen zieht sofort nach; die
Neunummerierung folgt, sobald die simPlay-Seitenpolitik steht (bis dahin TODO).

## 8. Abhängigkeitsgraph

```text
IP-29 ──┬─> IP-30 (mit IP-02✅, IP-24✅) ──┬─> IP-31 (mit IP-09✅, IP-34) ──┬─> IP-32
        │                                  │                                ├─> IP-33
        └─> IP-34 ──────────────────────────┘                                ├─> IP-18
                                                                            └─> IP-15 (mit IP-12✅) ──┬─> IP-16 (mit IP-30)
                                                                                                      └─> IP-23 (mit IP-24✅)
IP-02✅ ──> IP-13✅, IP-14✅
IP-12✅ ──> IP-13✅, IP-19✅, IP-15
IP-17✅  (Port bleibt für spätere Wiederverwendung; nicht verdrahtet)
```

Ein einziger Strang: IP-29 führt simPlay ein und räumt den Eigenbau ab. IP-30 stellt den Übersetzer
um, IP-34 den Schrift-Stack. Auf beiden ruht IP-31, die Schreibfläche auf `PaperSheetView`; aus ihr
wachsen die Absatz-Operationen (IP-32), das umgestellte Undo (IP-33), die KI-Leiste (IP-18) und die
Editor-Aufteilung (IP-15), aus der wiederum der Vorschaumodus (IP-16) und das Kontrollkästchen im
Baum (IP-23) folgen.

Abgeschlossen und unberührt: **IP-01** ✅ (teilweise abgelöst), **IP-02** ✅, **IP-24** ✅,
**IP-09** ✅, **IP-12** ✅, **IP-13** ✅, **IP-14** ✅, **IP-17** ✅, **IP-19** ✅.
Unabhängiger Ausgangspunkt der Abweichung: **IP-29**.

## 9. Risiken und offene Fragen

* **simPlay ist eine neue Drittanbieter-Abhängigkeit.** Vom Nutzer bestätigt (vollständiger Ersatz).
  Bezug über GitHub Packages mit Token; die CI braucht ein Secret. Ohne Netz oder Token scheitert der
  Build. IP-29 behandelt Repository, Token und einen möglichen `publishToMavenLocal`-Weg für die
  Entwicklung.
* **ai-ghost-Seitenpolitik fehlt in simPlay – TODO.** Seitennummerierung (ungezählte
  Titel-/Copyright-Seite, Neunummerierung beim Schalten eines optionalen Teils), gespiegelte Ränder
  (recto/verso), inaktive Seiten eines ausgeschalteten Teils, führende/abschließende Leerseiten und
  die harte Kante des Klappentexts sind in `simplay-engine` nicht vorgesehen. **Der Nutzer fügt diese
  Politik nachträglich in simPlay ein.** Bis dahin trägt IP-30 nur eine Zwischenlösung (feste Ränder,
  keine Nummerierung oder eine triviale fortlaufende), und IP-23 kann das Ausgrauen zeigen, aber
  nicht neu nummerieren. Betroffene Abschnitte des Zielzustands stehen unter diesem Vorbehalt.
* **Stellt `simplay-fx` einen JavaFX-`FontMeasureCalculator` bereit?** Nicht aus der Doku belegt.
  IP-34 prüft das an der Artefakt-/Quelllage; falls nein, trägt `app/ui` einen kleinen lokalen
  Rechner auf einem verborgenen `javafx.scene.text.Text`-Knoten – derselbe, mit dem der frühere
  `JavaFxTextMetrics` gemessen hat.
* **Exponiert `simplay-fx` eine Familienermittlung?** Die Doku nennt keine. Bis dahin bleiben
  `FontCatalog`/`FontResolver` ai-ghost-Code in `app/ui`; wandert die Fähigkeit später nach
  `simplay-fx`, kann ai-ghost sie übernehmen (TODO).
* **JPMS und jlink.** `simplay-engine` ist ein Kotlin-Multiplatform-Artefakt (JVM-Variante),
  `simplay-fx` exportiert JavaFX transitiv. Die Modulnamen für `module-info.java` von
  `lib/ai-ghost-layouting-model` und `app/ui` und die `jlink`-Einbindung (`addExtraDependencies`) sind
  in IP-29 zu klären; das Laufzeit-Image muss weiter bauen.
* **Reife und Versionierung von simPlay.** Eine feste Version wird gepinnt. Ein Bruch in einer
  Minor-Version träfe den ganzen Renderpfad. IP-29 pinnt exakt und hält die Version an einer Stelle.
* **Das Messen gehört dem FX-Thread.** `simplay-engine.measure` läuft synchron; das erste Layout
  eines langen Buches kann das Fenster einfrieren. IP-16 begleitet es mit einer Fortschrittsanzeige
  und misst die Kosten, statt sie zu verbergen.
* **`PaperSheetView` im `EDITABLE`-Modus ist eine vollständige Editorkomponente.** Der Teil, der am
  ehesten ai-ghost-spezifisches Verhalten verlangt (Absatz-als-Einheit, Enter teilt statt Umbruch).
  Jeder solche Bedarf wird über die simPlay-API (`CaretModel`, `TextSelectionModel`, Tastenhandler
  darüber) beantwortet, nie über eine Erweiterung von simPlay im ai-ghost-Repository.
* **Rückabbildung Dokument → Modell.** `PaperSheetView` gibt bei einer Bearbeitung ein neues
  `Document` heraus; IP-31 muss daraus die `List<String>`-Absätze je Teil rekonstruieren. Ein harter
  Zeilenumbruch innerhalb eines `TextBlock` (im Modell heute nicht vorgesehen) würde diesen Weg
  stören.
* **Die Standardschrift eines neuen Projekts** bleibt offen: `FontData` fällt auf `Arial` zurück, das
  nicht überall installiert ist; eine über die Fallback-Kette auflösende Vorgabe wird benötigt.
* **Übereinstimmung pro Maschine.** IP-34 macht eine Ersetzung sichtbar; beseitigen kann sie sie
  nicht.
* **Kein KI-Provider wird mit diesem Feature ausgeliefert**, nicht einmal ein Stub. `lib/ai` (IP-17)
  trägt nur die Aktionsschnittstelle; sie bleibt ungenutzt für spätere Wiederverwendung. IP-18 und
  IP-19 bauen nur Schaltflächen, deren `onAction` auf eine leere `*View`-Methode mit `TODO(...)`
  zeigt. Jeder Provider kommt erst mit dem späteren Plugin-System-Feature. Harte Einschränkung: Kein
  Plan dieses Features fügt einen Stub, einen Mock-Provider oder irgendeine andere Interaktion mit
  einer tatsächlichen oder simulierten KI hinzu.
* **Modul-Entfernung.** `lib/ai-ghost-layouting` und `lib/ai-ghost-layouting-fx` werden per `git rm`
  entfernt; `settings.gradle.kts`, die `ci-pipeline`, README, MkDocs und `CHANGELOG.md` sind
  entsprechend zu prüfen (`ci-pipeline`- und `project-docs`-Skill).

### Abgelehnte Alternativen

* **Eigenbau behalten (FP-001 in seiner ursprünglichen Form).** Verworfen: simPlay löst genau dasselbe
  Problem (Rohmodell, Messung über Callback, Umbruchstrategien, Ausrichtung, `FlowPage`/`SinglePage`,
  JavaFX-`PaperSheetView` mit Editieren, Auswahl, Zoom, Virtualisierung, CSS und `FloatingOverlay`)
  und wird gepflegt.
* **Eigen-Engine als Fallback hinter einer Schnittstelle.** Verworfen (Nutzerentscheidung): doppelter
  Pfad, doppelte Pflege, und die Treuekette müsste für beide gelten.
* **Apache PDFBox / Apache FOP / `java.awt.font.TextLayout` / SWT `TextLayout`.** Schon in der
  ursprünglichen FP-001 abgelehnt (jeweils eigene Messung, kein passendes platziertes Ergebnis oder
  schwere, nicht-modulare Abhängigkeit). Unverändert gültig.

### Getroffene Entscheidungen

* **simPlay vollständig übernehmen**; `lib/ai-ghost-layouting` und `lib/ai-ghost-layouting-fx`
  entfernen; `lib/ai-ghost-layouting-model` als einzigen Übersetzer behalten und auf das
  simPlay-Rohmodell umstellen.
* **JavaFX nur noch in `app/ui`** (transitiv über `simplay-fx`); die JavaFX-Erlaubnis für ein
  `lib`-Modul in `.claude/rules/architecture.md` wird zurückgenommen.
* **Seitenformat** A5 als Vorgabe, weitere Vorgaben A4, 12,5 × 19 cm, 13,5 × 21,5 cm, 6 × 9 Zoll;
  Ränder 20 mm innen, 15 mm außen, 15 mm oben, 20 mm unten.
* **Das Vorwerk** ist die Titelseite, gefolgt direkt von der Copyright-Seite. **Der Klappentext** ist
  immer das letzte Blatt, durch eine harte Kante abgesetzt, ohne Seitenzahl.
* **Optionale Teile** beginnen immer auf einer eigenen Seite (ein `FlowPage` je Teil), behalten ihren
  Text, sind ausgegraut und bleiben beschreibbar.
* **Der Metrik-Fingerabdruck** wird über ASCII, Latin-1, Latin Extended-A und Kyrillisch bei 12 pt aus
  der simPlay-Messung genommen, mit Ascent, Descent und Leading. Menge und Größe sind fest.
* **ai-ghost-Seitenpolitik** (Nummerierung, gespiegelte Ränder, inaktive/leere Seiten,
  Klappentext-Kante) wird vom Nutzer nachträglich in simPlay eingebracht; ai-ghost trägt bis dahin
  nur eine Zwischenlösung und markiert die Lücke als TODO.

### Bewusst außerhalb des Umfangs

* **Export in jeglicher Form.** Ein eigenes Feature – als Plugin auf Apache PDFBox oder über das
  simPlay-Modul `j-pdf`. Was dieses Feature ihm schuldet, ist ein serialisierbares, toolkit-freies
  simPlay-Rohdokument.
* **Plugin-Infrastruktur.** `ai-ghost-plugin-api` trägt nur `ProjectPart` und `ProjectPartInfo`; kein
  Loader, keine Service-Registrierung. Voraussetzung eines plugin-basierten Exports, nicht dieses
  Features.
* **Jeder KI-Provider, Stub oder echt.** Siehe Abschnitt 9. `lib/ai` trägt nur die
  Aktionsschnittstelle (IP-17); keine Implementierung wird geschrieben, getestet oder verdrahtet.
* **Die ai-ghost-Seitenpolitik in simPlay selbst.** Der Nutzer bringt sie dort ein; dieses Feature
  konsumiert sie nur, sobald sie steht.

## 10. Abschlusskriterien des Features

* Titelseite, Copyright-Seite, Prolog, jedes Kapitel, Epilog und Klappentext können in der Anwendung
  vollständig geschrieben werden; der Platzhalter „Not implemented yet.“ ist weg.
* `lib/ai-ghost-layouting` und `lib/ai-ghost-layouting-fx` sind aus dem Repository und aus
  `settings.gradle.kts` entfernt; der Build kennt sie nicht mehr.
* `lib/ai-ghost-layouting-model` baut aus `Book`, `Design` und `Meta` ein simPlay-`Document` und
  hängt von `simplay-engine`, nicht von JavaFX ab.
* Schreibfläche und Vorschau sind `PaperSheetView` in den Modi `EDITABLE` und `READONLY` über
  demselben `Document`; ein Absatz fällt in beiden auf dieselbe Seite.
* Prolog, Epilog und Klappentext werden aus dem Baum geschaltet; ein ausgeschalteter Teil behält
  seinen Text, ist ausgegraut und bleibt beschreibbar. (Neunummerierung und Klappentext-Kante gelten,
  sobald die simPlay-Seitenpolitik steht; bis dahin als TODO dokumentiert.)
* Ein vor diesem Feature geschriebenes Dokument öffnet mit genau den Teilen, die es früher hatte.
* Text wird in der Typografie, den Rändern und der Seitenstruktur des Buches geschrieben, mit echten
  Blattgrenzen dort, wo das gedruckte Buch umbricht.
* Schriften kommen aus den installierten Familien; keine Manuskriptschrift wird ausgeliefert und keine
  Schriftdatei geöffnet. Eine fehlende oder anders messende Schrift wird mit ihrem Ersatz gemeldet,
  gebildet aus der simPlay-Messung.
* Ein geänderter Designwert ändert den offenen Text, ohne das Projekt neu zu öffnen.
* Das Schreiben in einem buchgroßen Dokument bleibt reaktionsschnell, und das erste Layout eines
  langen Buches erscheint nicht als eingefrorenes Fenster.
* Prompts, Teildaten und Design sind neben dem Blatt erreichbar und unterbrechen den Text nie.
* Jede Textänderung und jede Strukturänderung kann rückgängig gemacht und wiederhergestellt werden;
  Undo tauscht die `Document`-Instanz zurück.
* Für einen Absatz, eine Überschrift und einen ganzen Teil ist je eine KI-Schaltfläche erreichbar
  (die schwebende Leiste über `FloatingOverlay`); jede ist per FXML `onAction` an eine leere,
  parameterlose `*View`-Methode mit Rumpf `TODO("AI action: …")` gebunden. Der `lib/ai`-Aktions-Port
  aus IP-17 bleibt ungenutzt stehen.
* JavaFX taucht in keinem Modul außer `app/ui` auf, und `.claude/rules/architecture.md` sagt das.
* simPlay und seine transitiven Abhängigkeiten stehen in der Lizenz-Allowlist; der Lizenzbericht
  scheitert nicht.
* Der Projektbaum behält seine Struktur und seine Auswahl-API; das Kontrollkästchen auf drei Knoten
  ist die einzige Ergänzung daran.
* Build und Tests sind grün, Dokumentation und Changelog sind gemäß dem `project-docs`-Skill
  aktualisiert, die Pipeline gemäß dem `ci-pipeline`-Skill geprüft.
