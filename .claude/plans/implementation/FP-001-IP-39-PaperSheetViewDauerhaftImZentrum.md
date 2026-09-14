# IP-39: PaperSheetView dauerhaft im Zentrum

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-39
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`
* Löst IP-15 und IP-16 ab; übernimmt Grundfunktionen und Sync-Muster aus IP-31.

## Abhängigkeiten

* Voraussetzung: IP-38, IP-09
* Blockiert: IP-32, IP-33, IP-18, IP-23
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-component-lifecycle`
* `fx-model`
* `testing`
* `icons`
* `project-docs`

## Harte Einschränkung

* Es gibt genau eine `PaperSheetView`-Instanz und genau ein `Document` (`Book.document`); keine
  Baumauswahl tauscht es aus.
* Eine Baumauswahl navigiert über den `TextAnchor` des gewählten Knotens.
* Schreiben und Vorschau sind derselbe `Document`-Zustand in den Modi `EDITABLE`/`SELECTABLE`.

## Aufgaben

### 1. Aufteilung

* `EditorView.fxml` auf drei Zonen umbauen: Baum, Blatt, Inspector.
* Inspector einklappbar führen.
* Platzhalter `Not implemented yet.` entfernen.

### 2. Navigation statt Routing

* `selectedProjectTreeItem` löst den `TextAnchor` des gewählten Knotens auf (statisch für Titel,
  Copyright, Prolog, Epilog, Klappentext; `chapter.id` für ein Kapitel).
* `PaperSheetView` navigiert zum Anker (`CaretModel`/simPlay-0.3.1-API), baut kein neues `Document`.
* Ein erschöpfendes `when` über `ProjectListItem` bleibt bestehen, jetzt für die Anker-Auflösung statt
  für den Dokument-Aufbau.

### 3. Rückschreiben

* Der `documentProperty`-`ChangeListener`, der Bearbeitungen erkennt (Muster aus IP-31), schreibt
  direkt in `Book.document` zurück - kein `targets`-Array je Auswahl mehr nötig, da es nur noch ein
  `Document` gibt.
* Eine Struktur-Änderung, die eine Kapitelgrenze betrifft (neues/gelöschtes Kapitel über den Baum),
  hält `Book.chapters` mit den Ankern im `Document` synchron (Aufgabe von IP-38, hier nur konsumiert).

### 4. Schreib-/Vorschau-Umschalter

* Umschalter `Schreiben`/`Vorschau` in der Werkzeugleiste, Icon nach `icons`.
* Schaltet nur `paperSheetView.mode` zwischen `EDITABLE` und `SELECTABLE` um; kein zweites `Document`,
  keine zweite `PaperSheetView`, kein zweites `measure`.
* Modus in `Preferences` merken.

### 5. Erstes Layout

* `simplay-engine.measure` des ganzen, immer angezeigten Buches mit Fortschrittsanzeige begleiten.
* Messen auf dem FX-Thread; Kosten messen und in der Statusdatei festhalten.
* Fenster darf beim ersten Layout eines langen Buches nicht einfrieren.

### 6. Ansichtszustand

* Splitter-Positionen, Einklappzustand, Ansicht (Schreiben/Vorschau) und die zuletzt angesteuerte
  Anker-Position in `Preferences` ablegen.
* Zustand beim Öffnen wiederherstellen; nichts davon in das Projektdokument schreiben.
* `Preferences`/`PreferencesProperty` entsprechend erweitern, Mapper- und Property-Tests ergänzen.

### 7. Tests

* Navigation je Baumknoten headless prüfen (richtiger Anker, kein Dokument-Tausch).
* Moduswechsel behält Text und Ankerposition.
* Rückschreiben einer Bearbeitung in `Book.document`.
* Wiederherstellung des Ansichtszustands.
* Antwortverhalten beim ersten Layout eines langen Buches.

### 8. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Eine `PaperSheetView` zeigt dauerhaft das ganze Buch; jeder Baumknoten navigiert statt zu wechseln.
* Schreiben und Vorschau sind derselbe Zustand in zwei Modi.
* Jede Bearbeitung landet unmittelbar in `Book.document`.
