# IP-03: Layout-Kern

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-03
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: IP-01, IP-02, IP-24
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* IP-24 steht auf `NOT_STARTED`, der Start von IP-03 ist bis dahin gesperrt.
* Blockiert: IP-04
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `ci-pipeline`
* `project-docs`

## Aufgaben

### 1. Modul

* Modul `lib/layouting` als `ai-ghost-layouting` in `settings.gradle.kts` aufnehmen.
* Paketwurzel `org.pcsoft.app.aighost.layouting`.
* `build.gradle.kts` nach Vorbild `lib/ai/build.gradle.kts`, mit Umleitung von `compileJava`.
* Abhängigkeit auf `ai-ghost-model`, kein Toolkit.
* `module-info.java` anlegen.
* Keine neue Fremdabhängigkeit.

### 2. Messschnittstelle

* `TextMetrics` als Interface im Modul definieren.
* Methoden `wordWidth`, `spaceWidth` und `lineMetrics`.
* `LineMetrics` per `git mv` aus `app/ui` in das Modul verschieben.
* `JavaFxTextMetrics` in `app/ui` implementiert das Interface.
* Deterministische Implementierung `FixedTextMetrics` im main-Sourceset mitliefern.

### 3. Aufgelöster Stil

* Familie, Grad, Schnitt, Ausrichtung, Zeilenabstand und Abstände davor und danach bündeln.
* Auflösung aus `Design` je Elementklasse.
* Quellen: `titleDesign`, `authorDesign`, `copyrightDesign`, `chapterDesign`, `textDesign`.
* Zeilenabstand aus den fünf `*LineSpacing`-Feldern von `Design`.

### 4. Blockmodell

* Blocktypen für Titelseite, Copyright-Seite, Überschrift, Überschriftzeile, Absatz und Klappentext.
* Erzeugung aus `BookPart` beziehungsweise `Blurb` plus `Design`.
* Autor und Copyright aus `Meta` in die beiden Seitenblöcke übernehmen.

### 5. Zeilenumbruch

* Umbruchstellen über `java.text.BreakIterator.getLineInstance` bestimmen.
* Umbruch gegen eine vorgegebene Spaltenbreite über `TextMetrics`.
* Ausrichtung links, rechts, zentriert und Blocksatz.
* Blocksatz verteilt die Restbreite auf die Wortabstände, letzte Zeile bleibt linksbündig.
* Umbruchschritt hinter einem Interface kapseln.
* Jede gesetzte Zeile trägt `x`, `y`, Grundlinie, Text, Stil, Absatzindex und Zeichenbereich.
* Ergebnistyp als reine Datenstruktur ohne Toolkit-Typ.

### 6. Tests

* Umbruch und Ausrichtung gegen die deterministische Messung prüfen.
* Überlange Wörter und leere Absätze abdecken.
* Rückabbildung auf den Zeichenbereich prüfen.
* Zweimaliger Lauf liefert identische Zahlen.
* Test von `JavaFxTextMetrics` auf das neue Interface nachziehen.

### 7. Abschluss

* Build über Agent ausführen.
* Pipeline nach `ci-pipeline` prüfen, neues Modul.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Ein Teil und ein Design ergeben eine reproduzierbare Folge gesetzter Zeilen.
* Das Ergebnis enthält keinen Toolkit-Typ.
