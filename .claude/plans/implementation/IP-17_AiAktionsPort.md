# IP-17: AI-Aktionsport

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-17
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: keine
* Start jederzeit möglich.
* Blockiert: IP-18, IP-19
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `project-docs`

## Aufgaben

### 1. Schnittstelle

* Aktionen `rewrite`, `expand`, `shorten` und `generatePart` in `lib/ai` definieren.
* Anfrage- und Ergebnistypen festlegen.
* Streamendes Ergebnis von Anfang an vorsehen.
* Abbruch und Fehlermeldung vorsehen.

### 2. Grenzen

* Zeichen- und Tokengrenzen über `TokenUtils` prüfen.
* Grenzen aus den Preferences lesen.

### 3. Absatzregel

* Leerzeile beendet einen Absatz im Ergebnis.
* Ergebnis auf eine Absatzliste abbilden.

### 4. Stub

* Deterministische Implementierung ohne Netzzugriff mitliefern.
* Streamen, Abbruch und Fehler im Stub nachbilden.

### 5. Tests

* Streamen, Abbruch, Grenzen und Absatzregel prüfen.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Eine AI-Aktion ist aufrufbar, abbrechbar und ohne Netz testbar.
