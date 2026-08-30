# IP-18: AI-Aktionen an Absatz und Überschrift

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-18
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: IP-10, IP-17
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `component`
* `ui-styling`
* `fx-component-lifecycle`
* `testing`
* `icons`
* `project-docs`

## Aufgaben

### 1. Aktionsleiste

* `AiActionBar` schwebend am fokussierten Absatz oder an der Überschrift zeigen.
* Aktionen Umschreiben, Ausbauen und Kürzen anbieten.
* Icons nach `icons` anlegen.

### 2. Ablauf

* Aktion außerhalb des FX-Threads ausführen.
* Nur den betroffenen Block als beschäftigt kennzeichnen.
* Abbruch anbieten.

### 3. Ergebnis

* Text unmittelbar ersetzen.
* Ersetzung als einen Undo-Eintrag aufzeichnen.
* Fehler über die bestehenden Dialoge melden.

### 4. Tests

* Aktionsleiste, Beschäftigt-Zustand und Undo headless prüfen.
* Abbruch und Fehlerweg prüfen.

### 5. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Ein Absatz wird vom Blatt aus umgeschrieben.
* Das Ergebnis wird mit einem Undo zurückgenommen.
