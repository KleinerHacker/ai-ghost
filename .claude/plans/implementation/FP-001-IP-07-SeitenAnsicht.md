# IP-07: Seiten-Ansicht

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-07
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-04, IP-26
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-06, IP-16, IP-27
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-component-lifecycle`
* `testing`
* `project-docs`

## Aufgaben

### 1. Komponente

* `PaperPageView` als Control mit Skin in `lib/layouting-fx` anlegen.
* `DocumentLayout` über eine Property entgegennehmen, keine eigenen Daten halten.
* Keinen Typ aus `ai-ghost-model` in einer Signatur führen.

### 2. Blattdarstellung

* Blattfläche, Schatten und Abstand zwischen den Blättern zeichnen.
* Jede gesetzte Zeile an ihren Koordinaten zeichnen.
* Zeichenroutine hinter einer kleinen Schnittstelle kapseln.
* Dieselbe `javafx.scene.text.Font` verwenden, mit der gemessen wurde.

### 3. Zustände

* Inaktive Seiten ausgegraut darstellen.
* Harte Kante einer abgesetzten Seite zeichnen.
* Seitenzahl nur auf nummerierten Seiten zeigen.
* Jeden Zustand aus dem Layoutergebnis lesen, nichts herleiten.

### 4. Bedienung

* Zoom und Breitenanpassung anbieten.
* Zu einer Seite oder einem Block scrollen.

### 5. Virtualisierung

* Nur sichtbare Blätter im Szenengraph halten.
* Blätter beim Scrollen ein- und aushängen.

### 6. Gestaltung

* Neutrale Standarddarstellung ohne Farbe der Anwendung.
* Stilklassen für die spätere Überschreibung vergeben.

### 7. Tests

* Darstellung und Scrollziel headless über TestFX prüfen.
* Ausgegraute Seiten und fehlende Seitenzahl prüfen.
* Virtualisierung an einem langen Dokument prüfen.

### 8. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Ein `DocumentLayout` wird als Folge gedruckter Seiten gezeigt.
* Nichts an der Darstellung ist bearbeitbar.
* Der Knoten gehört der Bibliothek und kennt die Anwendung nicht.
