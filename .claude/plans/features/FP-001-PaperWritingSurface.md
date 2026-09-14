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

> **Zweite große Planabweichung (TextAnchor, ab simPlay 0.3.1).** Vom Nutzer bestätigt, zweite
> grundlegende Änderung: Es gibt kein pro Buchteil gebautes `Document` mehr. `PaperSheetView` zeigt
> dauerhaft das ganze Buch als ein einziges, frei editierbares `Document` in der Mitte des Editors; ein
> Klick im Projektbaum navigiert im gezeigten Dokument, statt ein anderes Dokument einzusetzen. Das
> ai-ghost-Modell wird umgekehrt: Nicht mehr das Modell (`List<String>` je Buchteil) ist die
> gespeicherte Quelle, aus der ein `Document` abgeleitet wird - das `Document` selbst ist die
> gespeicherte Quelle. Das Modell trägt nur noch, was kein Fließtext ist: Reihenfolge und Name der
> Kapitel im Baum, die Ein-/Ausschalter der optionalen Teile, Prompts, und eine stabile Anker-Kennung
> je Kapitel. simPlay 0.3.1 liefert dafür einen `TextAnchor`: eine im `Document` gespeicherte, beim
> Editieren mitwandernde Markierung, mit der ein Fragment (Titel, ein bestimmtes Kapitel, ...)
> wiedergefunden wird, obwohl sich Seiten und Blockgrenzen beim freien Schreiben verschieben. Statische
> Teile (Titelseite, Copyright, Prolog, Epilog, Klappentext) bekommen dieselbe feste Anker-Kennung, die
> `BookDocumentBuilder` bereits als Seiten-`id` verwendet (`title`, `copyright`, `prolog`, `epilog`,
> `blurb`); jedes Kapitel bekommt eine `UUID`, die im Modell gespeichert wird, statt sich über seine
> Listenposition zu identifizieren. Der `TextStyle` eines Blocks wird zwar mitgespeichert (`Document`
> ist ein vollständiges simPlay-Objekt), beim Laden aber verworfen und durch den aus `Design`
> berechneten Stil ersetzt, damit eine Design-Änderung weiterhin sofort auf dem ganzen, bereits
> geschriebenen Text wirkt. Abschnitt 2 bis 10 sind auf diesen Stand geschrieben; Abschnitt 6,
> „Abgelöste Pläne (TextAnchor)“ benennt die betroffenen Pläne. Die genaue API von `TextAnchor` ist zum
> Zeitpunkt dieser Planänderung nicht freigegeben (simPlay 0.3.1 steht noch aus); Abschnitt 9 hält das
> als offene Frage fest, ebenso die Jackson-Verträglichkeit des simPlay-`Document` in der bestehenden
> ZIP-Persistenz.

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
  `mirroredMargins`) und Zeilenabstände je Elementklasse (IP-02). `Preferences` trägt die Gruppe
  `Editor` (`paragraphMergePauseMillis`). **Von der TextAnchor-Abweichung betroffen:** `Book.title`/
  `titleAppendix`, das `BookPart`-Interface (`title`/`titleAppendix`/`paragraph`) und seine drei
  Implementierungen `Prolog`, `Epilog`, `Blurb` sowie `Chapter.title`/`titleAppendix`/`paragraph` und
  `Copyright` tragen heute noch den Fließtext selbst; `Prolog`/`Epilog`/`Blurb` tragen zusätzlich
  `included` (IP-24, bleibt als Schalter erhalten, das Textfeld daneben entfällt). IP-36 nimmt jedes
  dieser Textfelder heraus; was bleibt, ist in Abschnitt 3 benannt.
* `lib/ai-ghost-layouting` – Eigenbau-Satz-Engine (frühere IP-03/IP-04/IP-05/IP-06). **Wird mit
  dieser Abweichung per `git rm` entfernt.**
* `lib/ai-ghost-layouting-model` – Übersetzer von `Book`, `Design`, `Meta` auf die Engine. **Bleibt,
  wird auf das simPlay-Rohmodell umgestellt** (IP-30).
* `lib/ai-ghost-layouting-fx` – Eigenbau-JavaFX-Renderer: `FontCatalog`, `FontResolver`,
  `FontResolution`, `JavaFxTextMetrics`, `FontFingerprint(s)`, `PaperPageView`, `PaperFlowView`,
  `PaperFlowListener`, Regressionsprüfstand (frühere IP-07/IP-08/IP-25/IP-26; IP-06 zur Hälfte).
  **Wird mit dieser Abweichung per `git rm` entfernt.** simPlay 0.2.1 liefert Verfügbarkeitsprüfung
  und Metrik-Fingerabdruck selbst (`simplay-fx`-`FxFontProbe`); nur die Ersatzfamilie-Meldung und die
  Familienliste für die Auswahl ziehen nach `app/ui` (IP-34).
* `lib/ai-ghost-ai` – der KI-Aktions-Port aus IP-17 (`AiAction`, `AiActionRequest`, Callbacks,
  `AiActionLimits`, `ParagraphSplitter`), ohne Implementierung. Unverändert.
* `app/ui` – das einzige JavaFX-Modul, MVVM FX, jlink-Image. Trägt heute `FontIdentity`,
  `FontIdentityCheck`, `FontTranslation`, `Inspector` mit den Abschnitten „Book“/„Chapter“/„Design“
  (IP-12/IP-13), den Projekteinstellungsdialog (IP-14), Undo/Redo (IP-09), die KI-Schaltfläche „Kapitel
  generieren“ (IP-19). `BookPartEditor`/`BookPartEditorViewModel`/`BookPartEditorController` (IP-31)
  bauen für den im Baum gewählten Teil je ein eigenes, einseitiges `Document` und tauschen es bei jeder
  Auswahl aus; `splitParagraph`/`mergeParagraph`/`removeParagraph`/`moveParagraph` liegen fertig, aber
  unverdrahtet in `BookPartEditorController` (für IP-32 stehengelassen). **Von der
  TextAnchor-Abweichung abgelöst:** genau dieses Auswechseln des `Document` je Auswahl. IP-38/IP-39
  ersetzen es durch ein dauerhaft gezeigtes, einziges `Document` des ganzen Buches, in dem eine
  Baumauswahl nur noch navigiert.
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

Drei Zonen rechts des Projektbaums: das **Papier** dauerhaft in der Mitte (eine einzige `PaperSheetView`
von simPlay, die das ganze Buch als ein `Document` zeigt), ein einklappbarer **Inspector** rechts, der
alles trägt, was kein gedruckter Text ist, und ein **Ansichts-Schalter** zwischen `Schreiben`
(`EDITABLE`) und `Vorschau` (`SELECTABLE`) auf demselben `Document` - kein zweites `Document`, keine
zweite `PaperSheetView`, kein zweites `measure`.

* **Ein Dokument für das ganze Buch.** `Book.document: Document` ist der gespeicherte Zustand, nicht
  mehr abgeleitet. `BookDocumentBuilder` baut es genau einmal - bei einem neuen Projekt und bei einem
  Migrationsschritt für ein älteres Projekt (Abschnitt 4) - und reicht danach jede Design-Änderung nur
  noch als Stil-Auffrischung auf die bestehenden Blöcke durch (Abschnitt 5). Eine Baumauswahl tauscht
  das `Document` nicht mehr aus, sie navigiert `PaperSheetView` zu dem `TextAnchor`, der zum gewählten
  Knoten gehört.
* **`TextAnchor` (simPlay 0.3.1) hält die Fragmente wieder auffindbar.** Titel, Copyright, Prolog,
  jedes Kapitel, Epilog und Klappentext bekommen beim Bau des `Document` je einen Anker; ein Kapitel
  behält seinen Anker über eine im Modell gespeicherte `UUID`, die statischen Teile über eine feste
  Kennung (`title`, `copyright`, `prolog`, `epilog`, `blurb` - dieselben Werte, mit denen
  `BookDocumentBuilder` heute schon seine Seiten-`id`s vergibt). Der Anker wandert mit, wenn Text davor
  eingefügt, gelöscht oder umsortiert wird; das Modell muss dafür keine Position mehr führen.
* **Treuekette über simPlay.** `simplay-engine` verwandelt das Rohdokument (`Document` aus `Page`,
  `TextBlock`, `TextStyle`, `TextAnchor`) in ein `MeasuredDocument` mit absoluten Positionen;
  `simplay-fx` malt dieses Ergebnis in `PaperSheetView`. Schreibfläche und Vorschau sind derselbe
  `Document`-Zustand in den Modi `EDITABLE` und `SELECTABLE`, sodass ein Absatz in beiden auf derselben
  Seite liegt - strukturell, nicht durch einen Vergleich, und ohne zweiten Aufbau.
* **Messung.** simPlay misst über einen `FontMeasureCalculator`; `simplay-fx` stellt ihn intern
  bereit und rendert `PaperSheetView` selbst damit – `app/ui` instanziiert keinen eigenen. Keine
  Schriftdatei wird gelesen, geparst oder ausgeliefert. Familien kommen aus
  `javafx.scene.text.Font`.
* **Übereinstimmung pro Maschine.** Die Messung läuft lokal über JavaFX; `simplay-fx`s `FxFontProbe`
  stempelt beim Speichern einen Fingerabdruck und meldet beim Öffnen eine Abweichung
  (`MeasuredDocument.fingerprintDeviations`); den Namen der verwendeten Ersatzfamilie ergänzt
  `app/ui` (IP-34).
* **Der Übersetzer bleibt ai-ghost, baut aber nur noch einmal.** `lib/ai-ghost-layouting-model` baut
  aus `Book`, `Design` und `Meta` ein simPlay-`Document`: ein `FlowPage` je Buchteil in Reihenfolge,
  Titelseite und Copyright-Seite als eigene Seiten, der Klappentext als letztes Blatt, jede Seite mit
  ihrem `TextAnchor`. `PageLayout` (Größe, Ränder) je Seite wird aus `PageFormat` berechnet. Dieser Bau
  geschieht nur noch beim Anlegen eines neuen Projekts, beim Anlegen eines neuen Kapitels und bei der
  Migration eines älteren Projekts (IP-36/IP-37) - nicht mehr bei jeder Baumauswahl. Eine
  Design-Änderung baut nicht neu, sie ersetzt nur den `TextStyle` jedes Blocks anhand der Rolle seines
  Ankers (IP-38).
* **ai-ghost-Seitenpolitik, Stand simPlay 0.3.0.** simPlay liefert Zeilen-/Seitenumbruch,
  Ausrichtung, `FlowPage`/`SinglePage`, seit 0.3.0 zusätzlich `Document.numbering: PageNumbering`
  (Position, Startwert, `excludedPageIds`, `PageCountingMode` `CONTINUOUS`/`SKIP_EXCLUDED`,
  `textStyle`) und, in `simplay-fx`, `PaperSheetView.pageModes` mit `PageMode`
  (`HIDDEN`/`DISABLED`/`STATIC`/`SELECTABLE`/`NAVIGABLE`/`EDITABLE`) je `Page.id`. Seitennummerierung
  (ungezählte Titel-/Copyright-Seite, Neunummerierung beim Schalten eines optionalen Teils) ist damit
  lieferbar (IP-35 ✅); inaktive Seiten eines ausgeschalteten Teils folgen erst mit IP-16, der
  Buchvorschau mit allen Seiten gleichzeitig - `PageMode` greift dort, wo mehrere Seiten sichtbar
  sind, nicht im Editor mit seiner einen gezeigten Seite. Weiterhin **nicht**
  geliefert: gespiegelte Ränder (recto/verso), führende/abschließende Leerseiten, die harte Kante des
  Klappentexts. Diese Restpolitik wird **vom Nutzer nachträglich in simPlay eingefügt**; bis dahin
  trägt ai-ghost nur das Nötigste als Zwischenlösung und markiert die Lücke als TODO (Abschnitt 9).
* **Vorwerk.** Titelseite mit Titel, weiteren Titelzeilen und Autor, gefolgt direkt von der
  Copyright-Seite.
* **Optionale Teile.** Prolog, Epilog und Klappentext beginnen immer auf einer eigenen Seite, behalten
  immer ihren Text, bleiben beschreibbar, sind ausgegraut, wenn ausgeschaltet, und werden über
  `PageMode.DISABLED` wirklich inaktiv - sofort, nicht erst mit einer künftigen Buchvorschau, weil das
  ganze Buch inklusive aller Seiten immer schon gleichzeitig sichtbar ist (IP-23). Die Titel- und
  Copyright-Seite sind bereits seit IP-35 über `excludedPageIds` aus der Seitennummerierung genommen.
  Das Kontrollkästchen im Baum entscheidet über die Zugehörigkeit zum Buch, nicht über die Existenz.
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
* `PaperSheetView` zeigt immer das ganze Buch; eine Baumauswahl navigiert über den `TextAnchor` des
  gewählten Teils zu seiner Stelle im Dokument, statt ein anderes Dokument einzusetzen.
* Die Vorschau ist derselbe `Document`-Zustand im Modus `SELECTABLE` statt `EDITABLE`; ein Absatz liegt
  in beiden Modi auf derselben Seite, weil es dasselbe `Document` und dieselbe Messung ist, kein
  zweiter Aufbau und kein Vergleich.
* Ein vor dieser Abweichung geschriebenes Projekt öffnet mit genau dem Text, den es vorher hatte: eine
  einmalige Migration baut sein `Document` aus den bisherigen `List<String>`-Feldern.

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
* **Umgekehrt zur ursprünglichen Fassung dieses Feature-Plans:** `Book.document: Document` ist der
  gespeicherte Zustand des Fließtexts; kein Buchteil trägt seinen Text mehr als `List<String>`. Das
  Modell trägt nur, was kein Fließtext ist: Kapitelname, Kapitel-`UUID`, die Ein-/Ausschalter der
  optionalen Teile, Prompts. View-Zustand (Splitter, Einklappzustand, Ansicht, Lese-/Schreibposition)
  bleibt in `Preferences`, nie im Projektdokument.
* Das im `Document` gespeicherte `TextStyle` jedes Blocks ist beim Laden nicht bindend: Es wird sofort
  durch den aus `Design` berechneten Stil der Rolle seines Ankers ersetzt, damit eine Design-Änderung
  auch rückwirkend auf bereits geschriebenem Text wirkt.
* Ein Projekt, das vor dieser Abweichung gespeichert wurde, trägt kein `Book.document`. Beim Öffnen
  wird es einmalig aus den bisherigen Feldern gebaut (`BookDocumentBuilder`) und ab dann als
  `Document` weitergeführt; die bisherigen Textfelder werden nicht weiter gepflegt.

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
* **`app/ui`** – eine dauerhafte `PaperSheetView` (`EDITABLE`/`SELECTABLE` auf demselben `Document`)
  zeigt das ganze Buch; `Inspector`; der Schreib-/Vorschau-Umschalter schaltet nur den `mode` derselben
  Ansicht um, baut kein zweites `Document`; die schwebende KI-Leiste über `FloatingOverlay`; Undo/Redo
  auf dem `Document`-Tausch; `FontIdentity`/`FontIdentityCheck` als dünne Hülle um `simplay-fx`s
  `FxFontProbe` (Verfügbarkeit, Fingerabdruck, Ersatzfamilie-Name, Familienliste über
  `javafx.scene.text.Font.getFamilies()`); die Überschreibung der `paper-sheet-view`-`-fx-`-
  Eigenschaften mit der ai-ghost-Palette. Eine Baumauswahl löst keinen `Document`-Tausch mehr aus,
  sondern eine Navigation über den `TextAnchor` des gewählten Knotens (IP-39).

**Modellstand.** `PageFormat`, die Zeilenabstände, die `included`-Schalter, die `Editor`-
Präferenzgruppe bestehen weiter (IP-02/IP-24/IP-10). Neu durch die TextAnchor-Abweichung: `Book`
verliert `title`/`titleAppendix`, das `BookPart`-Interface verliert `title`/`titleAppendix`/
`paragraph` (nur `prompts` bleibt an `Prolog`/`Epilog`/`Chapter`), `Chapter` verliert zusätzlich
`title`/`titleAppendix`/`paragraph`, behält aber `name` und bekommt eine neue, gespeicherte `id: UUID`
als Anker; `Copyright` verliert seinen Text ebenso. An ihrer Stelle trägt `Book` das neue Feld
`document: Document` (IP-36). Der Editor-View-Zustand (Splitter, Einklappzustand, Ansicht,
Ankerposition) kommt mit IP-39 zu `Preferences`. Der Metrik-Fingerabdruck bleibt ein Feld von
`FontData` (aus IP-22).

```text
ProjectProperty
  ├─ designProperty ─┬─> Inspector (Stilabschnitte)
  │                  └─> Stil-Auffrischung je Anker-Rolle ─> Book.document (bestehendes Document)
  ├─ bookProperty.documentProperty <──(direkter Sync bei jeder Bearbeitung)──> PaperSheetView
  │                                                                            (immer EDITABLE/SELECTABLE)
  └─ (Auswahl) ProjectList.selectedItem ──> TextAnchor auflösen ──> PaperSheetView navigiert
                                                   ▲
                          FxFontProbe (Fingerabdruck) ───┘ (simplay-fx-intern, auf dem FX-Thread)
```

Neuer Bau (`BookDocumentBuilder`) findet nur noch beim Anlegen eines Projekts, beim Anlegen eines
Kapitels und bei der einmaligen Migration eines älteren Projekts statt (IP-36/IP-37/IP-38); jede
Bearbeitung in `PaperSheetView` schreibt direkt in `Book.document` zurück, nicht mehr über einen
Baustein pro Auswahl.

## 6. Übersicht der Implementierungspläne

Die Nummerierung bleibt stabil. IP-01, IP-02, IP-09, IP-12, IP-13, IP-14, IP-17, IP-19 und IP-24 sind
abgeschlossen und von beiden Abweichungen unberührt. IP-29, IP-30, IP-34, IP-35 sind abgeschlossen und
bleiben in Kraft (ihre Bausteine werden von IP-36 bis IP-39 weiterverwendet, nicht ersetzt). IP-31 ist
abgeschlossen, aber sein Ergebnis - das `Document` je Baumauswahl auszutauschen - ist mit der zweiten
Abweichung abgelöst; seine reinen Funktionen (`splitParagraph` und Geschwister) bleiben nutzbar. IP-15
und IP-16 sind vollständig abgelöst und in IP-39 aufgegangen (siehe „Abgelöste Pläne (TextAnchor)“).
IP-32, IP-33, IP-18 und IP-23 bleiben offen, mit angepassten Abhängigkeiten. IP-36 bis IP-39 sind neu.
Die Pläne IP-03, IP-04, IP-05, IP-06, IP-07, IP-08, IP-10, IP-11, IP-22, IP-25 und IP-26 sind aus der
ersten Abweichung abgelöst (siehe „Abgelöste Pläne (simPlay)“).

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
| IP-29 | simPlay Integration ✅                          | Repository, Abhängigkeit, Lizenz-Allowlist, CI-Token; Eigenbaumodule löschen | -                     |
| IP-30 | Book To simPlay Document Builder ✅              | `lib/ai-ghost-layouting-model` auf das simPlay-Rohmodell umstellen           | IP-29, IP-02, IP-24   |
| IP-34 | Font Discovery And Metric Fingerprint On simPlay ✅ | `FxFontProbe` verdrahten; Ersatzfamilie und Familienliste in `app/ui`        | IP-29                 |
| IP-31 | Writing Surface On PaperSheetView ✅ (Ergebnis abgelöst) | Grundlagen (`splitParagraph` &c., Sync-Muster) bleiben; Verdrahtung siehe IP-39 | IP-30, IP-09, IP-34 |
| IP-35 | Page Numbering And Page Modes On simPlay 0.3.0 ✅ | Seitenzahl verdrahtet; `PageMode` für ausgeschaltete Teile jetzt sofort verdrahtbar | IP-30          |
| IP-36 | Model Umstellung Auf Anker-Struktur            | `Book`/`Chapter`/`BookPart`/`Copyright` vom Fließtext befreien, Kapitel-`UUID` | IP-24, IP-02          |
| IP-37 | Dokument-Persistenz Und Migration              | `Book.document: Document` speicherbar machen; Altprojekte migrieren         | IP-36, IP-29           |
| IP-38 | Buch-Dokument Als Alleinige Basis              | `BookDocumentBuilder` einzige Bauquelle; Anker statt Index; Stil-Auffrischung | IP-37, IP-30, IP-34   |
| IP-39 | PaperSheetView Dauerhaft Im Zentrum            | Ein `Document`, immer sichtbar; Baumauswahl navigiert über `TextAnchor`      | IP-38, IP-09          |
| IP-32 | Paragraph Structure Operations On Document     | Absätze teilen, verbinden, löschen, umsortieren, ankerfest                  | IP-39                 |
| IP-33 | Undo On Immutable Document Swap                | Undo-Einträge auf den `Document`-Tausch umstellen                          | IP-39                 |
| IP-18 | AI Actions On Paragraph And Heading            | Schwebende KI-Leiste über `FloatingOverlay`; Schaltflächen mit `TODO(...)`   | IP-39                 |
| IP-23 | Optional Book Parts In The Tree               | Kontrollkästchen schaltet Prolog, Epilog und Klappentext ins Buch, `PageMode` sofort | IP-39, IP-24, IP-35 |

### Abgelöste Pläne (TextAnchor)

Zweite Abweichung, Grund und Ersatz festgehalten, damit die Frage nicht zurückkehrt.

| ID    | Früherer Plan                              | Ersetzt durch                                              |
|-------|---------------------------------------------|--------------------------------------------------------------|
| IP-15 | Editor Arrangement And Tree Routing        | IP-39 (Navigation über `TextAnchor` statt Dokument-Tausch)    |
| IP-16 | Writing And Preview Modes                  | IP-39 (`mode`-Umschaltung auf demselben `Document`)           |

IP-31s konkretes Ergebnis - `BookPartEditor` baut und tauscht ein `Document` je Baumauswahl - ist damit
ebenfalls abgelöst; seine reinen Funktionen und das Muster, Bearbeitungen über einen
`documentProperty`-Listener zu erkennen, gehen unverändert in IP-39 über. IP-30s Bausteine
(`BookDocumentBuilder`, `TitlePageBuilder`, `CopyrightPageBuilder`, `BookPartBuilder`, `BlurbBuilder`,
`StyleTranslation`, `PageLayoutTranslation`) bleiben vollständig in Kraft - sie werden von IP-38 weiter
benutzt, nicht ersetzt.

### Abgelöste Pläne (simPlay)

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
* **IP-22 Font Identity And Substitution Reporting** – neu als IP-34: Verfügbarkeit und
  Fingerabdruck kommen aus `simplay-fx`s `FxFontProbe` (ab simPlay 0.2.1), nicht aus eigenem
  JavaFX-Messcode. Das `FontData`-Feld und der Vergleich in `FontIdentity` bleiben.
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

### IP-29: simPlay Integration ✅

Plan: `FP-001-IP-29-SimPlayIntegration.md` (abgeschlossen, Datei entfernt)

Die eine Stelle, an der die neue Drittanbieter-Abhängigkeit eingeführt wird. Das GitHub-Packages-
Repository braucht ein Token lokal und in der CI; ohne diesen Plan scheitert jeder folgende. Die
Eigenbaumodule werden hier per `git rm` entfernt, damit kein toter Code stehen bleibt, und die
Architekturregel wird im selben Zug zurückgenommen. Die Lizenz-Allowlist wächst um simPlay und seine
transitiven Abhängigkeiten; fehlende Einträge werden dem Nutzer vorgelegt.

Abweichungen bei der Umsetzung:

* Nur das simPlay-Repository und die exakt gepinnte Version (`0.2.1`, per `extra`) wurden eingeführt.
  Die `api`/`implementation`-Verdrahtung von `simplay-engine`/`simplay-fx` – und damit die
  Lizenzprüfung ihrer transitiven Abhängigkeiten – wandert nach IP-30 (`layouting-model`), IP-31 und
  IP-34 (`app/ui`), da ohne aufgelöste Abhängigkeit kein Lizenzbericht möglich ist. `licensee` läuft
  auf dem jetzigen Graphen grün.
* Gradle-`project(...)`-Verweise sind Konfigurationsfehler, nicht erst Kompilierfehler. Die Zeilen
  auf `:lib:ai-ghost-layouting[-fx]` in `app/ui/build.gradle.kts` und
  `lib/layouting-model/build.gradle.kts` mussten daher sofort entfernt werden; nur die Code-Verweise
  (Kotlin-Importe, `requires` in `module-info.java`) bleiben für IP-30/IP-31/IP-34 gebrochen.
* Der CI-Job `regression-test` wurde aus `ci.yml` und `release.yml` (auch aus `release.needs`)
  entfernt – beide `regressionTest`-Gradle-Tasks lagen in den gelöschten Modulen. Der
  GitHub-Packages-Zugang kam als `GITHUB_TOKEN`-Env in jeden Gradle-Job (`GITHUB_ACTOR` ist auf dem
  Runner ohnehin gesetzt), kein eigenes Secret.
* MkDocs trug keine Modulseiten für die entfernten Bibliotheken; nichts zu löschen. `CHANGELOG.md`
  blieb unberührt (keine endnutzersichtbare Änderung).
* Endstand des Builds: Konfiguration grün, alle nicht betroffenen Module grün;
  `ai-ghost-layouting-model` bricht an `requires org.pcsoft.app.aighost.layouting`, `app/ui` dahinter.

### IP-30: Book To simPlay Document Builder ✅ (abgeschlossen, Datei entfernt)

Das einzige ai-ghost-Modul, das beide Seiten kennt: `Book`/`Design`/`Meta` hinein, ein simPlay-
`Document` heraus. Ein `FlowPage` je Buchteil hält den Teilbeginn auf einer eigenen Seite, ohne dass
der Übersetzer paginieren muss. Die Seitenpolitik, die simPlay heute nicht trägt (Nummerierung,
gespiegelte Ränder, inaktive/leere Seiten, Klappentext-Kante), bleibt bewusst dünn und als TODO
markiert – der Nutzer bringt sie nach simPlay ein, und der Übersetzer übernimmt sie dann von dort.

Abweichungen bei der Umsetzung:

* Auf simPlay 0.2.2 gehoben; die Maven-Artefakt-ID trägt jetzt das Präfix `simplay-`
  (`simplay-engine-jvm` statt `engine-jvm`) - `lib/layouting-model/build.gradle.kts` folgt.
* `simplay-engine-jvm` 0.2.2 liefert weiterhin keinen echten Moduldeskriptor; `requires transitive
  org.pcsoft.framework.simplay.engine` im `module-info.java` bleibt auf das abgeleitete Automatik-
  Modul gerichtet (per `jar --describe-module` bestätigt). Der Name stimmt, ist aber nicht vertraglich
  stabil - offen für eine spätere simPlay-Version mit echtem `module-info`.
* Die Interims-`licensee`-Ausnahme für `engine`/`engine-jvm` entfällt mit 0.2.2s vollständigem
  `<licenses>`-Block; `licensee` läuft in `lib/ai-ghost-layouting-model` ohne Sonderregel grün.

### IP-34: Font Discovery And Metric Fingerprint On simPlay ✅

Plan: `FP-001-IP-34-SchriftUndFingerabdruckAufSimPlay.md` (abgeschlossen, entfernt)

Seit simPlay 0.2.1 liefert `simplay-fx`s `FxFontProbe` Verfügbarkeitsprüfung (`checkAvailability`)
und Metrik-Fingerabdruck (`fingerprint`/`verify`/`stamp`) selbst, aus demselben internen
`FontMeasureCalculator`, den `PaperSheetView` im Renderpfad verwendet – kein zweiter Messpfad mehr
nötig. Referenzzeichensatz und Normgröße sind jetzt `FontFingerprint.REFERENCE_GLYPHS`
(Lateinschrift, gängige Diakritika, Satzzeichen – **kein Kyrillisch**) und `NORMALIZED_SIZE` (100.0)
aus simPlay selbst, nicht mehr eigens von ai-ghost festgelegt; ob Ghost Writer Kyrillisch führt und
diese Lücke etwas bedeutet, ist offen (Abschnitt 9). `app/ui` ergänzt nur, was `FxFontProbe` nicht
liefert: den Namen der aufgelösten Ersatzfamilie (`FontSubstitution`, über
`javafx.scene.text.Font.font(family, weight, posture, size)`) und die Familienliste für die Auswahl
über `javafx.scene.text.Font.getFamilies()`.

Abweichungen vom ursprünglichen Plantext:

* `FontData.metrics: FontMetricsData?` (eigenes Vier-Felder-POJO mit eigenem FX-Modell) entfällt
  vollständig zugunsten von `FontData.fingerprint: String?`, dem rohen
  `FontFingerprint.encode()`-String; simPlays Fingerabdruck trägt andere Felder
  (`normalizedSize`/`ascent`/`descent`/`advances`) als das bisherige POJO
  (`widths`/`ascent`/`descent`/`leading`), ein eigenes Spiegel-POJO hätte keinen Mehrwert mehr
  gehabt. `FontMetricsDataProperty` entfällt ersatzlos, `FontDataProperty` trägt nur noch ein
  `fingerprintProperty: StringProperty`.
* `FontIdentityCheck.stamp`/`check` arbeiten weiterhin direkt auf den `StyleData`-Objekten des
  `Design`, nicht über `BookDocumentBuilder`/`Document` - das war schon vor IP-34 so und bleibt die
  einfachere Route, `FxFontProbe` nimmt pro Stil ein einzelnes `Font`.
* simPlay 0.2.2s veröffentlichte Gradle-Modul-Metadaten führen `simplay-common`
  (`FontAvailability`) nur in der `runtimeElements`-, nicht in der `apiElements`-Variante von
  `simplay-fx`; `app/ui` muss `org.pcsoft.framework:simplay-common` deshalb als eigene, vom Nutzer
  bestätigte Abhängigkeit führen, um dagegen zu kompilieren.

### IP-31: Writing Surface On PaperSheetView ✅ (Ergebnis abgelöst)

Plan: `FP-001-IP-31-SchreibflaecheAufPaperSheetView.md` (abgeschlossen, entfernt)

`PaperSheetView` besitzt Cursor, Auswahl und die Bearbeitung; der Verbraucher besitzt den Text. Eine
Bearbeitung ersetzt `document` durch eine neue Instanz – die alte bleibt unangetastet. Der Draht
zwischen dem `Document` und den `List<String>`-Absätzen des Modells lag hier; alles
ai-ghost-Spezifische wird über simPlay-API beantwortet, nie über eine Abhängigkeit zurück aus simPlay.
Die Überschreibung der `paper-sheet-view`-`-fx-`-Eigenschaften mit der ai-ghost-Palette gehört
ebenfalls hierher (früher IP-27) und bleibt unverändert in Kraft.

**Durch die TextAnchor-Abweichung abgelöst:** Genau das Muster "pro Teil ein eigenes einseitiges
`Document`, `paperSheetView.mode` wechselt zwischen `EDITABLE`/`READONLY`, Bearbeitungen werden über
einen `documentProperty`-`ChangeListener` erkannt und index-weise auf `targets` zurückgeschrieben" wird
von IP-39 ersetzt: es gibt nur noch ein `Document` für das ganze Buch, das nie ausgetauscht wird. Was
bleibt: `splitParagraph`/`mergeParagraph`/`removeParagraph`/`moveParagraph` in
`BookPartEditorController` als reine, bereits getestete Funktionen (IP-32 verdrahtet sie neu, jetzt
ankerfest), und der gemeldete Upstream-Fehler unten.

* **In simPlay 0.2.2 gefunden und dem Maintainer gemeldet:** `TextBlock.toString()` fügt beim
  Zusammensetzen ein Leerzeichen vor jedem `TextWord` ein, das nicht auf ein Leerzeichen im
  Originaltext zurückgeht, sobald ein Wort direkt auf ein Symbol folgt; ein angehängtes, noch
  alleinstehendes Leerzeichen wird beim Retokenisieren verschluckt. Beides bringt den in
  `DocumentEditor.splice()` berechneten `caretIndex` gegenüber dem tatsächlich gespeicherten Text aus
  dem Takt und lässt jedes weitere getippte Zeichen eine Position zu früh landen. **IP-32 tippt echte
  Trennzeichen und braucht den Fix upstream**, unabhängig von der TextAnchor-Abweichung.

### IP-36: Model Umstellung Auf Anker-Struktur

Plan: `FP-001-IP-36-ModellUmstellungAufAnkerStruktur.md`

**Ziel:** `lib/ai-ghost-model`/`lib/ai-ghost-fx-model` vom Fließtext befreien, den das `Document`
künftig allein trägt.

**Umfang:** `BookPart`-Interface verliert `title`/`titleAppendix`/`paragraph`, behält `prompts`.
`Chapter` verliert `title`/`titleAppendix`/`paragraph`, behält `name`/`prompts`, bekommt ein neues
Feld `id: UUID` als stabiler Anker (bei Neuanlage vergeben, danach unveränderlich). `Prolog`/`Epilog`
behalten `included`/`prompts`, verlieren ihren Text. `Book` verliert `title`/`titleAppendix`.
`Copyright` verliert seinen Text, behält, was kein Fließtext ist. Jede Spiegelung in `lib/fx-model`
folgt (`fx-model`-Skill). **Nicht** Teil dieses Plans: das neue `document`-Feld selbst (IP-37) und
seine Befüllung (IP-38).

**Abhängigkeiten:** IP-24, IP-02 (die Felder, die bestehen bleiben, sind dort entstanden).

**Technische Überlegungen:** Eine `Chapter`-`UUID` ist nur stabil, wenn sie nie aus der Listenposition
abgeleitet wird; sie entsteht einmal bei der Anlage und wird danach nur noch gelesen. Was ein Kapitel
"ist", ändert sich: von "Titel + Text" zu "Name im Baum + Anker in einem fremden Dokument" - eine
KDoc-Anpassung an `Chapter`/`BookPart` ist Teil dieses Plans (`project-docs`).

### IP-37: Dokument-Persistenz Und Migration

Plan: `FP-001-IP-37-DokumentPersistenzUndMigration.md`

**Ziel:** `Book.document: Document` wird Teil des gespeicherten Projekts; ein vor der Abweichung
gespeichertes Projekt öffnet unverändert.

**Umfang:** `Book` bekommt das Feld `document: Document`. `ProjectStorage`/`StorageIo` müssen ein
simPlay-`Document` lesen und schreiben können; `Document`/`Page`/`TextBlock`/`TextAnchor` sind auf
Kotlin-Multiplatform-Serialisierung (`kotlinx-serialization`) ausgelegt, die bestehende
Projekt-Persistenz auf Jackson (`StorageIo.loadFromZip`/`saveToZip`, `@JsonIgnoreProperties`) - ein
Jackson-Modul, eigene (De-)Serialisierer oder eine eingebettete kotlinx-Byte-/Text-Repräsentation
innerhalb des Jackson-Eintrags sind mögliche Wege, offen als Risiko in Abschnitt 9. Eine Migration
erkennt ein Projekt ohne `document` (ältere `Book.version`) und baut es einmalig über
`BookDocumentBuilder` aus den noch vorhandenen Alt-Feldern, bevor `IP-36`s Entfernung dieser Felder
wirksam werden kann - die Lesereihenfolge von Migration und Modell-Umstellung ist eine harte
Abhängigkeit dieses Plans von IP-36, nicht umgekehrt.

**Abhängigkeiten:** IP-36 (die Felder, aus denen migriert wird, existieren dort noch als
Übergangs-DTO), IP-29 (simPlay-Abhängigkeit).

**Technische Überlegungen:** Der Migrationsweg muss die alten Feldnamen so lange lesen können, wie ein
Projekt ohne `document` im Umlauf sein kann - die Übergangs-DTO trägt daher die entfernten Felder
weiter, nur als reines Lese-Modell für die Migration, nicht mehr als `Chapter`/`Prolog`/`Epilog`/
`Blurb`/`Book` selbst.

### IP-38: Buch-Dokument Als Alleinige Basis

Plan: `FP-001-IP-38-BuchDokumentAlsAlleinigeBasis.md`

**Ziel:** `BookDocumentBuilder` (IP-30) wird die einzige Stelle, die ein `Document` baut - beim neuen
Projekt, beim neuen Kapitel, bei der Migration (IP-37) - und dazu die Stelle, die den `TextStyle`
jedes Blocks anhand der Rolle seines Ankers aus `Design` auffrischt, statt das ganze `Document` neu zu
bauen.

**Umfang:** Jede Seite bekommt zusätzlich zu ihrer stabilen Seiten-`id` einen `TextAnchor` (Titel,
Copyright, Prolog, Epilog, Klappentext statisch; ein Kapitel über `chapter.id`, nicht mehr über
`chapter-<index>`). Eine neue Funktion liest beim Öffnen und nach jeder Design-Änderung jeden Block
über seinen Anker aus, bestimmt seine Rolle (Titel/Kapiteltitel/Kapiteltitel-Anhang/Fließtext) und
ersetzt seinen `TextStyle`, ohne den Text anzufassen. Anlegen eines neuen Kapitels erzeugt eine neue
`UUID`, baut seine leere `FlowPage` mit `TextAnchor` und fügt sie an der richtigen Stelle in
`Book.document` ein.

**Abhängigkeiten:** IP-37 (das `document`-Feld existiert), IP-30 (die Builder werden wiederverwendet,
nicht ersetzt), IP-34 (Schrift-Stack für die Stil-Auffrischung).

**Technische Überlegungen:** Die genaue API von `TextAnchor` (Zuordnung Rolle ↔ Anker, Verhalten beim
Verschieben von Text über eine Ankergrenze) ist erst mit simPlay 0.3.1 bekannt; dieser Plan wird beim
Start anhand der dann vorliegenden Dokumentation konkretisiert, wie schon IP-35 es für simPlay 0.3.0
tat.

### IP-39: PaperSheetView Dauerhaft Im Zentrum

Plan: `FP-001-IP-39-PaperSheetViewDauerhaftImZentrum.md`

**Ziel:** Ersetzt IP-15 (Routing) und IP-16 (Vorschaumodus) vollständig: eine einzige, dauerhaft
gezeigte `PaperSheetView` über `Book.document`; eine Baumauswahl navigiert, tauscht kein `Document`.

**Umfang:** Drei-Zonen-Aufbau (Baum, Blatt, Inspector) wie ursprünglich in IP-15 geplant, aber ohne
Dokument-Routing - `EditorViewModel` löst eine Baumauswahl stattdessen auf den `TextAnchor` des
Knotens auf und lässt `PaperSheetView` dorthin navigieren/scrollen. Jede Bearbeitung schreibt über
denselben `documentProperty`-Listener (aus IP-31 übernommen) direkt in `Book.document` zurück; eine
Struktur-Änderung, die die Kapitelliste betrifft (neues/gelöschtes Kapitel über den Baum), hält
`Book.chapters` mit der Ankerreihenfolge im `Document` synchron. Der Schreib-/Vorschau-Umschalter
schaltet nur `paperSheetView.mode` zwischen `EDITABLE` und `SELECTABLE` um, ohne zweites `Document`,
zweite View oder zweites `measure`. Splitter-Position, Einklappzustand, Ansicht und
Ankerposition kommen in `Preferences` (IP-15s ursprüngliche FX-Modell-Aufgabe).

**Abhängigkeiten:** IP-38 (das eine, dauerhafte `Document` existiert und trägt Anker), IP-09 (Undo).

**Technische Überlegungen:** Das erste `measure` eines langen Buches lief bisher nur pro Teil; mit
einem einzigen, immer angezeigten Gesamtdokument übernimmt dieser Plan die
Fortschrittsanzeige/Antwortverhalten-Sorge, die vorher IP-16 trug (Abschnitt 9).

### IP-32: Paragraph Structure Operations On Document

Plan: `FP-001-IP-32-AbsatzOperationenAufDokument.md`

Teilen, Verbinden, Löschen und Umsortieren sind Operationen auf der `TextBlock`-Liste einer `Page` des
einen, ganzen `Book.document`; das Ergebnis ist ein neues `Document`. `CaretModel` adressiert Blöcke,
Wörter und Symbole. Enter teilt, Backspace am Blockanfang verbindet, Strg+Umschalt+Pfeil sortiert um –
als eigene Tastenhandler über der Komponente, da ein Block ein Absatz ist, kein mehrzeiliges Feld.
**Neu durch die TextAnchor-Abweichung:** Keine Operation überschreitet die Grenze eines `TextAnchor` -
ein Merge am Kapitelanfang darf nicht in den letzten Absatz des vorigen Kapitels hineinlaufen, ein
Löschen darf den letzten Absatz eines Kapitels nicht mit dem Kapitel selbst verwechseln. Das Anlegen
und Entfernen eines ganzen Kapitels bleibt eine Operation des Projektbaums (IP-38/IP-39), keine
Absatz-Operation.

### IP-33: Undo On Immutable Document Swap

Plan: `FP-001-IP-33-UndoAufDokumentTausch.md`

Der Tausch der `Document`-Instanz ist die natürliche Undo-Einheit. Ein Textänderungs-Eintrag und ein
struktureller Eintrag (früher `ParagraphListUndoEntry`) merken sich Vorher- und Nachher-`Document`
plus das Caret-Ziel und spielen den Tausch in beide Richtungen ab. Merge-Schlüssel und Tipp-Pause aus
IP-09/IP-10 bleiben. Das "Modell", das nach einem Undo/Redo neu abgeleitet wird, ist jetzt nur noch
`Book.chapters`s Reihenfolge/Bestand aus den Ankern des wiederhergestellten `Document`, nicht mehr
`List<String>`-Absätze je Teil.

### IP-18: AI Actions On Paragraph And Heading

Plan: `FP-001-IP-18-AiAktionenAmAbsatz.md`

Die schwebende Leiste wird als simPlay-`FloatingOverlay` gebaut (Trigger `PARAGRAPH_HOVER`, im
`EDITABLE`-Modus zusätzlich `CARET`), sodass simPlay das Anzeigen, Positionieren und Verbergen
übernimmt. Umschreiben, Ausbauen und Kürzen sind Schaltflächen mit Icons und Hover-/Fade-Verhalten,
jede per FXML `onAction` an eine parameterlose `*View`-Methode mit Rumpf `TODO("AI action: …")`. Keine
Verdrahtung an den Port aus IP-17, kein Stub, kein Provider. Voraussetzung ist jetzt IP-39 statt IP-31,
inhaltlich unverändert.

### IP-35: Page Numbering And Page Modes On simPlay 0.3.0 ✅

Plan: `FP-001-IP-35-SeitenzahlUndSeitenModiAufSimPlay.md` (entfernt, umgesetzt)

simPlay 0.3.0 liefert `Document.numbering: PageNumbering` (Position, Startwert, `excludedPageIds`,
`PageCountingMode` `CONTINUOUS`/`SKIP_EXCLUDED`, `textStyle`) und, in `simplay-fx`,
`PaperSheetView.pageModes: Map<Page.id, PageMode>` (`HIDDEN`/`DISABLED`/`STATIC`/`SELECTABLE`/
`NAVIGABLE`/`EDITABLE`). `BookDocumentBuilder` (IP-30) setzt die Nummerierung aus `Design.pageNumbering`
und nimmt Titel-/Copyright-Seite über `excludedPageIds` heraus; jede Seite trägt dafür eine feste,
stabile `id` (`title`, `copyright`, `prolog`, `chapter-<n>`, `epilog`, `blurb`) statt simPlays
zufälliger Vorgabe. Löst den Nummerierungsteil der TODO aus Abschnitt 9 ab.
**Abweichung vom Plan, jetzt gegenstandslos:** `PageMode.DISABLED` wurde nicht in `BookPartEditor`
verdrahtet, weil der Editor je Auswahl nur ein Dokument mit einer Seite zeigte und die künftige
Buchvorschau (damals IP-16) das nachholen sollte. Mit der TextAnchor-Abweichung zeigt der Editor immer
alle Seiten gleichzeitig (IP-39); die Verdrahtung findet jetzt direkt in IP-23 statt, ohne eine
gesonderte Vorschau abzuwarten. Gespiegelte Ränder, Leerseiten und die Klappentext-Kante bleiben
weiterhin offen. `PaperSheetMode` wurde mit simPlay 0.3.0 zugleich umbenannt (`READONLY` →
`SELECTABLE`), in `BookPartEditorViewModel` und seinem Test nachgezogen.

### IP-23: Optional Book Parts In The Tree

Plan: `FP-001-IP-23-OptionaleTeileImBaum.md`

Der eine Plan, der den Projektbaum ändert, und bewusst eng: Struktur und `selectedItem`-API bleiben,
ein Kontrollkästchen wird auf genau drei Knoten hinzugefügt. `CheckBoxTreeItem` wendet seinen Haken
standardmäßig auf den Teilbaum an, was eingeschränkt werden muss. Das Ausgrauen zieht sofort nach.
**Nicht mehr aufgeschoben:** `PageMode.DISABLED`/`null` für einen ausgeschalteten Teil wird von diesem
Plan direkt verdrahtet - die frühere Vertagung auf eine künftige Buchvorschau entfällt, weil IP-39 alle
Seiten ohnehin immer gleichzeitig zeigt.

## 8. Abhängigkeitsgraph

```text
IP-24✅ ─┬─> IP-36 (mit IP-02✅) ──> IP-37 (mit IP-29✅) ──> IP-38 (mit IP-30✅, IP-34✅) ──> IP-39 (mit IP-09✅)
        │                                                                                   ├─> IP-32
IP-29✅ ─┴─> IP-30✅ (mit IP-02✅, IP-24✅) ─┬─> IP-35✅                                       ├─> IP-33
        └─> IP-34✅                        │                                                 ├─> IP-18
                                           └───────────────────────────────────────────────> IP-23 (mit IP-24✅, IP-35✅)
IP-02✅ ──> IP-13✅, IP-14✅
IP-12✅ ──> IP-13✅, IP-19✅
IP-17✅  (Port bleibt für spätere Wiederverwendung; nicht verdrahtet)
```

Seit der zweiten Abweichung ein anderer Strang: IP-36 löst den Fließtext aus dem Modell (auf IP-24/
IP-02 aufbauend), IP-37 macht das entstehende `Document` speicherbar und migriert Altprojekte, IP-38
macht `BookDocumentBuilder` zur einzigen, dauerhaften Dokumentbasis mit `TextAnchor` statt Index. Erst
danach folgt IP-39, die dauerhaft gezeigte `PaperSheetView` - sie ersetzt, was vorher IP-15 und IP-16
werden sollten. Aus IP-39 wachsen die Absatz-Operationen (IP-32), das umgestellte Undo (IP-33) und die
KI-Leiste (IP-18); IP-23 (Kontrollkästchen im Baum) hängt zusätzlich an IP-35 (`PageMode`), das seinerseits
unverändert an IP-30 hängt. IP-29, IP-30 und IP-34 bleiben der unveränderte Unterbau aus der ersten
Abweichung, deren Bausteine IP-38 weiterverwendet.

Abgeschlossen und unberührt: **IP-01** ✅ (teilweise abgelöst), **IP-02** ✅, **IP-24** ✅,
**IP-09** ✅, **IP-12** ✅, **IP-13** ✅, **IP-14** ✅, **IP-17** ✅, **IP-19** ✅, **IP-29** ✅,
**IP-30** ✅, **IP-34** ✅, **IP-35** ✅. Abgeschlossen, Ergebnis durch die zweite Abweichung abgelöst:
**IP-31** ✅ (Grundfunktionen bleiben nutzbar, siehe IP-32/IP-39). Vollständig abgelöst, keine Datei
mehr: **IP-15**, **IP-16** (siehe Abschnitt 6).

## 9. Risiken und offene Fragen

* **simPlay ist eine neue Drittanbieter-Abhängigkeit.** Vom Nutzer bestätigt (vollständiger Ersatz).
  Bezug über GitHub Packages mit Token; die CI braucht ein Secret. Ohne Netz oder Token scheitert der
  Build. IP-29 behandelt Repository, Token und einen möglichen `publishToMavenLocal`-Weg für die
  Entwicklung.
* **ai-ghost-Seitenpolitik, Reststand nach simPlay 0.3.0 – TODO.** Gespiegelte Ränder (recto/verso),
  führende/abschließende Leerseiten und die harte Kante des Klappentexts sind in `simplay-engine`
  weiter nicht vorgesehen. **Der Nutzer fügt diese Politik nachträglich in simPlay ein.**
  Seitennummerierung (`Document.numbering`, `excludedPageIds`, `PageCountingMode`) ist seit 0.3.0
  vorhanden und von IP-35 verdrahtet, nicht mehr als Zwischenlösung. Inaktive Seiten eines
  ausgeschalteten Teils (`PageMode.DISABLED`) sind ebenfalls seit 0.3.0 vorhanden und werden jetzt
  direkt von IP-23 verdrahtet, da IP-39 alle Seiten ohnehin gleichzeitig zeigt - die frühere Vertagung
  auf eine gesonderte Buchvorschau entfällt. Betroffene Abschnitte des Zielzustands stehen weiterhin
  unter dem Vorbehalt des verbleibenden Rests.
* **`TextAnchor` ist zum Zeitpunkt dieser Planänderung nicht freigegeben (simPlay 0.3.1 steht noch
  aus).** Diese Abweichung setzt voraus, dass ein Anker eine Textstelle über beliebige Einfügungen,
  Löschungen und Verschiebungen davor hinweg wiederfindet, dass ihm eine Rolle beigegeben werden kann
  (welcher Design-Stil gilt) und dass er persistierbar ist. IP-38 konkretisiert die genaue API erst,
  wenn 0.3.1 vorliegt, wie schon IP-35 es für 0.3.0 tat. Trifft die API die Annahmen nicht, ist IP-38
  neu zu bewerten, bevor IP-39 beginnt.
* **Was schützt einen Anker vor versehentlichem Löschen?** Löscht der Nutzer den gesamten Text eines
  Kapitels bis vor seinen Anker, oder markiert und löscht er über eine Kapitelgrenze hinweg, muss klar
  sein, ob der Anker (und damit das Kapitel) verschwindet oder stehen bleibt. IP-32/IP-39 legen fest,
  dass eine Absatz-Operation nie über eine Ankergrenze hinausgreift; ein ganzes Kapitel entfernt sich
  weiterhin nur über den Projektbaum, nie durch Text-Löschen im Blatt. Offen: ob das Entfernen eines
  Kapitels über den Baum eine Rückfrage braucht (anders als die textbehaltenden optionalen Teile aus
  IP-24, hier geht Text wirklich verloren) - vom Nutzer vor IP-39 zu bestätigen.
* **Jackson-Persistenz gegen ein kotlinx-serialization-Modell.** `Document`/`Page`/`TextBlock`/
  `TextAnchor` sind auf simPlays eigene, Multiplatform-taugliche Serialisierung ausgelegt; die
  bestehende Projekt-Persistenz (`StorageIo`, `ProjectStorage`) ist Jackson-basiert. IP-37 muss einen
  Weg festlegen (Jackson-Modul, eigene Serialisierer oder eine eingebettete kotlinx-Repräsentation im
  Jackson-Baum) - ungeklärt, bis IP-37 beginnt.
* **Migrationsreihenfolge.** Ein Projekt ohne `document` muss noch gelesen werden können, nachdem
  `IP-36` die alten Textfelder aus `Chapter`/`Prolog`/`Epilog`/`Blurb`/`Book`/`Copyright` entfernt hat.
  IP-37 braucht dafür ein von den produktiven Modellklassen unabhängiges Lese-Modell für genau diese
  Migration (siehe IP-37, „Technische Überlegungen“).
* **`simplay-fx` liefert seit 0.2.1 Verfügbarkeit und Fingerabdruck selbst** (`FxFontProbe`):
  `checkAvailability`, `fingerprint`, `verify`, `stamp`, intern auf demselben `FontMeasureCalculator`
  wie der Renderpfad. Geklärt; kein Eigenbau mehr in IP-34.
* **`FxFontProbe.checkAvailability` nennt keine Ersatzfamilie**, nur das Enum
  `AVAILABLE`/`SUBSTITUTED`/`MISSING`. `app/ui` ermittelt den tatsächlich aufgelösten Familiennamen
  selbst dazu (IP-34).
* **simPlays Referenzzeichensatz für den Fingerabdruck führt kein Kyrillisch.** Offen, ob Ghost
  Writer kyrillische Glyphen trägt und ob die Lücke für ai-ghost relevant ist; IP-34 übernimmt den
  simPlay-Satz vorerst unverändert.
* **JPMS und jlink.** `simplay-engine` ist ein Kotlin-Multiplatform-Artefakt (JVM-Variante),
  `simplay-fx` exportiert JavaFX transitiv. Die Modulnamen für `module-info.java` von
  `lib/ai-ghost-layouting-model` und `app/ui` und die `jlink`-Einbindung (`addExtraDependencies`) sind
  in IP-29 zu klären; das Laufzeit-Image muss weiter bauen.
* **Reife und Versionierung von simPlay.** Eine feste Version wird gepinnt. Ein Bruch in einer
  Minor-Version träfe den ganzen Renderpfad. IP-29 pinnt exakt und hält die Version an einer Stelle.
* **Das Messen gehört dem FX-Thread.** `simplay-engine.measure` läuft synchron; das erste Layout
  eines langen Buches kann das Fenster einfrieren, jetzt für das ganze Buch auf einmal statt je Teil.
  IP-39 begleitet es mit einer Fortschrittsanzeige und misst die Kosten, statt sie zu verbergen.
* **`PaperSheetView` im `EDITABLE`-Modus ist eine vollständige Editorkomponente.** Der Teil, der am
  ehesten ai-ghost-spezifisches Verhalten verlangt (Absatz-als-Einheit, Enter teilt statt Umbruch).
  Jeder solche Bedarf wird über die simPlay-API (`CaretModel`, `TextSelectionModel`, Tastenhandler
  darüber) beantwortet, nie über eine Erweiterung von simPlay im ai-ghost-Repository.
* **Rückabbildung Dokument → Modell.** `PaperSheetView` gibt bei einer Bearbeitung ein neues
  `Document` heraus; das Modell trägt seit der TextAnchor-Abweichung ohnehin keine
  `List<String>`-Absätze mehr, IP-39 schreibt das neue `Document` direkt in `Book.document` und hält
  nur noch `Book.chapters`s Reihenfolge/Bestand mit den Ankern synchron. Ein harter Zeilenumbruch
  innerhalb eines `TextBlock` (weiterhin nicht vorgesehen) würde diesen Weg stören.
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
* **Das `Document` ist der gespeicherte Zustand des Fließtexts** (`Book.document`), nicht mehr eine
  aus `List<String>`-Feldern abgeleitete Sicht; `TextAnchor` (simPlay 0.3.1) hält Titel, Kapitel,
  Prolog, Epilog und Klappentext wiederauffindbar, Kapitel über eine gespeicherte `UUID`. Der
  `TextStyle` im `Document` wird beim Laden verworfen und durch den aus `Design` berechneten Stil
  ersetzt.
* **`PaperSheetView` wird nicht mehr je Baumauswahl neu gebaut**, sie zeigt dauerhaft das ganze Buch;
  eine Baumauswahl navigiert über den `TextAnchor` des gewählten Knotens.
* **Der Metrik-Fingerabdruck** wird über `simplay-fx`s `FxFontProbe` aus `simplay-engine`s
  `FontFingerprint` genommen (Lateinschrift plus Diakritika/Satzzeichen bei Normgröße 100.0, mit
  Ascent, Descent und Advances je Glyph). Referenzsatz und Größe sind von simPlay fest vorgegeben.
* **ai-ghost-Seitenpolitik.** Nummerierung und inaktive Seiten kommen seit simPlay 0.3.0 aus
  `Document.numbering` und `PaperSheetView.pageModes` und werden von IP-35 verdrahtet. Gespiegelte
  Ränder, Leerseiten und die Klappentext-Kante bringt der Nutzer weiterhin nachträglich in simPlay
  ein; ai-ghost trägt bis dahin nur eine Zwischenlösung und markiert die Lücke als TODO.

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
* `PaperSheetView` ist eine dauerhafte, einzige Instanz über dem ganzen Buch; Schreiben und Vorschau
  sind ihre Modi `EDITABLE` und `SELECTABLE` auf demselben `Document`, ohne zweiten Aufbau; eine
  Baumauswahl navigiert über den `TextAnchor` des gewählten Teils, statt ein anderes Dokument
  einzusetzen.
* Prolog, Epilog und Klappentext werden aus dem Baum geschaltet; ein ausgeschalteter Teil behält
  seinen Text, ist ausgegraut, bleibt beschreibbar und ist sofort über `PageMode.DISABLED` auch auf
  dem Papier inaktiv. (Neunummerierung und Klappentext-Kante gelten, sobald die simPlay-Seitenpolitik
  steht; bis dahin als TODO dokumentiert.)
* Ein vor diesem Feature geschriebenes Dokument öffnet mit genau den Teilen, die es früher hatte; die
  einmalige Migration baut sein `Document` aus den bisherigen Feldern.
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
