# Feature-Plan: Paper Writing Surface

> Nur zur Orientierung. Jede Aufgabe, Einschränkung und jeder Test gehört zu den
> Implementierungsplänen unter `.claude/plans/implementation`; `FP-001-Overview.md` listet sie, und
> Abschnitt 7 benennt die Datei jedes einzelnen.

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
Engine und Renderer sind Allzweckbibliotheken; `app/ui` ist ein Verbraucher von ihnen.

Der Export ist nicht Teil dieses Features. Er wird nur als Einschränkung benannt: Das Layout-Ergebnis
bleibt toolkit-unabhängig, damit ein späterer Export – ein Plugin auf Apache PDFBox – dieselbe
Seitenstruktur verbraucht.

## 2. Aktueller Stand

* `lib/ai-ghost-model`, `lib/ai-ghost-fx-model` – Manuskript-POJOs und ihre gespiegelten
  FX-Eigenschaften.
* `lib/ai-ghost-layouting` – der Layout-Kern, umgesetzt (IP-03): Stile, Zeilenumbruch, platzierte
  Zeilen, die `TextMetrics`-Schnittstelle und eine deterministische Implementierung. Kein Toolkit,
  keine App.
* `lib/ai-ghost-layouting-model` – die Brücke von `Book`, `Design` und `Meta` zum Kern, umgesetzt
  (IP-03).
* `lib/ai-ghost-ai` – nur `TokenUtils`; keine Generierungspipeline.
* `app/ui` – das einzige JavaFX-Modul, MVVM FX, jlink-Image. Trägt die Schriftarbeit von IP-01
  (`FontCatalog`, `FontResolver`, `FontResolution`, `JavaFxTextMetrics`).
* `.claude/rules/architecture.md` erlaubt JavaFX nur in `app/ui`.

Fehlt: jeder Editor für Prolog, Kapitel, Epilog und Klappentext; Rendering, Paginierung und Vorschau;
ein JavaFX-Renderer und ein Modul, das einen halten darf; ein Design-Editor; Undo/Redo; der
Schrift-Fingerabdruck.

## 3. Zielzustand

Drei Zonen rechts des Projektbaums: das **Papier** in der Mitte, ein einklappbarer **Inspector**
rechts, der alles trägt, was kein gedruckter Text ist, und ein **Modus-Schalter** zwischen
`Schreiben` und `Vorschau` über demselben Inhalt.

* **Treuekette.** Eine Engine verwandelt Design plus Text in platzierte Zeilen in Punkten; beide
  Oberflächen malen diese Koordinaten und entscheiden nichts selbst. Messen und Zeichnen nutzen
  denselben JavaFX-Textstapel, sodass keine zweite Schriftimplementierung anders antworten kann. Die
  Übereinstimmung gilt pro Maschine; ein Projekt zeichnet die Metriken auf, mit denen es geschrieben
  wurde, und eine Abweichung wird gemeldet.
* **Schriften.** Keine Schriftdatei wird gelesen, geparst oder ausgeliefert. Familien kommen aus
  `javafx.scene.text.Font`, das Messen läuft über einen verborgenen `Text`-Knoten.
* **Ein wiederverwendbarer Renderer.** `lib/layouting-fx` (`ai-ghost-layouting-fx`) ist eine
  JavaFX-Komponentenbibliothek, die das Messen, die exakte Seitenansicht und den beschreibbaren Fluss
  hält. Sie hängt von `ai-ghost-layouting` und JavaFX ab und von nichts sonst, besitzt kein Dokument
  und wendet keine Änderung an. `app/ui` behält alles, was die Anwendung benennt: die
  `FontData`-Übersetzung, den Fingerabdruck, die Bindung von Buch und Design, Undo, Inspector und KI.
* **Vorwerk.** Titelseite mit Titel, weiteren Titelzeilen und Autor, gefolgt direkt von der
  Copyright-Seite.
* **Optionale Teile.** Prolog, Epilog und Klappentext beginnen immer auf einer eigenen Seite, behalten
  immer ihren Text, bleiben beschreibbar, wenn ausgeschaltet, sind ausgegraut und aus der
  Seitennummerierung genommen. Das Kontrollkästchen im Baum entscheidet über die Zugehörigkeit zum
  Buch, nicht über die Existenz.
* **Der Klappentext** ist Umschlagtext: immer das letzte Blatt, durch eine harte Kante abgesetzt, ohne
  Seitenzahl.
* **KI** ist in diesem Feature nur eine Schaltfläche ohne Wirkung: eine schwebende Leiste am
  fokussierten Block mit Umschreiben, Ausbauen und Kürzen, dazu eine Aktion auf Teil-Ebene im
  Inspector. Jede dieser Schaltflächen ruft im `*View`-Controller eine leere, parameterlose Methode,
  deren einziger Rumpf `TODO("AI action: …")` ist. Kein Aufruf, kein Ergebnis, keine vorläufige
  Anzeige. Der Aktions-Port aus IP-17 liegt fertig in `lib/ai`, wird von diesem Feature aber nicht
  verdrahtet und bleibt für eine spätere Wiederverwendung stehen; ein Provider kommt erst mit dem
  späteren Plugin-System-Feature.
* **Design**-Änderungen wirken sofort auf dem Papier.

## 4. Anforderungen

### Funktionale Anforderungen

* Titelseite, Copyright-Seite, Prolog, jedes Kapitel, Epilog und Klappentext werden auf dem Papier
  geschrieben.
* Prolog, Epilog und Klappentext werden über ein Kontrollkästchen im Baum in das Buch ein- und
  ausgeschaltet, ohne Text zu verlieren und ohne Bestätigung.
* Text wird im Design des Buches gezeichnet, je Elementklasse: Titel, Kapiteltitel,
  Kapiteltitel-Anhang, Fließtext.
* Schriften werden aus den auf der Maschine installierten Familien gewählt; eine fehlende oder anders
  messende Schrift wird mit dem verwendeten Ersatz gemeldet.
* Seitenumbrüche werden dort gezeigt, wo das gedruckte Buch umbricht: als Blattlücke zwischen Absätzen,
  als Markierungslinie mit der Seitenzahl innerhalb eines Absatzes.
* Absätze sind die Bearbeitungseinheit: erstellen, teilen, verbinden, löschen, umsortieren.
* Nirgends Inline-Zeichenformatierung; eingefügter Rich-Text wird auf reinen Text reduziert.
* Jede Textänderung und jede Strukturänderung ist undo- und redo-fähig.
* Prompts, Teildaten und Designstile leben im Inspector, Seitenformat und Ränder im
  Projekteinstellungsdialog – nie auf dem Papier.
* Die Vorschau rendert das ganze Buch und scrollt zu dem im Baum gewählten Teil; ein Absatz fällt in
  beiden Oberflächen auf dieselbe Seite.
* Der Renderer zeichnet ein Dokumentlayout, ohne zu wissen, was ein Buch ist; Blatt, Lücke, Marke und
  Hintergrund werden über das Stylesheet der Bibliothek gestylt und von der Anwendung überschrieben.
* Eine Demo rendert ein Dokument mit der Bibliothek ohne irgendein ai-ghost-Modul auf ihrem Pfad.

### Technische Anforderungen

* Kotlin und Gradle. JavaFX ausschließlich in `app/ui` und in `lib/layouting-fx`; der Build scheitert,
  wenn die Bibliothek irgendeine andere Abhängigkeit oder einen Typ der Anwendung in einer Signatur
  bekommt.
* Paginierung und Typografie bleiben in einem toolkit-freien Modul und messen über eine Schnittstelle;
  das Layout-Ergebnis trägt keinen Toolkit-Typ.
* Keine Schriftdatei wird gelesen, geparst oder ausgeliefert; `Font.loadFont` wird nicht verwendet.
  Die Ghost-Writer-Schrift bleibt eine Anzeigeschrift der Oberfläche.
* Das Messen ist eine FX-Thread-Operation und sagt das in der API.
* Alle Geometrie in Punkten als `Double`; Millimeter existieren nur in der UI.
* Modelländerungen folgen `fx-model`; UI-Arbeit folgt `ui-styling`, `fx-component-lifecycle`, `icons`
  und `font`; Tests folgen `testing`; Dokumentation folgt `project-docs`; Workflows folgen
  `ci-pipeline`.
* Der Text eines Teils bleibt `List<String>`; View-Zustand gehört zu `Preferences`, nie zum
  Projektdokument.
* Keine neue Drittanbieter-Abhängigkeit ohne Rückfrage beim Nutzer. JavaFX in einem Bibliotheksmodul
  ist die eine solche Entscheidung, die dieses Feature braucht.

## 5. Architektur

```text
lib/ai-ghost-layouting          (kein Toolkit, keine App)
        ▲                            ▲
        │                            │
lib/ai-ghost-layouting-model   lib/ai-ghost-layouting-fx   (JavaFX-Komponentenbibliothek)
        ▲                            ▲
        └─────────── app/ui ─────────┘        (ai-ghost-spezifischer Kleber)
```

* **`lib/layouting`** – Satz-Engine: Textblöcke plus ein eigener Stil hinein, ein `DocumentLayout` aus
  Seiten und platzierten Zeilen heraus. Besitzt `TextMetrics`. Kennt kein Toolkit und kein
  Manuskriptmodell.
* **`lib/layouting-model`** – das einzige Modul, das von beiden Seiten abhängt: Builder von `Book`,
  `Design` und `Meta` in die Blöcke und Stile der Engine.
* **`lib/layouting-fx`** – Messen (Schriftkatalog, Auflösung, JavaFX-`TextMetrics`) und Zeichnen
  (`PaperPageView`, `PaperFlowView`) in einem Modul, Pakete `...layouting.fx.font`, `.control`,
  `.skin` plus eigenes Stylesheet.
* **`lib/ai-ghost-ai`** – trägt den Aktions-Port aus IP-17 (`AiAction`, `AiActionRequest`, Callbacks,
  `AiActionLimits`, `ParagraphSplitter`), ohne Implementierung. Dieses Feature verdrahtet ihn nicht;
  er bleibt für eine spätere Wiederverwendung stehen. Die KI-Schaltflächen rufen nur eine leere
  `*View`-Methode mit `TODO(...)`. Ein Provider kommt erst über das Plugin-System eines eigenen,
  späteren Features.
* **`app/ui`** – `BookPartEditor`, `Inspector`, `AiActionBar` (nur Schaltflächen, jede ruft eine leere
  `*View`-Methode mit `TODO(...)`), der überarbeitete `Editor` und sein Routing, Undo, die
  `FontData`-Übersetzung und der Metrik-Fingerabdruck.

**Modellerweiterung.** `Design` bekommt `PageFormat` und Zeilenabstand je Elementklasse; `FontData`
bekommt den Metrik-Fingerabdruck; `Book` trägt Prolog, Epilog und Klappentext immer, jeweils mit
seinem `included`-Schalter; `Preferences` bekommt den Editor-View-Zustand. Jedes wird gemäß `fx-model`
gespiegelt.

```text
ProjectProperty
  ├─ designProperty ─┬─> Inspector (Stilabschnitte)
  │                  └─> LayoutEngine ─┬─> PaperFlowView   (Umbruchpositionen)
  │                                    └─> PaperPageView   (exaktes Malen)
  ├─ bookProperty ──> BookPartEditor <──(Änderungsereignisse)──> PaperFlowView
  └─ (Auswahl) ProjectList.selectedItem ──> EditorViewModel ──> gezeigter Teil
                                                   ▲
                                    TextMetrics ───┘ (layouting-fx, JavaFX Text, auf dem FX-Thread)
```

## 6. Übersicht der Implementierungspläne

IP-22 gehört zur Schriftgrundlage und steht bei IP-01; IP-25 bis IP-28 tragen die Renderer-Bibliothek
und wurden hinzugefügt, nachdem IP-01 bis IP-03 abgeschlossen waren. IP-20 wurde mit dem Export
entfernt. Die Nummerierung wird stabil gehalten statt neu nummeriert.

| ID    | Implementierungsplan                      | Ziel                                                                   | Abhängigkeiten       |
|-------|-------------------------------------------|-----------------------------------------------------------------------|----------------------|
| IP-01 | Font Discovery And Text Measuring ✅       | Installierte Familien, Auflösung, Fallback, Messen über JavaFX        | -                    |
| IP-22 | Font Identity And Substitution Reporting ✅| Verwendete Metriken aufzeichnen, Ersetzung erkennen und melden        | IP-01                |
| IP-02 | Design Page Format Model ✅                | Seitenformat, Ränder und Abstände in `Design`, gespiegelt            | -                    |
| IP-24 | Optional Parts In The Model ✅             | Prolog, Epilog und Klappentext immer vorhanden und schaltbar         | -                    |
| IP-03 | Layout Core ✅                             | Aufgelöste Stile, Zeilenumbruch, Ausrichtung, platzierte Zeilen      | IP-01, IP-02, IP-24  |
| IP-04 | Pagination And Page Break Policy ✅        | Seitenfüllung, Umbrüche, gerade/ungerade Ränder, Policy-Hook         | IP-03                |
| IP-25 | Renderer Library Module ✅                 | Neues JavaFX-Bibliotheksmodul, JPMS, TestFX, CI, Architekturregel    | -                    |
| IP-26 | Font And Measuring Migration ✅            | Katalog, Auflösung und Metriken ziehen in die Bibliothek um          | IP-01, IP-25         |
| IP-05 | Incremental Layout And Caching ✅          | Absatzweise Invalidierung, damit Tippen reaktionsschnell bleibt      | IP-04                |
| IP-06 | Layout Regression Harness ✅               | Golden-Seitenstrukturen und der Oberflächenvergleich                 | IP-04, IP-07, IP-08  |
| IP-07 | Paper Page View ✅                         | Exakter schreibgeschützter Renderer eines Dokumentlayouts, in der Bibliothek | IP-04, IP-26 |
| IP-08 | Paper Flow View ✅                         | Schreibblatt mit Seitengeometrie und Umbruchmarken, in der Bibliothek | IP-04, IP-26        |
| IP-27 | Library Styling And Theming API           | Eigenes Stylesheet, Stilklassen, Überschreibung durch die ai-ghost-Palette | IP-07, IP-08   |
| IP-28 | Standalone Reuse And Documentation        | Demo ohne ai-ghost, veröffentlichtes Artefakt, Doku, Abhängigkeitsprüfung | IP-27          |
| IP-09 | Undo And Redo Infrastructure ✅            | Ein Undo-Stack über die Modelländerungen des Editors                 | -                    |
| IP-10 | Book Part Writing Surface ✅               | Cursor, Tippen und Bindung von Überschriften und Absätzen            | IP-08, IP-09         |
| IP-11 | Paragraph Structure Operations ✅          | Absätze teilen, verbinden, löschen und umsortieren                   | IP-10                |
| IP-12 | Inspector Shell And Content Sections ✅    | Kontextfenster mit Buch- und Teilabschnitten, absorbiert `BookEditor`| -                    |
| IP-13 | Design Style Sections ✅                   | Bearbeiten der Stile im Inspector mit Live-Wirkung                   | IP-02, IP-12, IP-26  |
| IP-14 | Project Settings Dialog ✅                 | Seitenformat, Ränder und Leerseiten in einem Dialog                  | IP-02                |
| IP-15 | Editor Arrangement And Tree Routing       | Drei Zonen, Routing jedes Baumknotens, View-Zustand persistiert      | IP-11, IP-12         |
| IP-16 | Writing And Preview Modes                 | Modus-Schalter, Vorschau des ganzen Buches, Scrollen, Virtualisierung| IP-05, IP-07, IP-15  |
| IP-17 | AI Action Port ✅                          | Aktionsschnittstelle in `lib/ai`, keine Implementierung, bleibt für spätere Wiederverwendung | -          |
| IP-18 | AI Actions On Paragraph And Heading       | Schwebende KI-Leiste; jede Schaltfläche ruft eine leere `*View`-Methode mit `TODO(...)` | IP-10                |
| IP-19 | AI Part Generation (nur Schaltfläche) ✅   | KI-Schaltfläche im Inspector-Teilabschnitt, ruft eine leere `*View`-Methode mit `TODO(...)` | IP-12         |
| IP-21 | In-Paragraph Sheet Split                  | Echte Blattlücke innerhalb eines Absatzes beim Schreiben (optional)  | IP-11                |
| IP-23 | Optional Book Parts In The Tree           | Kontrollkästchen schaltet Prolog, Epilog und Klappentext ins Buch    | IP-15, IP-24         |

## 7. Implementierungspläne

Jeder offene Plan ist unter `.claude/plans/implementation` ausgeschrieben, mit seinen Aufgaben, seinen
Einschränkungen und seinen Tests. Hier benannt sind nur die Grenze des Plans und die Begründung
dahinter, die der detaillierte Plan selbst nicht trägt.

### IP-01: Font Discovery And Text Measuring ✅

Abgeschlossen, in `app/ui`. Der Hilfs-`Text`-Knoten wird einmal erstellt und wiederverwendet, und
Breiten werden nicht aufgerundet: Aufrunden ist für die bevorzugte Breite eines Steuerelements
richtig, macht den Zeilenumbruch aber grob und größenabhängig. Es werden Wörter gemessen, keine
Absätze, was den Cache klein und wiederverwendbar hält. IP-26 verschiebt die wiederverwendbare Hälfte
in die Bibliothek, weshalb hier nichts neu geschrieben wird.

### IP-22: Font Identity And Substitution Reporting ✅

Plan: `FP-001-IP-22-SchriftIdentitaetUndErsatzmeldung.md`

Abgeschlossen, entlang der Modulgrenze geteilt. Der Fingerabdruck kommt aus Messungen, nicht aus der
Datei: Er erfasst genau das, was das Layout beeinflusst, und schweigt über den Rest. Referenzmenge und
Größe sind für alle Zeiten fest, sonst meldet jedes ältere Projekt eine falsche Abweichung; die Menge
wuchs auf Latin-1, Latin Extended-A und Kyrillisch, da sich eine ersetzte Familie meist genau in
diesen Buchstaben unterscheidet, während reines ASCII noch übereinstimmt. Das Messen ist reines JavaFX
und kennt keinen Typ dieser Anwendung, daher sitzt es in `lib/layouting-fx`, das sein erstes Paket mit
ihm exportiert, während `app/ui` behält, was es aufzeichnet: die Übersetzung auf `FontMetricsData`,
den Vergleich und den Bericht. Ein Fingerabdruck wird beim Speichern eines Projekts geschrieben und
nur dort, wo noch keiner steht – der Design-Editor aus IP-13 existiert noch nicht, und ein
Überschreiben bei jedem Speichern würde den Vergleich sinnlos machen.

### IP-02: Design Page Format Model ✅

Abgeschlossen. Ränder sind innen/außen statt links/rechts, da ein gedrucktes Buch den Bundsteg braucht
und ein nachträgliches Einfügen Modell, FX-Modell, Speicherung und Engine auf einmal berühren würde.
Zeilenabstand als Faktor übersteht eine Änderung der Schriftgröße.

### IP-24: Optional Parts In The Model ✅

Abgeschlossen. Der Schalter `included` sitzt auf `Prolog`, `Epilog` und `Blurb`, nicht auf `Book` und
nicht in einer gemeinsamen Schnittstelle: Kein Aufrufer erreicht ihn polymorph, und ein Kapitel kann
ihn nie verwenden. Da nichts gelöscht wird, braucht IP-23 keine Bestätigung und keinen
wiederherstellenden Undo-Eintrag.

### IP-03: Layout Core ✅

Abgeschlossen. Die Engine besitzt die Mess-Schnittstelle und kennt weder ein Toolkit noch das Modell
dieser Anwendung; ein Block ist Text plus Stil, daher brauchen Titelseite, Überschrift und Absatz
keinen eigenen Typ. Die Abbildung von platzierter Zeile zurück auf den Quellzeichenbereich ist das,
was IP-10 einen Cursor platzieren und IP-18 einen Absatz adressieren lässt. Silbentrennung ist
außerhalb des Umfangs, daher bleibt der Umbruchschritt hinter einer Schnittstelle.

### IP-04: Pagination And Page Break Policy ✅

Plan entfernt, war `FP-001-IP-04-SeitenumbruchUndPaginierung.md`.

Die Policy-Schnittstelle existiert, obwohl nichts sie implementiert, da Witwen und Waisen ändern, wo
eine Seite endet, und die Engine sonst später umgestaltet werden müsste. Eine Seite trägt zwei Zahlen,
die nicht verwechselt werden dürfen – ihre Position im Layout, die auch eine inaktive Seite einnimmt,
und ihre Seitenzahl im Buch. Diese Unterscheidung macht das Einschalten eines Teils zu einer reinen
Neunummerierung. Ein optionaler Teil beginnt immer eine neue Seite, sodass das Einschalten nie einen
benachbarten Teil neu umfließen kann. Eine Seite trägt auch, ob sie inaktiv, nummeriert und abgesetzt
ist; die Bibliothek erfährt „abgesetzt“ aus dem Ergebnis, nie, dass abgesetzt Klappentext bedeutet.

Wie geplant gebaut, um eine ausdrückliche Nutzeranfrage erweitert: Der gerade/ungerade Rändertausch
gilt nur, wenn eine neue Projekteinstellung, „Spiegelnde Ränder“ (`PageFormat.mirroredMargins`,
Vorgabe `false`), eingeschaltet ist – ohne sie behält jede Seite denselben inneren/äußeren Rand. Das
Feld wurde zu `PageFormat` in `lib/model` hinzugefügt und gemäß dem `fx-model`-Skill in
`PageFormatProperty` in `lib/fx-model` gespiegelt, mit vollständigen Property-Tests; es wurde kein
UI-Kontrollkästchen verdrahtet, da dies zum Projekteinstellungsdialog gehört, nicht zu diesem Plan.
Titelseite und Copyright-Seite wurden auf ausdrücklichen Wunsch als ungezählt festgelegt.

`lib/layouting` erhielt `PageGeometry` (ein modellunabhängiges Abbild von `PageFormat`, so wie
`TextStyle` einen gespeicherten Stil abbildet), gehalten von `PageGeometryTranslation` in
`lib/layouting-model` – `lib/layouting` selbst erhielt keine Abhängigkeit. `LayoutEngine.layout`
paginiert einen Teil, `LayoutEngine.layoutBook` ein ganzes Buch über Teile hinweg; ein Teil, der auf
einer eigenen Seite beginnt, fällt aus der Schleifenstruktur heraus, statt ein Flag zu brauchen. Die
Randseite folgt der physischen Position einer Seite (Position 0 ist recto), unabhängig davon, ob diese
Seite nummeriert ist, sodass die Seiten eines ausgeschalteten Teils das physische Layout des Buches
stabil halten.

### IP-25: Renderer Library Module

Plan: `FP-001-IP-25-RendererBibliotheksmodul.md`

JavaFX in einem Bibliotheksmodul braucht die Bestätigung des Nutzers, bevor die Abhängigkeit
hinzugefügt wird. Die Architekturregel wird nicht gelöscht, sondern geschärft – JavaFX gehört zu
`app/ui` und zu einem Modul, das selbst eine JavaFX-Komponentenbibliothek ist – sonst verletzt jeder
folgende Plan die Regeln. Das jlink-Image muss weiter funktionieren, und das TestFX-Setup von `app/ui`
ist ein Vorbild ohne Präzedenzfall außerhalb davon.

### IP-26: Font And Measuring Migration ✅

Plan: `FP-001-IP-26-SchriftUndMessungUmzug.md`

Der Schnitt verläuft entlang `FontData`: Was die Anwendung benennt, bleibt zurück, der Rest zieht um.
Das Verhalten ändert sich nicht – die Tests, die vor dem Umzug bestehen, müssen danach bestehen, was
den Umzug sicher macht, während spätere Pläne auf den Klassen aufbauen. Die FX-Thread-Einschränkung
reist mit der Klasse, da ein Verbraucher außerhalb dieses Repositorys keinen Plan zum Nachlesen hat.

Wie geplant gebaut, mit einer auf Wunsch des Nutzers entschiedenen Form. `FontCatalog`,
`FontResolver`, `FontResolution` und `JavaFxTextMetrics` zogen in `lib/layouting-fx` Paket
`org.pcsoft.app.aighost.layouting.fx.font` um, neben den Fingerabdruck aus IP-22; ihre drei Tests
zogen mit ihnen um und laufen auf dem eigenen kopflosen TestFX-Setup des Moduls. Der Bibliothekstyp,
der `FontData` in jeder verschobenen Signatur ersetzt, ist `FontDescription` – Familie, `size: Int`,
`bold`, `italic` – mit ganzzahliger Punktgröße, sodass Auflösung und Messungs-Cache bit-für-bit wie
zuvor bleiben. Die Übersetzung auf der Anwendungsseite ist eine einzelne Erweiterung
`FontData.toFontDescription()` in `app/ui` (`FontTranslation.kt`), nach dem Vorbild von
`FontFingerprintTranslation.kt`, kein Übersetzer-Objekt. `FontIdentity` ist der einzige
Produktionsaufrufer und löst jetzt darüber auf. Kein `module-info` brauchte eine strukturelle
Änderung: Das Zielpaket war bereits exportiert, `javafx.graphics` bereits `required` und `app/ui` las
`ai-ghost-layouting-fx` bereits seit IP-25 – nur die Kommentare wurden geschärft. `SplashStageTest`
wurde auf Wunsch des Nutzers gelöscht: Er scheiterte auf dem unveränderten HEAD in dieser kopflosen
Umgebung (Splash-Deckkraft `1.0` statt `0.0`), außerhalb des Umfangs dieses Plans. Der volle `build`
und ein erzwungenes `jlink` von `app/ui` sind grün.

### IP-05: Incremental Layout And Caching ✅

Plan entfernt, war `FP-001-IP-05-InkrementellesLayout.md`.

Dies ist der Plan, der entscheidet, ob sich das Feature schnell anfühlt, und er kommt vor den
Oberflächen. Zwei Dinge machen die FX-Thread-Einschränkung erträglich: Es werden nur Wörter gemessen,
sodass gewöhnliche Prosa weit weniger Messungen braucht als sie Zeichen hat, und sind die Wörter erst
zwischengespeichert, ist das Anordnen von Zeilen und Seiten Arithmetik, die überall laufen darf.

Wie geplant gebaut. Der Zwischenspeicher landete als `IncrementalLineBreaker` in `lib/layouting`,
einem `LineBreaker`, der einem `GreedyLineBreaker` vorgeschaltet ist und je Block – gekennzeichnet
durch Text, `TextStyle` und Spaltenbreite – das Ergebnis eines Einzelblock-Umbruchs hält; unveränderte
Blöcke werden wiederverwendet, nur der geänderte wird neu gemessen, und das Zusammenstapeln zu einem
`LaidOutText` ist messungsfreie Arithmetik, dessen Ergebnis einem vollständigen Umbruch gleicht (bei
`FixedTextMetrics` bitgenau). Eine geänderte Spaltenbreite verwirft den Cache selbsttätig, eine
Designänderung über `clear()` durch den Aufrufer; `prewarm` bietet das Vorabmessen an. Die
Seitengrenzen werden über das unveränderte `LayoutEngine.layout` voll neu berechnet, da dies keine
Messung trägt. `BookPartEditorViewModel` hält den Umbrecher jetzt über die Lebensdauer des Editors
statt ihn je Tastendruck neu zu erzeugen. Messung (Kunstbuch, 201 Blöcke, 34 Seiten, echte
`JavaFxTextMetrics`): kaltes Gesamtlayout 164,5 ms, Neulayout je Tastendruck 1,68 ms. Der Benchmark
ist ein Entwicklertest in `lib/layouting-fx` (kein `RT`, da er gegen keine feste Referenz vergleicht).
IP-16 ist damit entsperrt und wartet noch auf IP-15.

### IP-06: Layout Regression Harness ✅

Plan entfernt, war `FP-001-IP-06-LayoutRegressionsPruefstand.md`.

Golden Files sind Momentaufnahmen von Zahlen, nicht von Bildern: Ein numerisches Diff sagt, welche
Zeile sich bewegt hat. Sie werden gegen die deterministischen Metriken erzeugt, nie gegen eine auf der
Build-Maschine installierte Schrift, sonst unterscheidet sich das Ergebnis je Entwickler und je
Runner. Der Oberflächenvergleich gehört zur Bibliothek, da beide Oberflächen es tun.

Wie geplant gebaut, mit einer Kategorie, die auf Wunsch des Nutzers zum `testing`-Skill hinzugefügt
wurde: Der Golden-File-Test und der Oberflächenvergleich sind weder einfache Entwicklertests noch
`IT` (in `lib` verboten), daher wurde dort zuerst eine dritte Kategorie, der **Regressionstest**
(Suffix `RT`, ausschließlich in `lib`, spiegelbildlich zur Beschränkung von `IT` auf `app`),
hinzugefügt. `lib/layouting` erhielt `LayoutGoldenFileRT` (sieben Beispielprojekte – kurzer Teil,
langer Teil, Blocksatz-Block, zwei Designs, gespiegelte Ränder für gerade/ungerade Seiten, Prolog
aus- und eingeschaltet) plus `GoldenFileSupport`, das die Seiten eines `DocumentLayout` als reine
Zahlen serialisiert (`position`, `number`, `active`, `lines`, `leftMargin`, `rightMargin`) und gegen
eine eingecheckte `.golden`-Datei vergleicht, wobei bei einer Abweichung die erste abweichende Zeile
benannt wird; eine Datei wird durch erneutes Ausführen des Tests mit
`-DlayoutGoldenFiles.update=true` neu erzeugt und vor dem Commit per `git diff` geprüft.
`lib/layouting-fx` erhielt `PaperFlowPageComparisonRT`, das ein `DocumentLayout` auf derselben Bühne
sowohl in `PaperFlowView` als auch in `PaperPageView` einspeist und zwei Zählungen gegen das Layout
selbst prüft: die Seitenzahl von `PaperPageView` sowie das Verhältnis der `.paper-flow-view-gap`-
Bereiche von `PaperFlowView` (einer je Blockgrenze, unabhängig davon, wo eine Seite umbricht) zu den
`.paper-flow-view-break-mark`-Marken (eine je Seitenumbruch innerhalb eines einzelnen Blocks). Beide
Module erhielten eine Gradle-Aufgabe `regressionTest` (`*RT`-Klassen, aus der einfachen `test`-Aufgabe
ausgeschlossen), von der `check` – und damit `build` – abhängt; die `test`-Aufgabe der CI führt jetzt
`test regressionTest` ausdrücklich aus, da `regressionTest` getrennt von `test` steht. Zwei Fehler
traten zutage und wurden vor dem Commit der Golden Files behoben: Die `-D`-Systemeigenschaft zur
Neuerzeugung der Golden Files wurde nicht in die geforkte Test-JVM der Aufgabe weitergereicht, und die
erste Fassung des Oberflächenvergleichs nahm an, ein Abstand markiere immer nur eine Seitengrenze,
während `PaperFlowViewSkin` tatsächlich zwischen je zwei Blöcken einen zeichnet. Die veraltete
README-Zeile für `lib/ai-ghost-layouting-fx` („Planned“) wurde im selben Durchgang auf „Implemented“
korrigiert, da IP-07/IP-08 sie bereits ausgeliefert hatten.

### IP-07: Paper Page View ✅

Plan entfernt, war `FP-001-IP-07-SeitenAnsicht.md`.

Die Komponente entscheidet nichts über Typografie; sie malt Koordinaten, was sie per Konstruktion mit
der Schreibfläche übereinstimmen lässt. Die Virtualisierung landet hier statt in IP-16: Eine
Bibliothek, die kein langes Dokument zeigen kann, ist nicht wiederverwendbar.

Wie geplant gebaut: `PaperPageView` (ein schlichtes `Control` mit einem von `SkinBase` abgeleiteten
Skin, Canvas-basiertes Zeichnen, kein FXML) in `lib/layouting-fx`, Paket `...layouting.fx.paper`, mit
einer `PagePainter`-Schnittstelle und ihrem `DefaultPagePainter`. Um zwei mit dem Nutzer getroffene
Entscheidungen erweitert, beide ohne das abgeschlossene IP-04 zu berühren: Da `Page`/`DocumentLayout`
keine Seitenbreite oder -höhe tragen, nimmt die Komponente eine eigene `pageGeometryProperty` neben
`documentLayoutProperty`; und die harte Kante einer inaktiven Seite wird gelesen, indem das
`active`-Flag einer Seite mit dem ihres Nachbarn in `DocumentLayout.pages` verglichen wird, statt ein
Feld zu `Page` hinzuzufügen. Die Virtualisierung ist eine `ScrollPane` über einem `Pane` voller Höhe,
ein `Canvas` pro Seite nur, solange sie den Viewport plus einen Puffer schneidet.

### IP-08: Paper Flow View ✅

Plan entfernt, war `FP-001-IP-08-SchreibblattAnsicht.md`.

Das native Steuerelement bricht mit demselben Textstapel um, mit dem die Engine gemessen hat, sodass
die Umbrüche übereinstimmen, sobald die Insets des Steuerelements aus der Spaltenbreite herausgenommen
sind. Das Verzögern gehört auf die Umbruch-Neuberechnung, nicht auf den Text. Die Komponente besitzt
den Cursor, der Verbraucher besitzt den Text – das hält die Komponente wiederverwendbar und Undo
funktionsfähig. Das Anwenden einer Änderung, die Bindung an `BookPartProperty` und das
ai-ghost-Verhalten gehören zu IP-10; das Teilen eines Steuerelements an einem Umbruch ist IP-21.

Wie geplant gebaut: `PaperFlowView` (ein schlichtes `Control` mit einem von `SkinBase` abgeleiteten
`PaperFlowViewSkin`) in `lib/layouting-fx`, Paket `...layouting.fx.paper`, neben `PaperPageView`. Je
Block wird eine native `TextArea` gebaut, indem die platzierten Zeilen eines `DocumentLayout` nach
`LaidOutLine.blockIndex` gruppiert und ihr Text wieder zusammengefügt wird; ein Seitenumbruch zwischen
zwei Blöcken wird als echter Abstandsbereich in Größe des Seitenabstands von `PaperPageViewSkin`
gerendert, ein Umbruch innerhalb eines Blocks als gestrichelte `paper-flow-view-break-mark`-Linie mit
der Zielseitenzahl, positioniert über den Zeichenanteil, an dem der Umbruch liegt – `TextArea` hält
ihr eigenes Textlayout privat, sodass die Marke den exakten Pixel der umbrochenen Zeile nicht
erreichen kann, ohne die Toolkit-Reflexion, die dieser Code nie verwendet. Die gemeldete
`columnWidthProperty` zieht die eigenen Insets des Textsteuerelements von der Inhaltsbreite der
Seitengeometrie ab. Jede Änderung wird nur gemeldet, nie angewandt: Eine
`PaperFlowListener`-Schnittstelle (`onTextChanged`, `onCaretMoved`, `onFocusChanged`,
`onSplitRequested`, `onMergeRequested`, `onRemoveRequested`, alle standardmäßig leer) ist das, worüber
das Steuerelement mit einem Aufrufer spricht, geformt nach der Request/Callback-Form des `AiAction`-
Ports aus IP-17 statt als mehrere getrennte Listener-Listen. Enter wird in eine Split-Anfrage
abgefangen statt einen Zeilenumbruch einzufügen, da ein Block ein Absatz ist, kein mehrzeiliges Feld;
Backspace am Blockanfang und Delete am Blockende werden zu Merge-Anfragen statt angewandt zu werden.
Eingefügter Inhalt geht immer über `insertText` mit der reinen Zeichenkette der Zwischenablage hinein,
nie mit ihrem Rich-Inhalt. Die Spaltenbreiten-Neuberechnung und die Umbruchmarken werden 100 ms
verzögert hinter einer `PauseTransition` bei einer Größenänderung, sodass ein Neuaufbau aus einem
wirklich neuen `DocumentLayout` sofort bleibt; der Neuaufbau merkt sich auch, welcher Block den Fokus
trug und wo sein Cursor stand, und stellt beides wieder her, sobald die neuen Textsteuerelemente
existieren, sodass die Übergabe eines frisch berechneten Layouts das Tippen nie unterbricht. Das
`.paper`-Paket von `lib/layouting-fx` – das sowohl `PaperPageView` als auch `PaperFlowView` trägt –
wird jetzt aus `module-info.java` exportiert und schließt eine seit IP-07 offene Lücke (es war nie
exportiert worden, da noch nichts außerhalb des Moduls es las). `:lib:ai-ghost-layouting-fx:build` ist
grün, 8 neue Entwicklertests. CHANGELOG und MkDocs blieben unberührt, da keine der beiden Views in
einen `app/ui`-Bildschirm verdrahtet ist – das tut IP-10. IP-06 und IP-27 sind jetzt entsperrt; IP-10
ist zusammen mit dem bereits abgeschlossenen IP-09 entsperrt.

### IP-27: Library Styling And Theming API

Plan: `FP-001-IP-27-BibliotheksStyling.md`

Nur das Chrome ist stylebar; der Text wird vom Layout-Ergebnis gestylt. Ein Stylesheet, das eine
Schriftgröße ändern könnte, würde die Treuekette still wieder öffnen, daher muss die Grenze in der
API-Dokumentation ausdrücklich sein.

### IP-28: Standalone Reuse And Documentation

Plan: `FP-001-IP-28-EigenstaendigeNutzung.md`

Eine Bibliothek bleibt nur so lange wiederverwendbar, wie etwas scheitert, wenn sie es nicht mehr ist,
weshalb die Abhängigkeitsprüfung automatisiert ist. Die Demo ist ein Beispiel in einem eigenen
Quellset, kein Produkt.

### IP-09: Undo And Redo Infrastructure ✅

Plan entfernt, war `FP-001-IP-09-UndoRedoInfrastruktur.md`.

Sie muss existieren, bevor die erste Oberfläche in sie hineinschreibt, sonst wächst Bearbeitung und KI
jeweils ein eigener Mechanismus. Sie bleibt in `app/ui`, da die Bibliothek keine Änderung anwendet.

Gebaut als `UndoEntry`/`PropertyUndoEntry`/`UndoStack` unter `app/ui/.../undo`, im Besitz von
`MainWindowViewModel` und geleert bei `newProject()`/`openProject()`. Über den ursprünglichen Umfang
hinaus bat der Nutzer um einen benannten Tooltip je Eintrag und ein Verlaufs-Dropdown an den
Undo/Redo-Werkzeugleisten-Schaltflächen (`SplitMenuButton`, gestylt wie eine Zurück-Schaltfläche eines
Browsers), das über `undoUntil`/`redoUntil` mehrere Schritte auf einmal überspringt;
`UndoStack.visibleEntryCount` begrenzt, wie viele Einträge das Dropdown offenlegt, als schlichte
Eigenschaft statt als persistierte Präferenz. Der `icon-creator`-Agent hatte in dieser Umgebung kein
Werkzeug zum Schreiben von Bildern verfügbar, daher wurden `undo@32.png`/`redo@32.png` stattdessen mit
einem kleinen Pillow-Skript gezeichnet, passend zur bestehenden Icon-Palette – mit dem Nutzer
bestätigt.

### IP-10: Book Part Writing Surface ✅

Plan entfernt, war `FP-001-IP-10-Schreibflaeche.md`.

Prolog, Kapitel und Epilog sind ein Editor über einer `BookPartProperty`, nicht drei; der Klappentext
ist die Ausnahme, die gebaut werden muss. Der Cursor ist ein Absatzindex plus ein Zeichen-Offset, nie
eine Koordinate, sodass das Bearbeiten eine Designänderung übersteht. Ein ausgeschalteter Teil bleibt
beschreibbar – das Ausgrauen sagt, dass er nicht im Buch ist, nicht, dass er gesperrt ist. Alles
ai-ghost-Spezifische wird durch eine API der Bibliothek beantwortet, nie durch eine Abhängigkeit
zurück in die Anwendung.

Breiter gebaut als der Plan-Titel, auf Wunsch des Nutzers. Der Projektbaum erhielt zwei echte Knoten,
`TitlePageItem` und `CopyrightPageItem`, vor dem Prolog (das vollständige Knoten-Routing gehört
weiterhin zu IP-15); Titelseite und Copyright-Seite werden auf dem Blatt schreibgeschützt gezeigt, ihr
Text bleibt beim Inspector und den Projekteinstellungen. Die Tipp-Pause, die das Zusammenfassen von
Undo-Schritten beendet, wurde zu einer neuen Präferenzgruppe `Editor`
(`paragraphMergePauseMillis`, Vorgabe 600), gemäß `fx-model` gespiegelt und einmal gelesen, wenn der
Undo-Stack `MainWindowView -> Editor -> BookPartEditor` übergeben wird; noch kein Steuerelement im
Einstellungsdialog. `BookPartEditor` (`app/ui`) bettet `PaperFlowView` ein und baut bei jeder
gemeldeten Bearbeitung und jeder Designänderung das Modell über die `lib/layouting-model`-Builder,
`GreedyLineBreaker(JavaFxTextMetrics)` und `LayoutEngine.layout` neu auf, dann gibt es das frische
Layout zurück; das erste Layout fällt auf die schlichte Inhaltsbreite der Seite zurück, bis
`PaperFlowView` seine eigene meldet, und ein leerer beschreibbarer Teil wird mit einem leeren
Absatzblock bestückt. `app/ui` hängt jetzt von `lib/ai-ghost-layouting-model` ab. Das Teilen an einem
Umbruch bleibt bei IP-21, strukturelle Absatzoperationen bei IP-11.

### IP-11: Paragraph Structure Operations ✅

Plan entfernt, war `FP-001-IP-11-AbsatzOperationen.md`.

Blockliste, Layout und Cursorziel ändern sich zusammen und sind eine Transaktion. Das Teilen
innerhalb eines Absatzes ist der Fall, der einen Off-by-one in der Zeichenbereich-Abbildung von IP-03
offenlegt. Die Bibliothek fordert die Operation an, die Anwendung führt sie aus.

Wie geplant gebaut: `PaperFlowListener.onMoveRequested`, `PaperFlowView.requestCaret` als einmalig
konsumierbares Caret-Ziel mit Vorrang vor dem Caret-Memo, die reinen Listenfunktionen
`splitParagraph`/`mergeParagraph`/`removeParagraph`/`moveParagraph` in `BookPartEditorController`,
und `ParagraphListUndoEntry` als neuer struktureller Undo-Eintrag, gepusht statt aufgezeichnet. Um
zwei vom Nutzer angeforderte Ergänzungen erweitert: einen vorbestehenden, beim Testen entdeckten
Fehler, bei dem schnelles Tippen den Caret auf seine Position vor dem gerade eingefügten Zeichen
zurücksetzte (behoben über eine über den Rebuild hinweg gemerkte Textabbildung je Block), und eine
zeilen-bewusste Pfeiltasten-Navigation zwischen Blöcken (Pfeil hoch/runter an der ersten bzw. letzten
umbrochenen Zeile, gegen die echten Zeilengrenzen des Layout-Ergebnisses geprüft, nicht gegen eine
`TextArea`-interne Zeilenzahl). Details in der Statusdatei des Features.

### IP-12: Inspector Shell And Content Sections ✅

Plan: `FP-001-IP-12-InspectorGrundgeruest.md` (bei Abschluss entfernt)

Abschnitte bleiben fest und identisch benannt, da der Inspector teilbezogene und projektbezogene Daten
mischt und ein Panel, das still seine Form ändert, unklar macht, was bearbeitet wird.

Wie geplant gebaut: ein neues `Inspector`-MVVM-FX-Trio (`Inspector`, `InspectorView`,
`InspectorViewModel`, `InspectorView.fxml`) mit zwei festen `TitledPane`-Abschnitten, „Book“ und
„Chapter“, jeder mit seiner eigenen laufzeit-only `expandedProperty` im View-Modell. Das
`BookEditor`-Trio, sein FXML, CSS und seine Tests wurden vollständig per `git rm` entfernt statt nur
gekürzt – nachdem Titel, Titelzeilen, Autor, Copyright und die buch-ebenen Prompts in den
Inspector-Abschnitt „Book“ gewandert waren, blieb in `BookEditor` nichts Nutzersichtbares übrig.
`Editor`/`EditorView`/`EditorViewModel` verdrahten `Inspector` jetzt in die Split-Pane statt.
Eine Ergänzung über den ursprünglichen Umfang hinaus: `lib/fx-model` erhielt eine öffentliche Factory
`ChapterProperty.of(chapter: Chapter): ChapterProperty` (im selben Paket wie `ChapterProperty`, unter
Verwendung seines vorhandenen internen argumentlosen Konstruktors plus `set(chapter)`), da das Bauen
einer bindbaren `ChapterProperty` aus einem im Projektbaum gewählten schlichten `Chapter` kein
Gegenstück hatte; ihr KDoc wurde entsprechend korrigiert und ein Entwicklertest
(`ChapterPropertyOfTest`) hinzugefügt.

### IP-13: Design Style Sections ✅

Plan: `FP-001-IP-13-DesignStilAbschnitte.md` (bei Abschluss entfernt)

Der Abschnitt schreibt dieselbe `DesignProperty`, die das Layout liest, was das Live-Update ohne
zusätzliche Verkabelung funktionieren lässt. Nur installierte Familien werden angeboten, da eine
nicht auflösbare Familie nicht gemessen werden kann.

Enger gebaut als der Plan beschrieb, da die Beschränkung des Familienkatalogs, der
familienspezifische Beispieltext und die Kennzeichnung nicht installierter Familien bereits in
`StyleDataEditor` existierten (gebaut für `BookPartPageDesignSettings` aus IP-14), statt hier neue
Arbeit zu sein – `StyleDataEditorViewModel.familyName` prüft bereits `FontCatalog.contains(...)`, und
die Familien-Combobox rendert jeden Eintrag bereits in seiner eigenen Schrift. Was dieser Plan
tatsächlich hinzufügte, war ein dritter, stets aktiver `TitledPane`-Abschnitt „Design“ im
`Inspector`, neben „Book“ und „Chapter“, mit vier wiederverwendeten `StyleDataEditor`-Instanzen
(Titel, Kapiteltitel, Kapiteltitel-Anhang, Fließtext), direkt gebunden an
`project.designProperty.titlePageProperty.titleStyleProperty` und die drei entsprechenden
Eigenschaften von `chapterPageProperty` – anders als die beiden anderen Abschnitte folgt dieser nicht
der Auswahl im Projektbaum, sondern nur der Frage, ob überhaupt ein Projekt gebunden ist
(`InspectorViewModel.designAvailable`). Die vier Editoren werden von `InspectorView` gehalten und an
`InspectorViewModel` weitergereicht, genauso wie `BookPartPageDesignSettingsView` seine drei
weiterreicht. `StyleDataEditor` erhielt eine kleine Ergänzung, `release()`, das an das ohnehin interne
`StyleDataEditorViewModel.release()` delegiert, sodass der Abschnitt seine Bindungen sauber löst, wenn
das Projekt geschlossen wird, statt sie an einem verwaisten Design hängen zu lassen.

### IP-14: Project Settings Dialog ✅

Plan: `FP-001-IP-14-ProjekteinstellungenDialog.md` (bei Abschluss entfernt)

Millimeter werden gezeigt, Punkte gespeichert. Eine Randsumme, die die Seite überschreitet, muss
abgelehnt werden, sonst erhält die Engine eine negative Spaltenbreite.

Breiter gebaut als zunächst geplant, auf Wunsch des Nutzers: Der Dialog ist eine Master-Detail-Hülle
mit einem `ProjectSettingsTree` (Wurzel verborgen) links und dem Abschnitts-Editor rechts. Nur der
`General`-Abschnitt ist echt – `GeneralSettings` mit Seitengrößen-Vorgaben, den vier Rändern und den
beiden Leerseiten-Flags, gebunden an eine Arbeitskopie-`DesignProperty`. Der `Design`-Knoten und
seine vier Kinder (`Epilog`, `Chapter`, `Prolog`, `Blurb`) sind `PlaceholderSettings`-Panels; ihre
echten Stil-Editoren bleiben bei IP-13. Es wurde kein neues Modell hinzugefügt – die Design-POJOs für
Prolog/Epilog/Klappentext und einen getrennten Titelanhang sind weiterhin offen und wurden auf einen
späteren Schritt aufgeschoben.

Der Dialog hält eine Arbeitskopie (eine losgelöste `ProjectProperty`, deren Design eine tiefe Kopie
des Ziels ist); OK und APPLY schreiben die Seitengeometrie und die beiden Flags in die echte
`DesignProperty` zurück, CANCEL / ESCAPE verwerfen. Die Schaltflächen sind die Standard-`OK`,
`CANCEL`, `APPLY` (`DialogButtons.OK_CANCEL_APPLY`); APPLY ist konsumiert, sodass es speichert ohne
zu schließen, und OK und APPLY sind deaktiviert, solange die Eingabe nicht gespeichert werden kann.
Der Menüeintrag und die Werkzeugleisten-Schaltfläche in `MainWindowView` sind jetzt verdrahtet; die
Aktion ist immer verfügbar, da ein Projekt immer ein Design trägt. Das Styling landete in einem neuen
`styles/component/project-settings.css` (in `AiGhostTheme` registriert) statt in `dialog.css`, da die
Combobox, das Kontrollkästchen und der schlichte Trenner hier zuerst verwendet werden.
Nutzerdokumentation: `docs/docs/project-settings.md`.

Nachträglich auf Wunsch des Nutzers angepasst: Der Seitenformat-Editor wanderte vom `General`-Knoten
auf den `Design`-Knoten, sodass `Design.implemented` jetzt der echte Editor und `General` ein leerer
Platzhalter ist; der Dialog öffnet auf `Design`. Der `Design`-Zweig erhielt zwei weitere
Platzhalter-Kinder, `Title page` und `Copyright page`, vor `Epilog`. `ProjectSettingsSection` wurde
von einem `enum` in ein `sealed interface` mit `data object`-Fällen umgeformt, analog zu
`ProjectListItem`, und `ProjectSettingsTreeView` baut seinen Baum jetzt aus expliziten
`TreeItem`-Feldern mit einer benannten `ProjectSettingsTreeCell`, genauso wie `ProjectListView` – so
kann ein Abschnitt, der später für einen einzelnen Buchteil steht, eine `data class` werden, ohne zu
ändern, wie der Baum gebaut wird.

### IP-15: Editor Arrangement And Tree Routing

Plan: `FP-001-IP-15-EditorAufteilungUndBaumRouting.md`

`ProjectList` behält seine API; das Routing ist ein erschöpfendes `when` in `EditorViewModel`, sodass
ein Knoten, der später eine Bedeutung bekommt, ein Compilerfehler ist. View-Zustand geht zu
`Preferences`, nie in das Projektdokument.

### IP-16: Writing And Preview Modes

Plan: `FP-001-IP-16-SchreibUndVorschauModus.md`

Hier wird die FX-Thread-Einschränkung für den Nutzer sichtbar: Das ganze Buch muss gemessen werden,
bevor seine Seitenzahl bekannt ist. Eine Fortschrittsanzeige für das erste Layout muss eingeplant
werden, statt sie wegzuhoffen. Die Leseposition ist eine Absatzreferenz, kein Scroll-Offset, da die
Modi unterschiedliche Geometrie haben.

### IP-17: AI Action Port ✅

Plan entfernt, war `FP-001-IP-17-AiAktionsPort.md`.

Streaming ist von Anfang an in der Schnittstelle, da ein nachträgliches Einfügen jeden Aufrufer
ändert. Eine definierte Teilungsregel hält ein generiertes Kapitel davon ab, in einem Absatz zu
landen. Kein Stub und kein echter Provider wird hier ausgeliefert – ein konkreter KI-Provider ist
gänzlich außerhalb des Umfangs dieses Features und kommt erst über das Plugin-System eines eigenen,
späteren Features. Jede Stelle, die in eine Implementierung rufen würde, trägt ein offenes `TODO`.

Wie geplant gebaut, auf genau diese Einschränkung eingegrenzt: `lib/ai` erhielt das Paket
`org.pcsoft.app.aighost.ai.action` mit dem versiegelten `AiActionRequest` (`Rewrite`, `Expand`,
`Shorten`, `GenerateChapter`), dem Port `AiAction` (Streaming-Callback, Cancel-Handle, keine
Implementierung, ein `TODO`, das das künftige Provider-Feature benennt), `AiActionCallback`,
`AiActionHandle`, dem versiegelten `AiActionError` (`LimitExceeded`, `Cancelled`, `Failed`), dem
eigenständigen `AiActionLimits.check` (liest `Preferences.Ai`, gibt Arrow `Either` zurück, meldet die
`TokenUtils`-Schätzung neben der Zeichenbegrenzung) und `ParagraphSplitter`. `lib/ai` hängt von nun
an von `lib/model` und Arrow ab. Tests decken nur die beiden reinen Funktionen ab, `AiActionLimits`
und `ParagraphSplitter`, da keine `AiAction`-Implementierung existiert, gegen die getestet werden
könnte.

Der Port bleibt unverändert erhalten und wird auf Wunsch des Nutzers für eine spätere
Wiederverwendung nicht zurückgebaut. IP-18 und IP-19 verdrahten ihn in diesem Feature dennoch nicht –
ihre KI-Schaltflächen rufen nur eine leere `*View`-Methode mit `TODO(...)`.

### IP-18: AI Actions On Paragraph And Heading

Plan: `FP-001-IP-18-AiAktionenAmAbsatz.md`

Die schwebende Leiste am fokussierten Block wird als reine Bedienoberfläche gebaut: Umschreiben,
Ausbauen und Kürzen als Schaltflächen mit ihren Icons und ihrem Hover-/Fade-Verhalten. Jede
Schaltfläche ist per FXML `onAction` an eine parameterlose Methode des `*View`-Controllers gebunden,
deren einziger Rumpf `TODO("AI action: …")` ist. Keine Busy-Anzeige, kein Abbruch, kein
Ersetzungspfad, kein Undo-Eintrag, kein Fehlerweg, keine Verdrahtung an den `AiAction`-Port aus
IP-17 – all das kommt mit dem Plugin-System-Feature. Die Bibliothek bekommt keinen Begriff von einer
KI.

### IP-19: AI Part Generation (nur Schaltfläche) ✅

Im Inspector-Abschnitt des Kapitels steht eine einzige KI-Schaltfläche „Kapitel generieren“ (auf
Nutzerwunsch statt „Teil generieren“). Sie ist per FXML `onAction` an eine parameterlose Methode des
`InspectorView`-Controllers gebunden, deren einziger Rumpf `TODO("AI action: generate-part")` ist.
Keine Streaming-Anzeige, kein vorläufiger Zustand, kein Annehmen/Verwerfen, kein Auflösen bei
Teilwechsel, keine Verdrahtung an den `AiAction`-Port aus IP-17 – die gesamte Generierung gehört zum
Plugin-System-Feature.

### IP-21: In-Paragraph Sheet Split

Plan: `FP-001-IP-21-SeitentrennungImAbsatz.md`

Der Teilungspunkt bewegt sich mit jedem Tastendruck, daher werden Steuerelemente verbunden und
geteilt, während der Cursor in ihnen sitzt – das ist der schwierige Teil. Bewusst zuletzt: Das
Feature ist ohne es nutzbar, und der Aufwand ist erst gerechtfertigt, wenn man mit der Markierungslinie
gelebt hat. Es ist eine Rendering-Angelegenheit und landet in der Bibliothek.

### IP-23: Optional Book Parts In The Tree

Plan: `FP-001-IP-23-OptionaleTeileImBaum.md`

Der eine Plan, der den Projektbaum ändert, und bewusst eng: Struktur und `selectedItem`-API bleiben,
ein Kontrollkästchen wird auf genau drei Knoten hinzugefügt. `CheckBoxTreeItem` wendet seinen Haken
standardmäßig auf den Teilbaum an, was eingeschränkt werden muss. Da IP-24 den Text behält, zerstört
das Löschen eines Hakens nichts – es ist weiterhin ein Undo-Eintrag.

## 8. Abhängigkeitsgraph

```text
IP-01✅┬─> IP-22✅
       └─┐
IP-25✅┬─┴─> IP-26✅─┬─> IP-07✅┬─> IP-27 ──> IP-28
                    ├─> IP-08✅┘
                    └─> IP-13✅  (mit IP-02, IP-12✅)
IP-02✅┬───> IP-03✅ ──> IP-04✅┬─> IP-05✅ ─────────────┐
IP-24✅┤  │                   │                       │
       └─> IP-14✅              ├─> IP-07✅┬────────────┤
                                │          ├─> IP-06✅   │
                                └─> IP-08✅┘            │
                                      │                 │
                                      └─> IP-10✅ ───────┼──> IP-18
                                      (mit IP-09✅)      │
                                                └─> IP-11 ─┬─> IP-15 ─┬─> IP-16
                                                           │          └─> IP-23   (mit IP-24)
                                                           └─> IP-21
IP-09✅ ──> IP-10✅
IP-12✅┬─> IP-13✅
       ├─> IP-15
       └─> IP-19✅
IP-17✅  (Port bleibt für spätere Wiederverwendung; von IP-18/IP-19 nicht verdrahtet)
IP-05✅, IP-07✅, IP-15 ──> IP-16
IP-07✅, IP-08✅ ──> IP-06✅, IP-27
```

Der Graph ist als zwei Bäume gezeichnet, die aus verschiedenen Wurzeln wachsen und sich nur an einer
Naht treffen.

**Oberer Baum – die Renderer-Bibliothek `lib/layouting-fx`.** Wurzeln: IP-01 und IP-25. Er baut den
wiederverwendbaren JavaFX-Renderer, der keinen Typ dieser Anwendung trägt: Schrifterkennung und
Textmessung (IP-01), den Bericht zu Schriftidentität und Ersetzung (IP-22), das Bibliotheksmodul mit
seinem JPMS-, TestFX- und CI-Setup (IP-25), den Umzug von Katalog, Auflösung und Messung in es
(IP-26), die beiden Views – exakte Seite und Schreibfluss (IP-07, IP-08) –, die Styling- und
Theming-API (IP-27) und den Beleg der eigenständigen Wiederverwendung mit ihrer Dokumentation (IP-28).
Der Baum besitzt alles, was ein Verbraucher außerhalb dieses Repositorys ebenfalls bekäme.

**Unterer Baum – die Schreibfläche in `app/ui`.** Wurzeln: IP-02 und IP-24, plus die unabhängigen
Stränge IP-09 ✅, IP-12 ✅ und IP-17 ✅. Er baut das Editor-Feature auf der Bibliothek: das
Design-Seitenformatmodell (IP-02) und die stets vorhandenen optionalen Teile (IP-24), den
toolkit-freien Layout-Kern (IP-03), Paginierung und die Seitenumbruch-Policy (IP-04), inkrementelles
Layout und Caching (IP-05 ✅), den Projekteinstellungsdialog (IP-14), den Layout-Regressionsprüfstand
(IP-06 ✅), die Bearbeitungsfläche und Absatzoperationen (IP-10, IP-11), die Editor-Aufteilung mit
Schreib- und Vorschaumodus (IP-15, IP-16), Undo und Redo (IP-09 ✅), die Inspector-Hülle und ihre
Inhaltsabschnitte (IP-12 ✅), den KI-Aktions-Port für spätere Wiederverwendung (IP-17 ✅) und die
KI-Schaltflächen ohne Wirkung, die ihn nicht verdrahten (IP-18, IP-19), die optionalen Teile im
Projektbaum (IP-23) und das Blatt-Teilen innerhalb eines Absatzes (IP-21).

**Die Naht.** Der untere Baum verbraucht IP-07 und IP-08 des oberen – die App zeichnet ihre Seiten mit
den Bibliotheks-Views. IP-13 (Design-Stilabschnitte) ist die zweite Verbindung: Sie braucht IP-26 des
oberen Baums zusammen mit IP-02 und IP-12 ✅ des unteren. Sonst kreuzt nichts zwischen den beiden.

Abgeschlossen: **IP-01** ✅, **IP-22** ✅, **IP-02** ✅, **IP-24** ✅, **IP-03** ✅, **IP-25** ✅,
**IP-26** ✅, **IP-09** ✅, **IP-12** ✅, **IP-17** ✅, **IP-04** ✅, **IP-07** ✅, **IP-08** ✅,
**IP-13** ✅, **IP-10** ✅, **IP-06** ✅, **IP-05** ✅.
Unabhängige Ausgangspunkte: **IP-09** ✅, **IP-12** ✅, **IP-17** ✅.

## 9. Risiken und offene Fragen

* **JavaFX in einem Bibliotheksmodul** wurde bestätigt und ist geklärt. `.claude/rules/architecture.md`
  benennt jetzt die JavaFX-Komponentenbibliothek unter `lib` als erlaubten Ort für das Toolkit. Keine
  Entscheidung blockiert mehr einen Plan.
* **Benennung der Renderer-Bibliothek.** `ai-ghost-layouting-fx` behält die Konvention der
  Schwestermodule, trägt aber den Anwendungsnamen in eine zur Wiederverwendung gedachte Bibliothek.
  Offen.
* **Veröffentlichung der Renderer-Bibliothek** als echtes Artefakt oder nur innerhalb dieses
  Repositorys. Offen; IP-28 nimmt die Behandlung der anderen Bibliotheksmodule an.
* **TestFX in einem Bibliotheksmodul** hat hier keinen Präzedenzfall; Teil von IP-25 aus diesem Grund.
* **Umfang von `PaperFlowView`.** Eine vollständige Textbearbeitungskomponente und der Teil, der später
  am ehesten ai-ghost-spezifisches Verhalten will. Jeder solche Bedarf wird durch eine API
  beantwortet, nie durch eine Abhängigkeit zurück in die Anwendung.
* **Das Messen gehört dem FX-Thread.** Das zentrale technische Risiko. IP-05 beantwortet es und muss
  es messen; IP-16 muss die Kosten zeigen statt sie zu verbergen.
* **Die Standardschrift eines neuen Projekts** ist offen: `FontData` fällt auf `Arial` zurück, das
  nicht überall installiert ist; eine Vorgabe, die über die Fallback-Kette auflöst, wird benötigt.
* **Die Insets des nativen Textsteuerelements** verschieben seinen Umbruch gegen den der Engine. IP-08
  nimmt sie aus der Spaltenbreite; IP-06 fängt eine verbleibende Abweichung.
* **Der volle Text eines Blocks wird rekonstruiert, nicht gespeichert.** `DocumentLayout` trägt nur
  umbrochene Zeilen, daher fügt `PaperFlowView` die Zeilen eines Blocks mit einem einzelnen Leerzeichen
  dazwischen wieder zusammen; ein harter Zeilenumbruch innerhalb eines Blocks, sollte je einer
  existieren, würde den Umlauf nicht überstehen. Ein solcher Fall existiert noch nicht.
* **Die Übereinstimmung gilt pro Maschine.** IP-22 macht eine Ersetzung sichtbar; es kann sie nicht
  beseitigen.
* **Kerning über Wortgrenzen hinweg geht verloren.** Wörter werden einzeln gemessen, daher ist eine
  Blocksatzzeile geringfügig zu breit. Engine und Renderer irren auf dieselbe Weise, sodass die
  Treuekette hält. In IP-26 zu prüfen.
* **Witwen, Waisen, Silbentrennung** sind ausgeschlossen; der Hook existiert, keine Implementierung
  wird ausgeliefert.
* **Kein KI-Provider wird mit diesem Feature ausgeliefert**, nicht einmal ein Stub. `lib/ai` (IP-17)
  trägt nur die Aktionsschnittstelle; sie bleibt für eine spätere Wiederverwendung stehen und wird in
  diesem Feature nicht verdrahtet. IP-18 und IP-19 bauen nur Schaltflächen, deren `onAction` auf eine
  leere `*View`-Methode mit `TODO(...)` zeigt. Jeder Provider – eingebaut wie nutzergeliefert – kommt
  erst mit dem späteren Plugin-System-Feature. Dies ist eine harte Einschränkung: Kein Plan dieses
  Features darf einen Stub, einen Mock-Provider oder irgendeine andere Interaktion mit einer
  tatsächlichen oder simulierten KI hinzufügen.
* **Drei neue Gradle-Module** erfordern eine Prüfung gegen den `ci-pipeline`-Skill.

### Abgelehnte Drittanbieter-Bibliotheken

Mit dem Grund festgehalten, da die Frage sonst wiederkehrt.

* **Apache PDFBox** trägt überhaupt keine Layout-Engine – Zeilenumbruch, Fluss, Ausrichtung und
  Paginierung sind die Arithmetik des Aufrufers. Es steuert Schrift-Parsing, -Einbettung und
  PDF-Schreiben bei, was das Export-Feature braucht und dieses nicht. Es bleibt dort die getroffene
  Wahl.
* **Apache FOP** hat eine echte Engine, aber ihre Eingabe ist XSL-FO-XML – unbrauchbar für
  inkrementelles Layout –, ihr platziertes Ergebnis hat keine stabile öffentliche API, es misst mit
  eigener Schriftbehandlung, und es ist eine schwere, nicht-modulare Abhängigkeit gegen ein
  jlink-Image.
* **`java.awt.font.TextLayout` / `LineBreakMeasurer`** messen über Java2D, während Prism malt.
  Umbruchgelegenheiten kommen bereits aus `java.text.BreakIterator`, Trefferprüfung aus
  `Text.hitTest`, und `requires java.desktop` würde eines der größten JDK-Module in das Image ziehen.
* **SWT `TextLayout`** stimmt in der Fähigkeit überein, braucht aber ein `Display` mit eigener
  Ereignisschleife, sodass die Anwendung eine SWT-Anwendung werden müsste. Natives Artefakt pro
  Plattform, und EPL-2.0 steht nicht auf der Allowlist.

Die gemeinsame Ursache ist nicht zufällig: Eine Layout-Engine ist eine Messung plus ein
Umbruchalgorithmus, daher bringt jeder Kandidat mit echtem Layout seine eigene Messung mit. Das Layout
zu nehmen und dabei die JavaFX-Messung zu behalten, ist keine Kombination, die existiert, und die
Treuekette schließt alle vier aus.

### Getroffene Entscheidungen

* **Seitenformat** A5 als Vorgabe, Vorgaben A4, 12,5 x 19 cm, 13,5 x 21,5 cm, 6 x 9 Zoll; Ränder 20 mm
  innen, 15 mm außen, 15 mm oben, 20 mm unten.
* **Das Vorwerk** ist die Titelseite, gefolgt direkt von der Copyright-Seite.
* **Der Klappentext** ist immer das letzte Blatt, durch eine harte Kante abgesetzt, ohne Seitenzahl.
* **Optionale Teile** beginnen immer auf einer eigenen Seite, behalten ihren Text, sind ausgegraut
  und bleiben beschreibbar.
* **Der Metrik-Fingerabdruck** wird über druckbares ASCII plus Umlaute und scharfes s bei 12 pt
  genommen, mit Ascent, Descent und Leading. Menge und Größe sind ab diesem Punkt fest.
* **Der Renderer ist eine Bibliothek**, keine Komponente von `app/ui`.

### Bewusst außerhalb des Umfangs

* **Export in jeglicher Form.** Ein eigenes Feature, als Plugin auf Apache PDFBox umgesetzt. Was
  dieses Feature ihm schuldet, ist ein toolkit-unabhängiges `DocumentLayout` und eine dritte
  `TextMetrics`-Implementierung, die sich einfügt, ohne die Engine zu berühren. Alles, wofür eine
  Schriftdatei gelesen werden muss, gehört dorthin.
* **Plugin-Infrastruktur.** `ai-ghost-plugin-api` trägt nur `ProjectPart` und `ProjectPartInfo`; es
  gibt keine Plugin-Schnittstelle, keinen Loader und keine Service-Registrierung. Ein plugin-basierter
  Export braucht das zuerst gebaut – ein eigenes Feature und eine Voraussetzung des Exports, nicht
  dieses.
* **Jeder KI-Provider, Stub oder echt.** `lib/ai` trägt nur die Aktionsschnittstelle (IP-17); sie
  bleibt für eine spätere Wiederverwendung stehen und wird in diesem Feature nicht verdrahtet. Keine
  Implementierung wird geschrieben, getestet oder verdrahtet – nicht einmal ein deterministischer
  Stub zum Testen. Ein künftiges Feature führt ein Plugin-System für Provider ein, liefert die
  eingebauten Provider über denselben Mechanismus aus und ist der einzige Ort, an dem ein Provider
  auftauchen darf. Die KI-Schaltflächen von IP-18 und IP-19 rufen nur eine leere `*View`-Methode,
  deren Rumpf `TODO("AI action: …")` ist.

## 10. Abschlusskriterien des Features

* Titelseite, Prolog, jedes Kapitel, Epilog und Klappentext können in der Anwendung vollständig
  geschrieben werden; der Platzhalter „Not implemented yet.“ ist weg, und die Copyright-Seite folgt
  der Titelseite.
* Prolog, Epilog und Klappentext werden aus dem Baum geschaltet; ein ausgeschalteter Teil behält
  seinen Text, ist ausgegraut, aus der Nummerierung genommen und bleibt beschreibbar. Der Klappentext
  ist das letzte Blatt ohne Zahl.
* Ein vor diesem Feature geschriebenes Dokument öffnet mit genau den Teilen, die es früher hatte.
* Text wird in der Typografie, den Rändern und der Seitenstruktur des Buches geschrieben, mit
  Seitenumbrüchen dort markiert, wo das gedruckte Buch umbricht.
* Ein Absatz fällt in der Schreibfläche und in der Vorschau auf dieselbe Seite, und der Build
  scheitert, wenn die beiden auseinanderdriften.
* Schriften kommen aus den installierten Familien; keine Manuskriptschrift wird ausgeliefert und keine
  Schriftdatei geöffnet. Eine fehlende oder anders messende Schrift wird mit ihrem Ersatz gemeldet.
* Ein geänderter Designwert ändert den offenen Text, ohne das Projekt neu zu öffnen.
* Das Schreiben in einem buchgroßen Dokument bleibt reaktionsschnell, und das erste Layout eines
  langen Buches erscheint nicht als eingefrorenes Fenster.
* Prompts, Teildaten und Design sind neben dem Blatt erreichbar und unterbrechen den Text nie.
* Jede Textänderung und jede Strukturänderung kann rückgängig gemacht und wiederhergestellt werden.
* Für einen Absatz, eine Überschrift und einen ganzen Teil ist je eine KI-Schaltfläche erreichbar;
  jede ist per FXML `onAction` an eine leere, parameterlose `*View`-Methode gebunden, deren einziger
  Rumpf `TODO("AI action: …")` ist. Keine weitere KI-Logik ist in diesem Feature enthalten; der
  `lib/ai`-Aktions-Port aus IP-17 bleibt ungenutzt für eine spätere Wiederverwendung stehen.
* Das Layout-Ergebnis trägt keinen Toolkit-Typ, sodass ein späteres Export-Plugin es unverändert
  verbraucht.
* `lib/layouting-fx` baut, testet und ist als JavaFX-Bibliotheksmodul abgedeckt; seine
  Abhängigkeitsmenge ist `ai-ghost-layouting` plus JavaFX, und der Build scheitert, wenn sie wächst.
  Beide Oberflächen und das Messen leben dort, existieren nirgends doppelt, und eine Demo läuft ohne
  irgendein ai-ghost-Modul auf ihrem Pfad.
* Die Bibliothek liefert ihr eigenes Stylesheet aus und nimmt die ai-ghost-Palette über überschriebene
  Klassen an.
* JavaFX taucht in keinem Modul außer `app/ui` und `lib/layouting-fx` auf, und die Architekturregel
  sagt das.
* Der Projektbaum behält seine Struktur und seine Auswahl-API; das Kontrollkästchen auf drei Knoten
  ist die einzige Ergänzung daran.
* Build und Tests sind grün, Dokumentation und Changelog sind gemäß dem `project-docs`-Skill
  aktualisiert.
