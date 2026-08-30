# IP-16: Schreib- und Vorschaumodus

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-16
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

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

### 3. Virtualisierung

* Nur sichtbare Blätter im Szenengraph halten.
* Erstes Layout eines langen Buches mit Fortschrittsanzeige begleiten.
* Messen auf dem FX-Thread, Anordnen außerhalb.

### 4. Position

* Zum im Baum gewählten Teil scrollen.
* Position über den Moduswechsel als Absatzbezug halten, nicht als Scroll-Versatz.

### 5. Statusleiste

* Seitenzahl und Gesamtseitenzahl anzeigen.

### 6. Tests

* Moduswechsel, Position und Seitenzahl headless prüfen.
* Virtualisierung über ein langes Buch prüfen.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Das ganze Buch ist in seinem Design zu sehen und scrollt flüssig.
* Der Moduswechsel hält die Leseposition.
