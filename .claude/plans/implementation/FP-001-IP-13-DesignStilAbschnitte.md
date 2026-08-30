# IP-13: Design-Stilabschnitte

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-13
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-02, IP-12, IP-26
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `testing`
* `font`
* `project-docs`

## Aufgaben

### 1. Abschnitte

* Stilabschnitte für Titel, Kapiteltitel, Kapiteltitelzeile und Fließtext.
* Familie, Grad, Fett, Kursiv, Ausrichtung und Zeilenabstand je Abschnitt.

### 2. Familienauswahl

* Nur Familien aus dem Katalog der Bibliothek anbieten.
* Beispieltext je Familie in deren Schnitt zeigen.
* Beispiel je sichtbarer Zeile erst bei Bedarf erzeugen.
* Nicht installierte Familie eines Projekts kennzeichnen.

### 3. Wirkung

* Auf dieselbe `DesignProperty` schreiben, die das Layout liest.
* Blatt folgt der Änderung ohne erneutes Öffnen des Projekts.

### 4. Gestaltung

* Texte über das Message-Bundle führen.
* Stile in `styles/component` ergänzen.

### 5. Tests

* Bindung, Familienliste und Kennzeichnung headless prüfen.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Eine geänderte Stilangabe verändert das Blatt sofort.
* Nur installierte Familien stehen zur Auswahl.
