# IP-09: ChromePane-Schnellaktionen

## Herkunft

* Feature Plan: `.claude/plans/features/FP-003-PaneliumChromeMenu.md`
* Plan-ID im Feature Plan: IP-09
* Status-Datei des Features: `.claude/plans/features/FP-003-PaneliumChromeMenu-status.md`

## Abhängigkeiten

* Voraussetzung: IP-01, IP-03, IP-07
* Blockiert: keine (letzter Plan des Features)
* Reihenfolge und Graph stehen in Abschnitt 7 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `testing`
* `project-docs`

## Aufgaben

### 1. Icon-Buttons

* Icon-Schaltflächen Speichern, Separator, Undo, Redo in `captionLeftItems` der `ChromeCaptionBar` anlegen.

### 2. Verdrahtung

* Speichern-Button an `actionSave` anbinden.
* Undo/Redo-Buttons an `viewModel.undoStack` (`canUndoProperty`/`canRedoProperty`) anbinden.

### 3. Standardtitel ausblenden

* `defaultTitleVisible="false"` am `ChromePane` (bzw. `isDefaultTitleVisible`/`defaultTitleVisibleProperty()`) setzen.

### 4. Tests

* Prüfen, dass die Buttons funktional identisch zur bisherigen ToolBar arbeiten.
* Prüfen, dass der Standard-Fenstertitel nicht mehr angezeigt wird.

### 5. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-003-Overview.md` und im Feature Plan als erledigt markieren.
* `FP-003-Overview.md` mit `git rm` entfernen, da dies der letzte Plan des Features ist.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* ChromePane zeigt Speichern, Separator, Undo, Redo funktionsfähig in `captionLeftItems`.
* Der Standard-Fenstertitel ist ausgeblendet; das Feature ist vollständig abgeschlossen.
