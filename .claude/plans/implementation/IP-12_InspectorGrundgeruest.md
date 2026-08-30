# IP-12: Inspector-Grundgerüst und Inhaltsabschnitte

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-12
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: keine
* Start jederzeit möglich.
* Blockiert: IP-13, IP-15, IP-19
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-component-lifecycle`
* `testing`
* `project-docs`

## Aufgaben

### 1. Komponente

* `Inspector` nach dem MVVM-FX-Muster anlegen.
* Feste, immer gleich benannte einklappbare Abschnitte.
* Einklappzustand je Abschnitt halten.

### 2. Abschnitt Buch

* Titel und Titelzeilen aus `BookEditor` übernehmen.
* Autor und Copyright aus `Meta` ergänzen.
* Bestehendes bidirektionales Bindungsverhalten erhalten.

### 3. Abschnitt Teil

* Kapitelname sowie Inhalts- und Stil-Prompt aufnehmen.
* Einzelnen Prompt des Klappentextes gesondert führen.

### 4. Leerzustände

* Abschnitt zeigt einen Leerzustand, wenn er zur Auswahl nicht passt.

### 5. Umbau

* `BookEditor` zu Abschnitten des Inspectors zurückbauen.
* `BookEditorView.fxml` und die Bundle-Schlüssel nachziehen.

### 6. Tests

* Bindung, Abschnittswechsel und Leerzustände headless prüfen.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Prompts und Teildaten werden neben dem Blatt bearbeitet.
* Die Mitte trägt nur noch Text.
