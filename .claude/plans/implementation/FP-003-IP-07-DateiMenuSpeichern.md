# IP-07: Datei-Menü: Speichern / Speichern unter

## Herkunft

* Feature Plan: `.claude/plans/features/FP-003-PaneliumChromeMenu.md`
* Plan-ID im Feature Plan: IP-07
* Status-Datei des Features: `.claude/plans/features/FP-003-PaneliumChromeMenu-status.md`

## Abhängigkeiten

* Voraussetzung: IP-02
* Blockiert: IP-09 (zusätzlich zu IP-01, IP-03)
* Reihenfolge und Graph stehen in Abschnitt 7 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-model`
* `translation`
* `testing`
* `project-docs`

## Aufgaben

### 1. Model "zuletzt verwendete Speicherorte"

* `model-explore`-Agenten ausführen, `RecentOpened` (`lib/model/pref`) als Vorbild analysieren.
* `model-creator`-Agenten für das neue Model unter `lib/model/pref` samt FX-Pendant unter `lib/fx-model/pref` beauftragen.
* `max`/`entries`, unveränderliche `add`/`remove`/`clear`-Operationen, Persistenz in `Preferences` vorgeben.
* Eigene, strukturell parallele Liste zu "Öffnen", kein gemeinsames Modell.

### 2. Backstage-Einträge

* `FXBackstageMenuItem` "Speichern" und "Speichern unter" mit eigenen `content`-Nodes anlegen.

### 3. Verdrahtung

* Auswahl eines Speicherorts an `actionSave`/`actionSaveAs` anbinden.
* Erfolgreiches Speichern ergänzt die Liste der zuletzt verwendeten Speicherorte.

### 4. Übersetzung

* Neue Message-Bundle-Keys über den `translator`-Agenten übersetzen lassen.

### 5. Tests

* Model-Tests: `add`/`remove`/`clear`, `max`-Grenze, Persistenz.
* UI-Test: Auswahl eines Speicherorts löst Speichern am gewählten Ort aus.

### 6. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-003-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Die Backstage-Einträge "Speichern" und "Speichern unter" bieten zusätzlich zur Dateiauswahl die zuletzt verwendeten Speicherorte an.
