# IP-14: Projekteinstellungen-Dialog

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-14
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-02
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `dialog`
* `ui-styling`
* `testing`
* `icons`
* `project-docs`

## Aufgaben

### 1. Dialog

* Dialog nach dem Muster von `AiGhostDialog` und `DetailDialog` anlegen.
* Auf dem bestehenden Icon `project-settings@32.png` öffnen.

### 2. Inhalte

* Seitenformat mit Presets A5, A4, 12,5x19, 13,5x21,5 und 6x9 Zoll.
* Vier Ränder in Millimetern.
* Leerseite am Anfang und am Ende.

### 3. Umrechnung und Prüfung

* Millimeter anzeigen, Punkt speichern, Umrechnung nur im Dialog.
* Randsumme größer als die Seite ablehnen.
* Fehlerhafte Eingabe am Feld melden.

### 4. Gestaltung

* Texte über das Message-Bundle führen.
* Stile in `styles/component/dialog.css` ergänzen.

### 5. Tests

* Presets, Umrechnung und Ablehnung unmöglicher Geometrie prüfen.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Die Seitengeometrie eines Projekts wird an einer Stelle gesetzt.
* Das Blatt übernimmt die neue Geometrie.
