# IP-04: Reiter Publish

## Herkunft

* Feature Plan: `.claude/plans/features/FP-003-PaneliumChromeMenu.md`
* Plan-ID im Feature Plan: IP-04
* Status-Datei des Features: `.claude/plans/features/FP-003-PaneliumChromeMenu-status.md`

## Abhängigkeiten

* Voraussetzung: IP-02
* Blockiert: keine
* Reihenfolge und Graph stehen in Abschnitt 7 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `translation`
* `project-docs`

## Aufgaben

### 1. Leerer Reiter

* `FXMenuTab(id, title)` für "Publish" anlegen, inhaltlich leer.
* Message-Bundle-Key für den Reitertitel anlegen.

### 2. Erweiterbarkeit

* Struktur so anlegen, dass spätere, außerhalb dieses Features liegende Arbeiten Inhalte ergänzen können.
* Kurzen Hinweis (KDoc) am Platzhalter hinterlassen, keine Business-Logik.

### 3. Übersetzung

* Neuen Message-Bundle-Key über den `translator`-Agenten übersetzen lassen.

### 4. Tests

* Smoke-Test: Reiter Publish ist sichtbar und leer.

### 5. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-003-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Reiter Publish ist sichtbar, aber ohne Inhalt.
