# IP-35: Seitenzahl und Seiten-Modi auf simPlay 0.3.0

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-35
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`
* Löst den Nummerierungs- und Inaktiv-Seiten-Teil der TODO aus Abschnitt 9 ab.

## Abhängigkeiten

* Voraussetzung: IP-30
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-23
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-model`
* `testing`
* `project-docs`

## Aufgaben

### 1. Version

* `simplayVersion` in `build.gradle.kts` von `0.2.2` auf `0.3.0` heben.
* Lizenzbericht (`app.cash.licensee`) auf neue transitive Abhängigkeiten prüfen.
* Fehlende Lizenzen dem Nutzer vorlegen, nicht selbst freigeben.

### 2. Modell

* `fx-model`-Skill vor jeder Änderung an `Design` laden.
* Seitenzahl-Einstellungen an `Design` ergänzen: Position, Startwert, Zählmodus.
* Gespiegeltes FX-Modell in `lib/fx-model` nachziehen.
* Vorgabe: keine Seitenzahl (`PageNumberPosition.OFF`-Äquivalent im Modell).

### 3. Builder

* `BookDocumentBuilder` setzt `Document.numbering` aus den neuen `Design`-Feldern.
* Titel- und Copyright-Seiten-`id` in `excludedPageIds` aufnehmen.
* Zählmodus aus dem Modell nach `PageCountingMode` übersetzen.

### 4. Optionale Teile

* `BookPartEditor`/Vorschau setzen `PageMode.DISABLED` je Seiten-`id` eines ausgeschalteten
  Prolog/Epilog/Klappentext.
* `null` beim Wiedereinschalten, Aufruf über `setPageMode`.
* Kein Eingriff in `pageModes` bei internen Bearbeitungen, nur bei Schalterwechsel.

### 5. Tests

* Nummerierung inkl. Startwert und beider `PageCountingMode`-Varianten headless prüfen.
* `excludedPageIds` für Titel-/Copyright-Seite prüfen.
* `PageMode.DISABLED`-Zuordnung und -Aufhebung headless prüfen.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen (Projekteinstellungsdialog, CHANGELOG).

## Ergebnis

* Seitenzahlen erscheinen im Papier gemäß Design-Einstellung.
* Titel- und Copyright-Seite bleiben ungezählt.
* Ein ausgeschalteter optionaler Teil ist über `PageMode.DISABLED` wirklich inaktiv, nicht nur
  eingefärbt.
