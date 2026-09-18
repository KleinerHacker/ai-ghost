# IP-06: Datei-Menü: Öffnen

## Herkunft

* Feature Plan: `.claude/plans/features/FP-003-PaneliumChromeMenu.md`
* Plan-ID im Feature Plan: IP-06
* Status-Datei des Features: `.claude/plans/features/FP-003-PaneliumChromeMenu-status.md`

## Abhängigkeiten

* Voraussetzung: IP-02
* Blockiert: keine
* Reihenfolge und Graph stehen in Abschnitt 7 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `translation`
* `testing`
* `project-docs`

## Aufgaben

### 1. Backstage-Eintrag

* `FXBackstageMenuItem` "Öffnen" mit eigenem `content`-Node in der Backstage aus IP-02 anlegen.

### 2. Darstellung

* `viewModel.openRecent` im `content`-Node darstellen.
* Bestehende Bindings und den Recent-Mechanismus unverändert wiederverwenden.

### 3. Übersetzung

* Neue Message-Bundle-Keys, falls nötig, über den `translator`-Agenten übersetzen lassen.

### 4. Tests

* Prüfen, dass die Recent-Liste dieselben Einträge wie bisher zeigt.
* Prüfen, dass Öffnen aus der Liste weiterhin funktioniert.

### 5. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-003-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Der Backstage-Eintrag "Öffnen" zeigt die bisherige Liste zuletzt geöffneter Dateien in äquivalenter Funktionalität.
