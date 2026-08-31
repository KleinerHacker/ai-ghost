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

* `BookPartEditor` nach dem MVVM-FX-Muster in `app/ui` anlegen.
* `PaperFlowView` der Bibliothek einsetzen und mit den Blöcken des Teils speisen.
* Blöcke aus `BookPartProperty` über die Bauer aus `lib/layouting-model` erzeugen.
* Prolog, Kapitel und Epilog über einen einzigen Editor führen.

### 2. Bindung

* Überschrift, Überschriftzeilen und Absätze bidirektional an `BookPartProperty` binden.
* Titelseite und Copyright-Seite an `BookProperty` und `MetaProperty` binden, nur lesend.
* Klappentext-Variante ohne Überschrift und mit einem Prompt binden.

### 3. Ereignisse verarbeiten

* Textänderung der Flow-Ansicht in das Modell übertragen.
* Fokuswechsel zwischen Blöcken über die Pfeiltasten anfordern.
* Caret als Absatzindex und Zeichenversatz halten, nicht als Koordinate.

### 4. Ausgeschaltete Teile

* Ausgegraute Teile bleiben beschreibbar.

### 5. Undo

* Jede Textänderung in den Stapel aus IP-09 aufzeichnen.

### 6. Tests

* Bindung, Tippen und Fokuswechsel headless prüfen.
* Erhalt des Carets über eine Designänderung prüfen.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Die Auswahl eines Kapitels öffnet dessen Text auf dem Blatt.
* Jeder Tastendruck landet im Modell.
