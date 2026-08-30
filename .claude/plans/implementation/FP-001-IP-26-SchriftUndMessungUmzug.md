# IP-26: Umzug von Schrift und Messung

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-26
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-01, IP-25
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-07, IP-08, IP-13
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-component-lifecycle`
* `testing`
* `project-docs`

## Aufgaben

### 1. Schriftbeschreibung der Bibliothek

* Eigenen Typ mit Familie, Größe, Gewicht und Neigung in `lib/layouting-fx` anlegen.
* Keinen Typ aus `ai-ghost-model` in einer Signatur führen.

### 2. Klassen verschieben

* `FontCatalog`, `FontResolver`, `FontResolution` und `JavaFxTextMetrics` per `git mv` verschieben.
* Paket auf `org.pcsoft.app.aighost.layouting.fx.font` setzen.
* Tests der vier Klassen mit verschieben.

### 3. Signaturen umstellen

* `FontData` in jeder verschobenen Signatur durch die Schriftbeschreibung ersetzen.
* Verhalten von Auflösung, Ersatzkette und Messung unverändert lassen.
* Messhilfsknoten weiterhin einmal anlegen und wiederverwenden.
* Breiten weiterhin ungerundet zurückgeben.

### 4. Anwendungsseite

* Übersetzung von `FontData` in die Schriftbeschreibung in `app/ui` anlegen.
* Aufrufer in `app/ui` auf Bibliothek und Übersetzung umstellen.
* `module-info.java` von `app/ui` um `requires` auf die Bibliothek ergänzen.

### 5. API-Zusage

* FX-Thread-Bindung der Messung in der KDoc der Bibliothek festhalten.
* Fehlende Thread-Sicherheit ausdrücklich benennen.

### 6. Tests

* Verschobene Tests gegen die neue Signatur führen.
* Übersetzung von `FontData` in `app/ui` testen.

### 7. Abschluss

* Build über Agent ausführen.
* jlink-Image von `app/ui` auf Lauffähigkeit prüfen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Katalog, Auflösung und Messung liegen in `lib/layouting-fx`.
* `app/ui` nutzt genau diese Implementierung über eine eigene Übersetzung.
* Kein Typ der Anwendung erscheint in einer Signatur der Bibliothek.
