# IP-24: Optionale Teile im Modell

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-24
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: keine
* Start jederzeit möglich.
* Blockiert: IP-03, IP-23
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-model`
* `testing`
* `project-docs`

## Aufgaben

### 1. Schalter

* Kleines Interface für abschaltbare Teile anlegen, nur für Prolog, Epilog, Klappentext.
* `BookPart` nicht erweitern, ein Kapitel bleibt ohne Schalter.
* Feld für `im Buch enthalten` mit Standard `false`.

### 2. Buch

* `Book.prolog`, `Book.epilog` und `Book.blurb` nicht mehr `null`-fähig führen.
* Teile immer mit ihren Standardwerten anlegen.
* `Book.version` auf 2 heben.

### 3. Migration

* Fehlenden Teil eines Version-1-Dokuments als ausgeschaltet lesen.
* Vorhandenen Teil eines Version-1-Dokuments als eingeschaltet lesen.
* Migration in `StorageIo` prüfen, kein Textverlust.

### 4. FX-Modell

* `PrologProperty`, `EpilogProperty` und `BlurbProperty` um den Schalter erweitern.
* `BookProperty` liefert die Teile ohne `null`.
* Änderungsweitergabe des Schalters sicherstellen.

### 5. Tests

* Mapper-Tests für Buch und die drei Teile ergänzen.
* Migration eines Version-1-Dokuments in beiden Richtungen prüfen.
* Erhalt des Textes über Aus- und Einschalten prüfen.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation und `CHANGELOG.md` nach `project-docs` prüfen.

## Ergebnis

* Prolog, Epilog und Klappentext sind immer vorhanden und tragen einen Schalter.
* Ein Dokument vor dieser Änderung öffnet mit genau den Teilen, die es hatte.
