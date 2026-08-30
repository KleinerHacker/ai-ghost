# IP-07: Seiten-Ansicht

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-07
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: IP-04
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-06, IP-16
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-component-lifecycle`
* `testing`
* `icons`
* `project-docs`

## Aufgaben

### 1. Komponente

* `PaperPageView` nach dem MVVM-FX-Muster mit FXML und View-Modell anlegen.
* `DocumentLayout` über `bindDocument` entgegennehmen, keine eigenen Daten halten.

### 2. Blattdarstellung

* Blattfläche, Schatten und Abstand zwischen den Blättern zeichnen.
* Jede gesetzte Zeile an ihren Koordinaten zeichnen.
* Zeichenroutine hinter einer kleinen Schnittstelle kapseln.
* Dieselbe `javafx.scene.text.Font` verwenden, mit der gemessen wurde.

### 3. Zustände

* Seiten eines ausgeschalteten Teils ausgegraut darstellen.
* Harte Kante vor dem Klappentext zeichnen.
* Seitenzahl nur auf aktiven Seiten zeigen.

### 4. Bedienung

* Zoom und Breitenanpassung anbieten.
* Zu einer Seite oder einem Block scrollen.

### 5. Gestaltung

* `styles/component/paper-page.css` anlegen und in `base.css` einhängen.
* Farben aus dem Farbschema beziehen, keine festen Werte.
* Texte über das Message-Bundle führen.

### 6. Tests

* Darstellung und Scrollziel headless über TestFX prüfen.
* Ausgegraute Seiten und fehlende Seitenzahl prüfen.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Ein `DocumentLayout` wird als Folge gedruckter Seiten gezeigt.
* Nichts an der Darstellung ist bearbeitbar.
