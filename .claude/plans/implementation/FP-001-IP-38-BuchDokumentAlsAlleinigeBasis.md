# IP-38: Buch-Dokument als alleinige Basis

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-38
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-37, IP-30, IP-34
* Blockiert: IP-39
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `project-docs`

## Harte Einschränkung

* `BookDocumentBuilder` ist die einzige Stelle, die ein `Document` von Grund auf baut - beim neuen
  Projekt, beim neuen Kapitel, bei der Migration (IP-37).
* Eine Design-Änderung baut nicht neu; sie ersetzt nur den `TextStyle` betroffener Blöcke.
* Die genaue API von `TextAnchor` wird erst mit dem Erscheinen von simPlay 0.3.1 verbindlich; dieser
  Plan wird zu Beginn anhand der dann vorliegenden Dokumentation konkretisiert (wie IP-35 es für
  simPlay 0.3.0 tat).

## Aufgaben

### 1. Anker statt Index

* Jede Seite bekommt zusätzlich zu ihrer stabilen Seiten-`id` (aus IP-35) einen `TextAnchor`.
* Statische Teile: feste Kennung (`title`, `copyright`, `prolog`, `epilog`, `blurb`).
* Kapitel: `chapter.id` (`UUID`) statt `chapter-<index>`.

### 2. Stil-Auffrischung

* Neue Funktion, die jeden Block über seinen Anker liest, seine Rolle bestimmt (Titel/Kapiteltitel/
  Kapiteltitel-Anhang/Fließtext) und seinen `TextStyle` aus `Design` ersetzt, ohne den Text
  anzufassen.
* Wird nach dem Laden eines Projekts und nach jeder Design-Änderung aufgerufen.
* Bestehende `StyleTranslation` (IP-30) wird wiederverwendet, nicht neu geschrieben.

### 3. Neues Kapitel

* Neue `UUID` erzeugen, leere `FlowPage` mit `TextAnchor` bauen, an der vom Aufrufer vorgegebenen
  Stelle in `Book.document` einfügen.
* `Book.chapters` (Name, `id`, Reihenfolge) und `Book.document` bleiben dabei synchron.

### 4. Kapitel entfernen

* Anker und zugehörige Seite(n) aus `Book.document` entfernen, Eintrag aus `Book.chapters` entfernen.
* Da hierbei - anders als bei den textbehaltenden optionalen Teilen aus IP-24 - Text unwiederbringlich
  verloren geht: Rückfrage-Verhalten wie vom Nutzer vor Beginn dieses Plans bestätigt umsetzen
  (offene Frage, Feature-Plan Abschnitt 9).

### 5. Migration anschließen

* `BookDocumentBuilder`-Aufruf aus IP-37 nutzt dieselbe Anker-Vergabe wie ein neues Projekt.

### 6. Tests

* Anker-Vergabe für jeden statischen Teil und für neu angelegte Kapitel.
* Stil-Auffrischung: eine Design-Änderung ändert den `TextStyle` bestehender Blöcke, nie den Text.
* Kapitel anlegen/entfernen hält `Book.chapters` und `Book.document` synchron.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* `BookDocumentBuilder` ist die einzige, dauerhafte Quelle für Anker und Anfangs-`Document`.
* Eine Design-Änderung wirkt auf jeden vorhandenen Block, ohne ihn neu aufzubauen.
