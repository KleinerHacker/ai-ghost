# IP-22: Schrift-Identität und Ersatzmeldung

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-22
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: IP-01
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-model`
* `ui-styling`
* `dialog`
* `testing`
* `project-docs`

## Aufgaben

### 1. Fingerabdruck

* Referenzsatz aus druckbarem ASCII, Umlauten und Eszett festlegen.
* Bei 12 pt messen, dazu Ascent, Descent und Leading.
* Referenzsatz und Grad dauerhaft unveränderlich halten.

### 2. Modell

* `FontData` um den Fingerabdruck erweitern.
* Fehlender Fingerabdruck bedeutet `nicht erfasst`, nie `Abweichung`.
* `FontDataProperty` nachziehen.

### 3. Vergleich

* Fingerabdruck beim Wählen einer Schrift schreiben.
* Beim Öffnen eines Projekts vergleichen.
* Fallback aus IP-01 anwenden, wenn die Familie fehlt.

### 4. Meldung

* Betroffene Elemente, erwartete Schrift und Ersatz nennen.
* Über die bestehenden Dialoge melden.
* Texte über das Message-Bundle führen.

### 5. Tests

* Mapper- und Property-Tests ergänzen.
* Vergleich, fehlender Fingerabdruck und Meldung prüfen.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Ein Projekt auf einer Maschine mit anderer Messung meldet den Ersatz.
* Ältere Projekte ohne Fingerabdruck melden nichts.
