# IP-21: Seitentrennung innerhalb eines Absatzes

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-21
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-11
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `project-docs`

## Aufgaben

### 1. Voraussetzung

* Nur beginnen, wenn sich die Umbruchmarke aus IP-08 im Gebrauch als unzureichend erweist.

### 2. Aufteilung

* Änderung liegt in `PaperFlowView` der Bibliothek, nicht in `app/ui`.
* Textcontrol eines Blocks am Umbruch in zwei Controls teilen.
* Beide Controls bleiben logisch ein Block.
* Teilstelle beim Tippen neu berechnen, ohne den Fokus zu verlieren.

### 3. Bedienung

* Caret über die Teilstelle hinweg bewegen.
* Auswahl über die Teilstelle hinweg führen.
* Ereignisse über die Teilstelle hinweg unverändert melden.

### 4. Tests

* Caret, Auswahl und Ereignisse über die Teilstelle prüfen.
* Neuberechnung der Teilstelle beim Tippen prüfen.
* Undo in `app/ui` über die Teilstelle prüfen.

### 5. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Das Schreiben sieht auch bei einem Umbruch im Absatz wie ein Blattstapel aus.
