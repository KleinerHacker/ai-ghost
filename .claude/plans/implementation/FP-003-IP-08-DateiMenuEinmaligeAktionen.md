# IP-08: Datei-Menü: Einmalige Aktionen

## Herkunft

* Feature Plan: `.claude/plans/features/FP-003-PaneliumChromeMenu.md`
* Plan-ID im Feature Plan: IP-08
* Status-Datei des Features: `.claude/plans/features/FP-003-PaneliumChromeMenu-status.md`

## Abhängigkeiten

* Voraussetzung: IP-02
* Blockiert: keine
* Reihenfolge und Graph stehen in Abschnitt 7 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `icons`
* `translation`
* `project-docs`

## Aufgaben

### 1. Icon

* `icons`-Skill vorab laden.
* `icon-creator`-Agenten für das About-Icon ("i") beauftragen.

### 2. Quick-Actions

* `FXBackstageQuickAction`-Einträge für Preferences, About und Online Doku im Footer der Backstage aus IP-02 anlegen.

### 3. Preferences und About

* Preferences-Dialog als Platzhalter anlegen, ohne funktionale Verdrahtung.
* About-Dialog als Platzhalter anlegen, ohne funktionale Verdrahtung.

### 4. Online Doku

* `onAction` öffnet `https://kleinerhacker.github.io/ai-ghost/latest/` im externen Browser des Betriebssystems.
* Umsetzung z. B. über `java.awt.Desktop.getDesktop().browse(URI)` prüfen und einbauen.
* Kein eingebetteter Browser; diese eine Aktion wird vollständig funktional verdrahtet.

### 5. Übersetzung

* Neue Message-Bundle-Keys über den `translator`-Agenten übersetzen lassen.

### 6. Tests

* Test, dass die Online-Doku-Aktion das Öffnen der korrekten URL im externen Browser auslöst.

### 7. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-003-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Preferences, About und Online Doku erscheinen als Quick-Actions im Footer der Backstage.
* Preferences/About existieren als Platzhalter; Online Doku öffnet funktionsfähig die externe Dokumentation.
