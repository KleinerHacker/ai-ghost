# IP-19: AI-Generierung eines Teils

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-19
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-12, IP-17
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-component-lifecycle`
* `testing`
* `icons`
* `project-docs`

## Aufgaben

### 1. Auslöser

* Aktion im Inspector-Abschnitt des Teils anbieten.
* Inhalts- und Stil-Prompt als Eingabe verwenden.

### 2. Darstellung während des Laufs

* Ergebnis beim Eintreffen auf dem Blatt zeigen.
* Eintreffende Teile gebündelt übergeben, nicht je Token.
* Erzeugte Blöcke als vorläufig kennzeichnen.

### 3. Übernehmen und Verwerfen

* Leiste mit Übernehmen und Verwerfen am Teil zeigen.
* Verwerfen stellt den vorherigen Text wieder her.
* Übernehmen als einen Undo-Eintrag aufzeichnen.
* Vorläufigen Zustand nicht speichern.
* Zustand beim Teilwechsel und beim Projektschluss auflösen.

### 4. Tests

* Vorläufiger Zustand, Übernehmen, Verwerfen und Auflösen headless prüfen.

### 5. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Ein Teil wird aus seinen Prompts erzeugt und erscheint beim Schreiben.
* Das Ergebnis wird ohne Dialog übernommen oder verworfen.
