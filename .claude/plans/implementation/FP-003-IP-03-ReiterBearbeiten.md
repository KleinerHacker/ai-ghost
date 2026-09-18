# IP-03: Reiter Bearbeiten

## Herkunft

* Feature Plan: `.claude/plans/features/FP-003-PaneliumChromeMenu.md`
* Plan-ID im Feature Plan: IP-03
* Status-Datei des Features: `.claude/plans/features/FP-003-PaneliumChromeMenu-status.md`

## Abhängigkeiten

* Voraussetzung: IP-02
* Blockiert: IP-09 (zusätzlich zu IP-01, IP-07)
* Reihenfolge und Graph stehen in Abschnitt 7 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `icons`
* `translation`
* `testing`
* `project-docs`

## Aufgaben

### 1. Icons erstellen

* `icons`-Skill vorab laden.
* `icon-creator`-Agenten für die Icons Ausschneiden, Kopieren, Einfügen beauftragen.

### 2. Gruppe Zwischenablage

* `FXMenuGroup` mit `FXMenuGroupLargeBox(Einfügen)` als `anchor` anlegen.
* `FXMenuGroupSmallBox(Ausschneiden, Kopieren)` daneben anlegen.
* Bestehende Zwischenablage-Anbindung prüfen; fehlende Aktionen neu schaffen.

### 3. Gruppe Undo/Redo

* `FXMenuGroup` mit zwei `FXMenuGroupLargeBox` (Undo, Redo) anlegen.
* Anbindung an `viewModel.undoStack` (`canUndoProperty`/`canRedoProperty`) herstellen.

### 4. Einklappverhalten

* `FXMenuGroupBoxPriority` je Box passend setzen (Default `MEDIUM`).

### 5. Übersetzung

* Neue Message-Bundle-Keys über den `translator`-Agenten übersetzen lassen.

### 6. Tests

* Regressionstest: Undo/Redo-Historie bleibt wie bisher nutzbar.
* Test der Zwischenablage-Aktionen (Ausschneiden/Kopieren/Einfügen).

### 7. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-003-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Reiter Bearbeiten zeigt beide Gruppen funktionsfähig an.
* Undo/Redo-Historie bleibt wie bisher nutzbar; Zwischenablage-Aktionen funktionieren.
