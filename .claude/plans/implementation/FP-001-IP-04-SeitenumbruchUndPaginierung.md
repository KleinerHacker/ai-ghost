# IP-04: Seitenumbruch und Paginierung

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-04
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-03
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-05, IP-06, IP-07, IP-08
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `project-docs`

## Aufgaben

### 1. Seitenaufbau

* Zeilen aus IP-03 auf Seiten des Seitenformats verteilen.
* Innen- und Außenrand nach gerader oder ungerader Seite wählen.
* Gerade und ungerade Seiten über das ganze Buch hinweg zählen.
* Leerseite am Anfang und am Ende des Buches berücksichtigen.

### 2. Vorspann

* Titelseite als erste Seite des Buches setzen.
* Copyright-Seite direkt hinter die Titelseite setzen.

### 3. Optionale Teile

* Prolog, Epilog und Klappentext immer auf einer neuen Seite beginnen.
* Seiten eines ausgeschalteten Teils als inaktiv kennzeichnen.
* Inaktive Seiten aus der Seitennummerierung ausnehmen.
* Position im Layout und Seitenzahl im Buch getrennt führen.

### 4. Klappentext

* Klappentext als letztes Blatt führen.
* Keine Seitenzahl vergeben.

### 5. Ergebnistyp und Politik

* `DocumentLayout` mit Seiten, Blöcken und Aktiv-Kennzeichen definieren.
* `PageBreakPolicy` als Interface anlegen, Implementierung `NONE` mitliefern.
* Layout eines ganzen Buches über alle Teile anbieten.
* Startseitenzahl für ein einzeln gesetztes Teil entgegennehmen.

### 6. Tests

* Snapshot-Tests der Seitenstruktur je Beispielprojekt.
* Umnummerierung beim Umschalten eines optionalen Teils prüfen.
* Gerade und ungerade Ränder prüfen.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* `LayoutEngine.layout` liefert die Seitenstruktur für ein Teil und für ein ganzes Buch.
* Ein Umschalten verschiebt Seitenzahlen, ohne Seiten entstehen oder verschwinden zu lassen.
