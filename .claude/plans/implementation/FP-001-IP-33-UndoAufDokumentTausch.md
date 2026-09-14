# IP-33: Undo auf dem unveränderlichen Dokument-Tausch

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-33
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`
* Stellt die Editor-Undo-Einträge aus IP-09/IP-10 auf simPlay um.

## Abhängigkeiten

* Voraussetzung: IP-39 (vormals IP-31; IP-39 liefert das eine, dauerhafte `Document`)
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-component-lifecycle`
* `testing`
* `project-docs`

## Harte Einschränkung

* Die Undo-Infrastruktur aus IP-09 (`UndoStack`, Tooltip, Verlaufs-Dropdown) bleibt unverändert.
* Undo tauscht die `Document`-Instanz zurück; das Modell folgt daraus.

## Aufgaben

### 1. Textänderungs-Eintrag

* `UndoEntry`-Variante für den `Document`-Tausch bei Tippen.
* Merkt Vorher-/Nachher-`Document` und die `List<String>`-Absätze bzw. das Caret.
* Merge-Schlüssel `(partId, blockIndex)` und Tipp-Pause aus `Preferences.Editor` beibehalten.

### 2. Struktureller Eintrag

* Ersatz für `ParagraphListUndoEntry`: Vorher-/Nachher-`Document` plus Caret-Ziel.
* Über `UndoStack.push(...)`, nicht `record(...)`.

### 3. Wiedergabe

* Undo/Redo setzt das gemerkte `Document` auf `PaperSheetView`.
* `Book.chapters`s Reihenfolge/Bestand aus den Ankern des gesetzten `Document` neu ableiten (kein
  `List<String>`-Modell mehr, seit der TextAnchor-Abweichung, IP-36/IP-38).
* Caret- und Fokus-Block aus dem Eintrag wiederherstellen.

### 4. Aufräumen

* Bei `newProject()`/`openProject()` wird der Stack geleert (bestehendes Verhalten).
* Keine `Document`-Referenz überlebt einen Projektwechsel.

### 5. Tests

* Headless: Tippen, Undo, Redo stellen Text und Caret her.
* Absatz teilen/verbinden/umsortieren -> je ein Undo-Schritt, korrekt zurückgespielt.
* Mehrere Tastenanschläge in Folge -> ein zusammengefasster Undo-Schritt bis zur Tipp-Pause.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Jede Text- und Strukturänderung ist über den `Document`-Tausch undo- und redo-fähig.
* Die Undo-Infrastruktur aus IP-09 bleibt unangetastet.
