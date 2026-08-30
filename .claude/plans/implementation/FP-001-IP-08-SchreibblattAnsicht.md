# IP-08: Schreibblatt-Ansicht

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-08
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-04, IP-26
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-06, IP-10, IP-27
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-component-lifecycle`
* `testing`
* `project-docs`

## Aufgaben

### 1. Komponente

* `PaperFlowView` als Control mit Skin in `lib/layouting-fx` anlegen.
* Seitenbreite, Ränder und Blattoptik aus IP-07 übernehmen.
* Je Block ein natives Textcontrol einsetzen.
* Keinen Typ aus `ai-ghost-model` in einer Signatur führen.

### 2. Umbruchdarstellung

* Echte Blattlücke setzen, wenn der Umbruch zwischen zwei Blöcken fällt.
* Gestrichelte Marke mit Seitenzahl zeichnen, wenn der Umbruch im Block fällt.
* Marke als Overlay über dem Block zeichnen, Control bleibt ein Stück.
* Harte Kante einer abgesetzten Seite deutlich von der Marke unterscheiden.

### 3. Spaltenbreite

* Insets und Padding des Textcontrols aus der Spaltenbreite herausrechnen.
* Berechnete Spaltenbreite über die API nach außen melden.

### 4. Bearbeitung

* Cursor, Fokus und Auswahl je Block führen.
* Fremdformatierung beim Einfügen auf reinen Text reduzieren.
* Keine Zeichenformatierung anbieten.

### 5. Ereignis-API

* Textänderung, Cursorbewegung und Fokuswechsel als Ereignis melden.
* Wunsch nach Teilen, Verbinden und Entfernen eines Blocks melden.
* Keine Änderung selbst anwenden und kein Dokument halten.

### 6. Aktualisierung

* Neuberechnung der Umbruchpositionen entprellen, etwa 100 ms.
* Textdarstellung nicht entprellen.
* Bei neuem Layoutergebnis Cursor und Fokus behalten.

### 7. Zustände

* Inaktive Blätter ausgegraut darstellen.
* Ausgegraute Blätter beschreibbar lassen.

### 8. Tests

* Umbruchmarke, Blattlücke und Spaltenbreite headless prüfen.
* Ereignisse bei Tippen, Teilen und Verbinden prüfen.
* Erhalt von Cursor und Fokus nach neuem Layout prüfen.

### 9. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Ein Layoutergebnis erscheint auf Blättern korrekter Geometrie mit Umbrüchen aus der Engine.
* In die Blöcke wird geschrieben, jede Änderung wird gemeldet statt angewendet.
* Der Knoten gehört der Bibliothek und kennt die Anwendung nicht.
