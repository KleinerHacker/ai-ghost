# IP-28: Eigenständige Nutzung und Dokumentation

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-28
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-27
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: nichts
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `ci-pipeline`
* `project-docs`

## Aufgaben

### 1. Demo

* Eigenen Quellsatz `demo` im Modul anlegen.
* Dokument aus einfachen Textblöcken aufbauen, ohne ai-ghost-Modul.
* Beide Knoten in einem Fenster zeigen.
* Demo nicht in das veröffentlichte Artefakt aufnehmen.

### 2. Abhängigkeitsprüfung

* Gradle-Prüfung anlegen, die verbotene Abhängigkeiten meldet.
* Verboten sind `ai-ghost-model`, `ai-ghost-layouting-model` und `app/ui`.
* Prüfung an `check` hängen.

### 3. Veröffentlichung

* Artefakt wie die übrigen Bibliotheksmodule veröffentlichen.
* Nutzer nach der Veröffentlichung nach außen fragen, falls offen.

### 4. KDoc

* Öffentliche API vollständig dokumentieren.
* FX-Thread-Bindung der Messung nennen.
* Grenze der Styling-API nennen.

### 5. Projektdokumentation

* Modul in README aufnehmen.
* MkDocs-Seite mit API, Styling und Thread-Bindung anlegen.
* Eintrag in `CHANGELOG.md` ergänzen.

### 6. Pipeline

* Demo und Abhängigkeitsprüfung in die Workflows aufnehmen.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Ein Dokument wird ohne ai-ghost-Modul auf dem Pfad gezeichnet.
* Der Build meldet jede verbotene Abhängigkeit der Bibliothek.
* Modul, API und Einschränkungen sind dokumentiert und veröffentlicht.
