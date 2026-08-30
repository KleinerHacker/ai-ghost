# IP-27: Styling- und Theming-API der Bibliothek

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-27
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-07, IP-08
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-28
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `testing`
* `icons`
* `project-docs`

## Aufgaben

### 1. Stilklassen

* Stilklassen für Blatt, Schatten, Lücke, Umbruchmarke und Hintergrund vergeben.
* Pseudoklassen für ausgegraute und abgesetzte Seite vergeben.
* Stilklasse für die Seitenzahl vergeben.

### 2. Standard-Stylesheet

* Neutrales Stylesheet in den Ressourcen der Bibliothek anlegen.
* Keine Farbe der Anwendung darin nennen.
* Stylesheet von den Knoten selbst laden lassen.

### 3. Styleable Properties

* Werte des Rahmens als styleable Properties anbieten.
* Textdarstellung ausdrücklich nicht styleable machen.

### 4. Grenze dokumentieren

* In der KDoc festhalten, dass CSS nur den Rahmen ändert.
* Begründung nennen: Schriftgröße aus dem Layoutergebnis bleibt unantastbar.

### 5. Anwendungsseite

* Stilklassen in `app/ui` unter `styles/component` überschreiben.
* Farben aus dem Farbschema beziehen, keine festen Werte.
* Texte einer Marke über das Message-Bundle führen.

### 6. Tests

* Wirksamkeit einer Überschreibung headless prüfen.
* Prüfen, dass eine Schriftgröße über CSS nicht änderbar ist.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Die Bibliothek sieht ohne Anwendung vollständig aus.
* In ai-ghost trägt sie das Farbschema der Anwendung.
* Die Textdarstellung bleibt vom Layoutergebnis bestimmt.
