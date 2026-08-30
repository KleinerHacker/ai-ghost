# IP-02: Design mit Seitenformat

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-02
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: keine
* Start jederzeit möglich.
* Blockiert: IP-03, IP-13, IP-14
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-model`
* `testing`
* `project-docs`

## Aufgaben

### 1. Modell

* `PageFormat` mit Seitenbreite, Seitenhöhe und den vier Rändern anlegen.
* Ränder als innen, außen, oben, unten führen, nicht links und rechts.
* Alle Werte in Punkt als `Double`.
* Standard A5, Ränder 20 innen, 15 außen, 15 oben, 20 unten.
* Zeilenabstand als Faktor je Elementklasse ergänzen.
* `Design.version` auf 2 heben.

### 2. Kompatibilität

* Standardwerte für Dokumente der Version 1 setzen.
* Laden und Speichern eines Version-1-Dokuments prüfen.

### 3. FX-Modell

* `PageFormatProperty` nach dem Muster von `lib/fx-model` anlegen.
* `DesignProperty` um Seitenformat und Zeilenabstände erweitern.
* Änderungsweitergabe der verschachtelten Property sicherstellen.

### 4. Tests

* Mapper-Tests für `PageFormat` und `Design` ergänzen.
* Property-Tests nach dem Muster der bestehenden FX-Modelle ergänzen.

### 5. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* `Design` trägt Seitenformat, Ränder und Zeilenabstände.
* Ältere Dokumente öffnen mit den Standardwerten.
