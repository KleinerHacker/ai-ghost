# IP-05: Inkrementelles Layout und Zwischenspeicher

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-05
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-04
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-16
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `project-docs`

## Aufgaben

### 1. Zwischenspeicher

* Layout je Absatz nach Text, aufgelöstem Stil und Spaltenbreite ablegen.
* Nur geänderte Absätze verwerfen.
* Designänderung verwirft den gesamten Zwischenspeicher.

### 2. Trennung der Schritte

* Messen und Anordnen als getrennte Schritte führen.
* Messen nur über `TextMetrics`, Anordnen ohne Toolkit-Bezug.
* Nur Wörter, Leerzeichen und Zeilenmaße messen, nie ganze Absätze.
* Vorabmessung der benötigten Wörter anbieten.

### 3. Seitengrenzen

* Seitengrenzen nach einer Verwerfung neu berechnen, ohne alles zu setzen.

### 4. Messung des Verhaltens

* Kunstbuch mit realistischer Länge als Testdatensatz anlegen.
* Dauer für einen Tastendruck und für ein Gesamtlayout erfassen.
* Messergebnisse im Plan-Status festhalten.

### 5. Tests

* Treffer und Verwerfung des Zwischenspeichers prüfen.
* Gleichheit von inkrementellem und vollständigem Layout prüfen.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Eine Absatzänderung setzt einen Absatz neu und zieht die Seitengrenzen nach.
* Messwerte für Tastendruck und Gesamtlayout liegen vor.
