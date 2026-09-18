# IP-05: Datei-Menü: Neu

## Herkunft

* Feature Plan: `.claude/plans/features/FP-003-PaneliumChromeMenu.md`
* Plan-ID im Feature Plan: IP-05
* Status-Datei des Features: `.claude/plans/features/FP-003-PaneliumChromeMenu-status.md`

## Abhängigkeiten

* Voraussetzung: IP-02
* Blockiert: keine
* Reihenfolge und Graph stehen in Abschnitt 7 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-model`
* `translation`
* `testing`
* `project-docs`

## Aufgaben

### 1. Profile konkretisieren

* Anzahl und Inhalt der vordefinierten Profile klären.
* Je Profil die vorausgewählten `BookPart`-Typen (Prolog, Kapitel, Epilog, ...) festlegen.

### 2. Model

* `model-explore`-Agenten für das Muster in `lib/model`/`lib/fx-model` ausführen.
* `model-creator`-Agenten für das neue Profil-Model samt FX-Pendant beauftragen.

### 3. Backstage-Eintrag

* `FXBackstageMenuItem` "Neu" mit eigenem `content`-Node in der Backstage aus IP-02 anlegen.
* Profile im `content`-Bereich als auswählbare Liste darstellen.

### 4. Verdrahtung

* Auswahl eines Profils legt ein neues Projekt mit dessen vorausgewählten Buchteilen an.

### 5. Übersetzung

* Neue Message-Bundle-Keys über den `translator`-Agenten übersetzen lassen.

### 6. Tests

* Je Profil prüfen, dass das neue Projekt die richtigen Buchteile enthält.

### 7. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-003-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Der Backstage-Eintrag "Neu" zeigt auswählbare, vordefinierte Profile.
* Eine Auswahl legt ein neues Projekt gemäß dem gewählten Profil an.
