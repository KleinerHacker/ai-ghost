# IP-02: MenuPane-Grundgerüst

## Herkunft

* Feature Plan: `.claude/plans/features/FP-003-PaneliumChromeMenu.md`
* Plan-ID im Feature Plan: IP-02
* Status-Datei des Features: `.claude/plans/features/FP-003-PaneliumChromeMenu-status.md`

## Abhängigkeiten

* Voraussetzung: IP-01
* Blockiert: IP-03, IP-04, IP-05, IP-06, IP-07, IP-08, zusätzlich IP-09
* Reihenfolge und Graph stehen in Abschnitt 7 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `translation`
* `testing`
* `project-docs`

## Aufgaben

### 1. FXMenuPane einbinden

* `MenuChromePane` in `MainWindowView.fxml`/ViewModel integrieren.
* `menuChromePane.menuPane = menuPane` und `menuChromePane.body = content` verdrahten.

### 2. Reiter anlegen

* `FXMenuTab(id, title)` für "Bearbeiten" und "Publish" anlegen.
* Beide über `menuPane.tabs.addAll(...)` registrieren, zunächst inhaltsleer.
* Message-Bundle-Keys für die Reitertitel anlegen.

### 3. Backstage-Grundgerüst

* Eigene `FXBackstageMenuPane`-Instanz erstellen.
* Instanz explizit als `menuPane.backstageContent` setzen, statt der automatischen Standardinstanz.
* Noch ohne `items` oder `quickActions`.

### 4. Doku-Abgleich

* MkDocs `menu-pane/implementation` gegen den aktuellen API-Stand von panelium-fx 0.4.0 prüfen.
* Abweichungen zur Annahme im Feature Plan im Feature Plan selbst nachtragen.

### 5. Übersetzung

* Neue Message-Bundle-Keys über den `translator`-Agenten übersetzen lassen.

### 6. Tests

* Smoke-Test: beide Reiter sichtbar, File-Tab öffnet eine leere Backstage.

### 7. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-003-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* MenuPane ist sichtbar mit den leeren Reitern Bearbeiten/Publish und einem File-Tab mit leerer Backstage.
* IP-03 bis IP-08 können unabhängig voneinander Inhalte ergänzen.
