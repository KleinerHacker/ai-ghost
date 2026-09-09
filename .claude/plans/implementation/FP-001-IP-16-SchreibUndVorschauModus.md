# IP-16: Schreib- und Vorschaumodus

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-16
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-30, IP-31, IP-15
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

* Vorschau und Schreibfläche verwenden dasselbe `Document` und dieselbe Messung.
* Kein Oberflächenvergleich nötig: Ein Absatz fällt strukturell in beiden Modi auf dieselbe Seite.

## Aufgaben

### 1. Umschalter

* Umschalter `Schreiben` und `Vorschau` in der Werkzeugleiste.
* Icon nach `icons` anlegen.
* Modus in `Preferences` merken.

### 2. Vorschau

* Ganzes Buch als ein `Document` über die Builder aus IP-30 bauen.
* Zweite `PaperSheetView` im Modus `READONLY` darüber.
* Caret und KI-Schaltflächen in der Vorschau ausblenden.
* Seitenvirtualisierung von `PaperSheetView` nutzen, nicht nachbauen.

### 3. Erstes Layout

* `simplay-engine.measure` des ganzen Buches mit Fortschrittsanzeige begleiten.
* Messen auf dem FX-Thread; Kosten messen und in der Statusdatei festhalten.
* Fenster darf beim ersten Layout eines langen Buches nicht einfrieren.

### 4. Position

* Zum im Baum gewählten Teil scrollen.
* Position über den Moduswechsel als Absatzbezug halten, nicht als Scroll-Versatz.

### 5. Statusleiste

* Seitenzahl und Gesamtseitenzahl anzeigen (aus dem `MeasuredDocument`).

### 6. Dünne Treueprüfung

* Test: ein gewählter Absatz liegt in Schreibmodus und Vorschau auf derselben Seitenposition.
* Kein Golden-File, kein `*RT`; ein einfacher headless Entwicklertest genügt.

### 7. Tests

* Moduswechsel, Position und Seitenzahl headless prüfen.
* Antwortverhalten beim ersten Layout eines langen Buches prüfen.

### 8. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Das ganze Buch ist in seinem Design zu sehen und scrollt flüssig.
* Der Moduswechsel hält die Leseposition.
* Ein Absatz liegt in beiden Modi auf derselben Seite.
