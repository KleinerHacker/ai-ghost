# IP-03: Layout-Kern

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-03
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-01, IP-02, IP-24
* Jede Voraussetzung steht im Feature-Status auf `COMPLETED`, IP-03 ist freigegeben.
* Blockiert: IP-04
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `ci-pipeline`
* `project-docs`

## Aufgaben

### 1. Modul `lib/layouting`

* Modul `lib/layouting` als `ai-ghost-layouting` in `settings.gradle.kts` aufnehmen.
* Paketwurzel `org.pcsoft.app.aighost.layouting`.
* `build.gradle.kts` nach Vorbild `lib/ai/build.gradle.kts`, mit Umleitung von `compileJava`.
* Keine Abhängigkeit auf `ai-ghost-model`, kein Toolkit.
* `module-info.java` anlegen.
* Keine neue Fremdabhängigkeit.

### 2. Messschnittstelle

* `TextMetrics` als Interface im Modul definieren.
* Methoden `wordWidth`, `spaceWidth` und `lineMetrics`.
* `LineMetrics` per `git mv` aus `app/ui` in das Modul verschieben.
* `JavaFxTextMetrics` in `app/ui` implementiert das Interface.
* Deterministische Implementierung `FixedTextMetrics` im main-Sourceset mitliefern.

### 3. Stiltyp

* Eigener Stiltyp mit Familie, Grad, Schnitt, Ausrichtung, Zeilenabstand und Abständen davor und danach.
* Eigener Ausrichtungstyp mit links, rechts, zentriert und Blocksatz.
* Kein Bezug auf `StyleData`, `FontData`, `Alignment` oder `Design`.

### 4. Blockmodell

* Ein einziger Blocktyp aus Text und Stil, ohne Rolle und ohne Typunterscheidung.
* Eingabe des Umbruchs ist eine Folge solcher Blöcke plus Spaltenbreite.
* Keine Kenntnis von Titelseite, Copyright-Seite, Überschrift, Absatz oder Klappentext.

### 5. Zeilenumbruch

* Umbruchstellen über `java.text.BreakIterator.getLineInstance` bestimmen.
* Umbruch gegen eine vorgegebene Spaltenbreite über `TextMetrics`.
* Ausrichtung links, rechts, zentriert und Blocksatz.
* Blocksatz verteilt die Restbreite auf die Wortabstände, letzte Zeile bleibt linksbündig.
* Umbruchschritt hinter einem Interface kapseln.
* Jede gesetzte Zeile trägt `x`, `y`, Grundlinie, Text, Stil, Blockindex und Zeichenbereich.
* Ergebnistyp als reine Datenstruktur ohne Toolkit-Typ.

### 6. Modul `lib/layouting-model`

* Modul `lib/layouting-model` als `ai-ghost-layouting-model` in `settings.gradle.kts` aufnehmen.
* Paketwurzel `org.pcsoft.app.aighost.layouting.model`, Paketspiegelung wie in `lib/fx-model`.
* `build.gradle.kts` nach Vorbild `lib/fx-model/build.gradle.kts`, ohne JavaFX.
* `api`-Abhängigkeit auf `ai-ghost-model` und `ai-ghost-layouting`.
* `module-info.java` anlegen.

### 7. Builder

* Builder für die Titelseite aus `Book.title`, `Book.titleAppendix` und `Meta.author`.
* Builder für die Copyright-Seite aus `Meta.copyright`.
* Builder für einen `BookPart` aus `title`, `titleAppendix` und `paragraph`.
* Builder für `Blurb` aus `paragraph`.
* Übersetzung von `StyleData` und dem passenden `Design`-Zeilenabstand in den Stiltyp.
* Quellen: `titleDesign`, `authorDesign`, `copyrightDesign`, `chapterDesign`, `textDesign`.
* Abstände davor und danach ohne Modellfeld, feste Vorgabewerte im Builder.

### 8. Tests

* Umbruch und Ausrichtung gegen die deterministische Messung prüfen.
* Überlange Wörter und leere Absätze abdecken.
* Rückabbildung auf Blockindex und Zeichenbereich prüfen.
* Zweimaliger Lauf liefert identische Zahlen.
* Builder je Quelle gegen die erwartete Blockfolge und Stilzuordnung prüfen.
* Test von `JavaFxTextMetrics` auf das neue Interface nachziehen.

### 9. Abschluss

* Build über Agent ausführen.
* Pipeline nach `ci-pipeline` prüfen, zwei neue Module.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* `lib/layouting` setzt Blöcke ohne jede Kenntnis von ai-ghost.
* `lib/layouting-model` erzeugt diese Blöcke aus `Book`, `Design` und `Meta`.
* Ein Teil und ein Design ergeben eine reproduzierbare Folge gesetzter Zeilen.
* Das Ergebnis enthält keinen Toolkit-Typ.
