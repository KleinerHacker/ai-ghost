# IP-32: Absatz-Operationen auf dem Dokument

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-32
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`
* Löst IP-11 ab.

## Abhängigkeiten

* Voraussetzung: IP-39 (vormals IP-31; IP-39 liefert das eine, dauerhafte `Document`)
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-component-lifecycle`
* `testing`
* `project-docs`

## Harte Einschränkung

* Blockliste, `Document` und Cursorziel ändern sich zusammen und sind eine Transaktion.
* Jede Operation erzeugt ein neues `Document` und ersetzt das alte.
* Keine Operation überschreitet die Grenze eines `TextAnchor`: ein Merge am Anfang eines Kapitels darf
  nicht in den letzten Absatz des vorigen Teils hineinlaufen, ein Löschen darf den letzten Absatz
  eines Kapitels nicht mit dem Kapitel selbst verwechseln. Anlegen und Entfernen eines ganzen Kapitels
  bleibt eine Operation des Projektbaums (IP-38/IP-39), keine Absatz-Operation dieses Plans.

## Aufgaben

### 1. Reine Listenfunktionen

* `splitParagraph`, `mergeParagraph`, `removeParagraph`, `moveParagraph` in `BookPartEditorController`.
* Arbeiten auf der `TextBlock`-Liste der betroffenen `Page` innerhalb eines `TextAnchor`.
* Ergebnis: neue `TextBlock`-Liste, daraus ein neues `Document`.
* Eine Operation an der ersten/letzten Zeile eines Ankers prüft die Ankergrenze und bricht dort ab,
  statt in den Nachbaranker hineinzulaufen.

### 2. Tastenbelegung

* Enter -> Absatz an der Caretstelle teilen (kein Zeilenumbruch im Block).
* Backspace am Blockanfang, Delete am Blockende -> mit Nachbarblock verbinden, außer an einer
  Ankergrenze.
* Strg+Umschalt+Pfeil hoch/runter -> Block umsortieren, innerhalb desselben Ankers.
* Als eigene Tastenhandler über `PaperSheetView`, da ein Block ein Absatz ist.

### 3. Navigation

* Pfeil hoch/runter an der ersten bzw. letzten umbrochenen Zeile springt in den Nachbarblock
  (ankerübergreifend erlaubt - Navigation ist keine Strukturänderung).
* Zeilengrenzen aus dem `MeasuredDocument` von simPlay lesen, nicht aus interner Zählung.

### 4. Caret-Ziel

* Nach jeder Operation Caret auf den korrekten Zielblock setzen (`CaretModel`, adressiert Blöcke/Wörter/Symbole).
* Einmalig konsumierbares Caret-Ziel mit Vorrang vor dem Caret-Memo.

### 5. Undo

* Struktureller Undo-Eintrag: Vorher-/Nachher-`Document` plus Caret-Ziel (Feinschliff in IP-33).
* Gepusht, nicht über den Tipp-Merge aufgezeichnet.

### 6. Kontextmenü

* Menüpunkte für Teilen, Verbinden, Umsortieren, Löschen.
* Texte über das Message-Bundle.

### 7. Tests

* Headless: alle vier Operationen inkl. Randfälle (erster/letzter Absatz eines Ankers).
* Eine Operation an einer Ankergrenze bricht ab, statt den Nachbaranker zu verändern.
* Je Operation genau ein Undo-Schritt.
* Zeilen-bewusste Navigation über mehrzeilige Blöcke, auch über eine Ankergrenze hinweg.
* Schnelles Tippen setzt den Caret nicht zurück.

### 8. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen (`editor.md`, CHANGELOG).

## Ergebnis

* Absätze lassen sich innerhalb eines Ankers teilen, verbinden, löschen und umsortieren.
* Jede Operation ist eine Transaktion mit genau einem Undo-Schritt.
* Kein Absatz-Vorgang verändert eine Ankergrenze oder den Kapitelbestand.
