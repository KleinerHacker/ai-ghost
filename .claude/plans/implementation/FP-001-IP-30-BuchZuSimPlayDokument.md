# IP-30: Buch zu simPlay-Dokument-Builder

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-30
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-29, IP-02, IP-24
* Blockiert: IP-31, IP-16
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-model`
* `testing`
* `project-docs`

## Harte Einschränkung

* `lib/ai-ghost-layouting-model` hängt von `ai-ghost-model` und `simplay-engine` ab, von keinem Toolkit.
* Das simPlay-`Document` ist eine abgeleitete Sicht, kein gespeicherter Zustand.
* Fehlende ai-ghost-Seitenpolitik in simPlay wird als `TODO` markiert, nicht in ai-ghost nachgebaut.

## Aufgaben

### 1. Übersetzer umstellen

* `BookPartBuilder`, `TitlePageBuilder`, `CopyrightPageBuilder`, `BlurbBuilder` auf simPlay-Typen umschreiben.
* Ausgabe ist ein `Document` aus `Page`, `TextBlock`, `TextStyle`.
* `TextBlock.of(text, style)` je Absatz, Überschrift und Anhangzeile.
* `BlockSpacing` (Abstand über/unter einem Block) beibehalten.

### 2. Seiten je Teil

* Ein `FlowPage` je Buchteil in Reihenfolge, sodass jeder Teil auf eigener Seite beginnt.
* Titelseite und Copyright-Seite als eigene Seiten (`SinglePage` oder `FlowPage` mit festem Inhalt).
* Klappentext als letztes Blatt.

### 3. PageLayout aus PageFormat

* `PageGeometryTranslation` zu einer `PageLayout`-Berechnung umbauen: `Size` und `Margins` aus `PageFormat`.
* Innen-/Außenrand auf `Margins` abbilden.
* Millimeter nur in der UI; hier Punkte als `Double`.

### 4. TextStyle aus Design

* Design je Elementklasse (Titel, Kapiteltitel, Anhang, Fließtext) auf `TextStyle` abbilden.
* Font, Zeilenabstand (Faktor plus Leading) und Ausrichtung (`LEFT`/`RIGHT`/`CENTER`/`JUSTIFY`) setzen.
* Umbruchstrategie: `GreedyWordLineBreakerStrategy` als Vorgabe.

### 5. Seitenpolitik als TODO

* `TODO`-Kommentare an den Stellen für Seitennummerierung, gespiegelte Ränder, inaktive/leere Seiten, Klappentext-Kante.
* Zwischenlösung: feste Ränder, keine oder triviale fortlaufende Nummerierung.
* Jedes `TODO` nennt, dass die Politik nach simPlay wandert und der Builder sie dann von dort übernimmt.

### 6. module-info

* `module-info.java` von `lib/ai-ghost-layouting-model` auf `requires simplay-engine` (JPMS-Name aus IP-29) umstellen.
* `ai-ghost-layouting` aus `requires` entfernen.

### 7. Tests

* Entwicklertest: Buch mit Prolog, zwei Kapiteln, Epilog, Klappentext ergibt erwartete Seiten- und Blockzahl.
* Ausgeschalteter optionaler Teil bleibt im `Document` (nur später aus Nummerierung genommen).
* Designänderung ändert das erzeugte `TextStyle`.
* `FixedTextMetrics`-artige simPlay-Messung für Determinismus nutzen, falls verfügbar.

### 8. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen (README-Zeile des Moduls, MkDocs, CHANGELOG).

## Ergebnis

* `lib/ai-ghost-layouting-model` erzeugt aus `Book`/`Design`/`Meta` ein simPlay-`Document`.
* Jeder Buchteil beginnt auf einer eigenen Seite.
* Die fehlende ai-ghost-Seitenpolitik ist überall als `TODO` markiert.
