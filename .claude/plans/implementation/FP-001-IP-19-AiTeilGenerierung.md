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

## Harte Einschränkung

* KEIN Stub, KEIN Mock, KEINE reale Anbindung an ein AI-System in diesem Plan
* `AiAction` aus IP-17 hat keine Implementierung; der eigentliche Aufruf bleibt ein offenes TODO
* Ein Provider kommt erst über das künftige Plugin-System-Feature

## Aufgaben

### 1. Auslöser

* Aktion im Inspector-Abschnitt des Teils anbieten.
* Inhalts- und Stil-Prompt als Eingabe verwenden.
* Stelle des tatsächlichen `AiAction.execute(...)`-Aufrufs mit TODO markieren, nicht implementieren.

### 2. Darstellung während des Laufs

* Verdrahtung bauen, die ein Ergebnis beim Eintreffen (`onChunk`) auf dem Blatt zeigen würde.
* Eintreffende Teile gebündelt übergeben, nicht je Token.
* Erzeugte Blöcke als vorläufig kennzeichnen.

### 3. Übernehmen und Verwerfen

* Leiste mit Übernehmen und Verwerfen am Teil zeigen.
* Verwerfen stellt den vorherigen Text wieder her.
* Übernehmen als einen Undo-Eintrag aufzeichnen.
* Vorläufigen Zustand nicht speichern.
* Zustand beim Teilwechsel und beim Projektschluss auflösen.

### 4. Tests

* Vorläufiger Zustand, Übernehmen, Verwerfen und Auflösen headless prüfen, mit einem Test-Callback anstelle eines Providers.

### 5. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Auslöser, Darstellung, Übernehmen/Verwerfen und Auflösen stehen vollständig, verdrahtet bis zum offenen TODO an der `lib/ai`-Aufrufstelle.
* KEIN Teil wird tatsächlich von einem AI-System erzeugt; das folgt erst mit dem Plugin-System-Feature.
