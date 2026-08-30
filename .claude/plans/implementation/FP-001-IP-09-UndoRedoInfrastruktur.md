# IP-09: Undo- und Redo-Infrastruktur

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-09
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: keine
* Start jederzeit möglich.
* Blockiert: IP-10, IP-18, IP-19
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `testing`
* `icons`
* `project-docs`

## Aufgaben

### 1. Stapel

* Undo-Stapel über Modelländerungen mit benanntem Eintrag anlegen.
* Gültigkeitsbereich je geöffnetem Projekt.
* Stapel beim Projektwechsel leeren.

### 2. Einträge

* Eintrag mit Rückgängig- und Wiederholen-Aktion führen.
* Aufeinanderfolgende Tippeingaben zu einem Eintrag zusammenfassen.
* Zusammenfassung bei Fokuswechsel oder Zeitablauf beenden.

### 3. Bedienung

* Menüeinträge und Tastenkürzel in `MainWindow` ergänzen.
* Icons für Rückgängig und Wiederholen nach `icons` anlegen.
* Beschriftung des Eintrags im Menü anzeigen.

### 4. Tests

* Rückgängig, Wiederholen und Zusammenfassung prüfen.
* Leeren beim Projektwechsel prüfen.

### 5. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Eine aufgezeichnete Änderung wird zurückgenommen und wieder angewandt.
* Fortlaufendes Tippen fällt zu einem Schritt zusammen.
