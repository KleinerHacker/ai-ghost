# IP-16: Schreib- und Vorschaumodus

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-16
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-05, IP-07, IP-15
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

### 1. Umschalter

* Umschalter `Schreiben` und `Vorschau` in der Werkzeugleiste.
* Icon nach `icons` anlegen.
* Modus in `Preferences` merken.

### 2. Vorschau

* Ganzes Buch über `PaperPageView` darstellen.
* Caret und AI-Bedienelemente in der Vorschau ausblenden.

### 3. Erstes Layout

* Erstes Layout eines langen Buches mit Fortschrittsanzeige begleiten.
* Messen auf dem FX-Thread, Anordnen außerhalb.
* Virtualisierung der Bibliothek nutzen, nicht erneut bauen.

### 4. Position

* Zum im Baum gewählten Teil scrollen.
* Position über den Moduswechsel als Absatzbezug halten, nicht als Scroll-Versatz.

### 5. Statusleiste

* Seitenzahl und Gesamtseitenzahl anzeigen.

### 6. Tests

* Moduswechsel, Position und Seitenzahl headless prüfen.
* Antwortverhalten beim ersten Layout eines langen Buches prüfen.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Das ganze Buch ist in seinem Design zu sehen und scrollt flüssig.
* Der Moduswechsel hält die Leseposition.
