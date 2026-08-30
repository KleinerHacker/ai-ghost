# IP-08: Schreibblatt-Ansicht

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-08
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: IP-04
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-06, IP-10
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-component-lifecycle`
* `testing`
* `icons`
* `project-docs`

## Aufgaben

### 1. Komponente

* `PaperFlowView` nach dem MVVM-FX-Muster anlegen.
* Seitenbreite, Ränder und Blattoptik aus IP-07 übernehmen.
* Je Block einen Platz für ein natives Textcontrol anbieten.

### 2. Umbruchdarstellung

* Echte Blattlücke setzen, wenn der Umbruch zwischen zwei Absätzen fällt.
* Gestrichelte Marke mit Seitenzahl zeichnen, wenn der Umbruch im Absatz fällt.
* Marke als Overlay über dem Block zeichnen, Control bleibt ein Stück.
* Harte Kante vor dem Klappentext deutlich von der Marke unterscheiden.

### 3. Spaltenbreite

* Insets und Padding des Textcontrols aus der Spaltenbreite herausrechnen.
* Berechnete Spaltenbreite an die Layout-Engine übergeben.

### 4. Zustände

* Blätter eines ausgeschalteten Teils ausgegraut darstellen.

### 5. Aktualisierung

* Neuberechnung der Umbruchpositionen entprellen, etwa 100 ms.
* Textdarstellung nicht entprellen.

### 6. Gestaltung

* `styles/component/paper-flow.css` anlegen und einhängen.
* Texte über das Message-Bundle führen.

### 7. Tests

* Umbruchmarke, Blattlücke und Spaltenbreite headless prüfen.

### 8. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Ein Teil erscheint auf Blättern korrekter Geometrie mit Umbruchpositionen aus der Engine.
* Die Blöcke sind bereit, bearbeitbare Controls aufzunehmen.
