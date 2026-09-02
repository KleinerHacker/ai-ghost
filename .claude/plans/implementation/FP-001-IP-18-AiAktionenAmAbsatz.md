# IP-18: AI-Aktionen an Absatz und Überschrift

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-18
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

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

## Harte Einschränkung

* KEIN Stub, KEIN Mock, KEINE reale Anbindung an ein AI-System in diesem Plan
* `AiAction` aus IP-17 hat keine Implementierung; der eigentliche Aufruf bleibt ein offenes TODO
* Ein Provider kommt erst über das künftige Plugin-System-Feature

## Aufgaben

### 1. Aktionsleiste

* `AiActionBar` schwebend am fokussierten Absatz oder an der Überschrift zeigen.
* Aktionen Umschreiben, Ausbauen und Kürzen anbieten.
* Icons nach `icons` anlegen.

### 2. Ablauf

* Verdrahtung bis zum Aufruf von `lib/ai` bauen, außerhalb des FX-Threads.
* Nur den betroffenen Block als beschäftigt kennzeichnen.
* Abbruch anbieten (Aufruf von `AiActionHandle.cancel()` vorgesehen).
* Stelle des tatsächlichen `AiAction.execute(...)`-Aufrufs mit TODO markieren, nicht implementieren.

### 3. Ergebnis

* Ersetzungspfad bauen: `onComplete`-Callback ersetzt Text unmittelbar, sobald ein Provider existiert.
* Ersetzung als einen Undo-Eintrag aufzeichnen.
* Fehler über die bestehenden Dialoge melden (`onError`-Callback verdrahtet).

### 4. Tests

* Aktionsleiste, Beschäftigt-Zustand und Undo headless prüfen, mit einem Test-Callback anstelle eines Providers.
* Abbruch- und Fehlerweg gegen den Test-Callback prüfen.

### 5. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Die Aktionsleiste, ihr Beschäftigt-Zustand, Abbruch, Undo und Fehlerweg stehen vollständig, verdrahtet bis zum offenen TODO an der `lib/ai`-Aufrufstelle.
* KEIN Absatz wird tatsächlich von einem AI-System umgeschrieben; das folgt erst mit dem Plugin-System-Feature.
