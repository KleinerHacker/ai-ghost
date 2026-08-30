# IP-23: Optionale Teile im Projektbaum

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-23
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: IP-15, IP-24
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-component-lifecycle`
* `testing`
* `project-docs`

## Aufgaben

### 1. Checkbox

* Checkbox auf den Knoten Prolog, Epilog und Klappentext ergänzen.
* An den Schalter aus IP-24 binden.
* Vererbung des Hakens an einen Teilbaum unterbinden.
* Übrige Knoten des Baums ohne Checkbox lassen.

### 2. Gleichlauf

* Checkbox folgt einem anderswo geänderten Schalter.
* Knoten macht kenntlich, dass ohne Haken `nicht im Buch` gilt, nicht `leer`.

### 3. Wirkung auf das Blatt

* Ausgrauen, Seitennummerierung und Seitenzahl unmittelbar nachziehen.
* Kein erneutes Öffnen des Projekts nötig.
* Keine Rückfrage stellen, es geht kein Text verloren.

### 4. Undo

* Jede Umschaltung als einen Undo-Eintrag aufzeichnen.

### 5. Gestaltung

* `styles/component/tree-view.css` um die Checkbox ergänzen.
* Texte über das Message-Bundle führen.

### 6. Tests

* Umschalten, Gleichlauf, Ausgrauen und Umnummerierung headless prüfen.
* Erhalt des Textes über eine Umschaltung prüfen.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Prolog, Epilog und Klappentext werden vom Baum aus in das Buch geschaltet.
* Kein Text geht dabei verloren.
