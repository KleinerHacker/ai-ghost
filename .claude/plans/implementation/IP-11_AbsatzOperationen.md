# IP-11: Absatz-Operationen

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-11
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: IP-10
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-15, IP-21
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `testing`
* `project-docs`

## Aufgaben

### 1. Teilen und Verbinden

* ENTER teilt den Absatz an der Caret-Position.
* BACKSPACE am Absatzanfang verbindet mit dem vorherigen Absatz.

### 2. Anlegen und Löschen

* Leeren Absatz löschen.
* Ersten Absatz eines leeren Teils anlegen.

### 3. Verschieben

* Absatz nach oben und nach unten verschieben.

### 4. Transaktion

* Blockliste, Layout und Caret-Ziel in einem Schritt behandeln.
* Caret nach jeder Operation an der erwarteten Stelle wiederherstellen.
* Je Operation genau einen Undo-Eintrag erzeugen.

### 5. Tests

* Teilen in der Absatzmitte gegen den Zeichenbereich aus IP-03 prüfen.
* Verbinden, Löschen und Verschieben prüfen.
* Caret-Position nach jeder Operation prüfen.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Die Absatzliste eines Teils ist vollständig vom Blatt aus bearbeitbar.
* Jede Operation ist in einem Schritt rückgängig zu machen.
