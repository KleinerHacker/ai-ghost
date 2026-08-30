# IP-15: Editor-Aufteilung und Baum-Routing

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-15
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: IP-11, IP-12
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-16, IP-23
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-component-lifecycle`
* `fx-model`
* `testing`
* `project-docs`

## Aufgaben

### 1. Aufteilung

* `EditorView.fxml` auf drei Zonen umbauen: Baum, Blatt, Inspector.
* Inspector einklappbar führen.
* Platzhalter `Not implemented yet.` entfernen.

### 2. Routing

* `selectedProjectTreeItem` in einem erschöpfenden `when` auswerten.
* Jeden `ProjectListItem` auf Fläche und Inspector-Abschnitte abbilden.
* `ProjectList` unverändert lassen, Routing nur im View-Modell.

### 3. Ansichtszustand

* Splitter-Positionen und Einklappzustand in `Preferences` ablegen.
* Zustand beim Öffnen wiederherstellen.
* Nichts davon in das Projektdokument schreiben.

### 4. FX-Modell

* `Preferences` und `PreferencesProperty` um den Ansichtszustand erweitern.
* Mapper- und Property-Tests ergänzen.

### 5. Tests

* Routing je Baumknoten headless prüfen.
* Wiederherstellung des Ansichtszustands prüfen.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Jeder Knoten des Baums öffnet die passende Fläche.
* Der Baum selbst ist unverändert.
