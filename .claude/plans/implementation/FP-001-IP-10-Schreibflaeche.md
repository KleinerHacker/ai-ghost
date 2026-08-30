# IP-10: Schreibfläche für Buchteile

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-10
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-08, IP-09
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-06, IP-11, IP-18
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-component-lifecycle`
* `testing`
* `project-docs`

## Aufgaben

### 1. Komponente

* `BookPartEditor` nach dem MVVM-FX-Muster anlegen.
* Je Block ein mitwachsendes Textcontrol in `PaperFlowView` setzen.
* Rahmen und Hintergrund des Controls entfernen.

### 2. Bindung

* Überschrift, Überschriftzeilen und Absätze bidirektional an `BookPartProperty` binden.
* Titelseite und Copyright-Seite an `BookProperty` und `MetaProperty` binden, nur lesend.
* Klappentext-Variante ohne Überschrift und mit einem Prompt binden.

### 3. Bedienung

* Caret und Tippen im Block.
* Fokuswechsel zwischen Blöcken über die Pfeiltasten.
* Eingefügten Rich Text auf reinen Text reduzieren.
* Caret als Absatzindex und Zeichenversatz halten, nicht als Koordinate.

### 4. Ausgeschaltete Teile

* Ausgegraute Teile bleiben beschreibbar.

### 5. Undo

* Jede Textänderung in den Stapel aus IP-09 aufzeichnen.

### 6. Tests

* Bindung, Tippen und Fokuswechsel headless prüfen.
* Reduktion von Rich Text prüfen.
* Erhalt des Carets über eine Designänderung prüfen.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Die Auswahl eines Kapitels öffnet dessen Text auf dem Blatt.
* Jeder Tastendruck landet im Modell.
