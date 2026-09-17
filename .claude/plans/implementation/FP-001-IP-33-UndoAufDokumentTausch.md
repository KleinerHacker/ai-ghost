# IP-33: Undo auf dem unveränderlichen Dokument-Tausch

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-33
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-39 ✅
* Blockiert: keinen weiteren Plan

## Zu ladende Skills

* `fx-component-lifecycle`
* `testing`
* `project-docs`

## Bestandsaufnahme (Code bereits geprüft)

* Struktureller Undo-Eintrag existiert bereits: `DocumentStructureUndoEntry` (IP-32),
  Vorher-/Nachher-`Document` plus `PendingCaretTarget`, über `UndoStack.push(...)`.
* Text-Undo läuft bereits über `undoStack.record(...)` auf der `StringProperty` je `PartTarget`,
  Merge-Schlüssel `page.id to target`, Tipp-Pause aus `UndoStack.mergeTimeoutMillis`.
* `handleDocumentChanged`/`propertyFor` lösen nach jeder Wiedergabe `pushWholeDocument()` aus, das
  `Document` wird also bei jedem Undo/Redo neu aus dem Modell abgeleitet.
* `UndoStack.clear()` ist bereits in `newProject()`/`openProject()` verdrahtet.
* Nicht abgedeckt: kein Redo-Test, kein Test für den zusammengefassten Undo-Schritt über mehrere
  Tastenanschläge, keine Undo/Redo-Tests für Split/Merge/Move auf Editor-Ebene (nur der isolierte
  `DocumentStructureUndoEntryTest`).

## Aufgaben

### 1. Text-Undo bestätigen, nicht neu bauen

* Keine neue `UndoEntry`-Variante für Tippen erstellen, bestehenden `record(...)`-Weg beibehalten.
* Merge-Schlüssel `(page.id, target)` und Tipp-Pause aus `Preferences.Editor` als ausreichend werten.
* Caret-Wiederherstellung nach Undo/Redo einer Textänderung im Test nachweisen.

### 2. Struktur-Undo bestätigen

* `DocumentStructureUndoEntry` und `applyStructuralChange` unverändert übernehmen.
* Ableitung von `Book.chapters`-Reihenfolge aus den Ankern des gesetzten `Document` per Test prüfen.

### 3. Tests ergänzen (`BookPartEditorTest`, ggf. neue Testklasse)

* Redo-Fall einer Textänderung ergänzen (Text und Caret wiederhergestellt).
* Test: mehrere Tastenanschläge in Folge vor der Tipp-Pause ergeben einen Undo-Schritt.
* Test: Split erzeugt einen Undo-Schritt, Undo/Redo stellt Text, Struktur und Caret wieder her.
* Test: Merge/Move ebenso je ein Undo-Schritt mit korrekter Wiedergabe.
* Test: Projektwechsel leert die Historie (`canUndoProperty`/`canRedoProperty` danach `false`).

### 4. Abschluss

* Build über Agent ausführen (`:app:ai-ghost-ui:build`).
* Dokumentation nach `project-docs` prüfen (KDoc/CHANGELOG, falls Verhalten sich sichtbar ändert).
* Feature-Status-Datei: IP-33 auf `COMPLETED` setzen, Fortschritt/Anmerkungen ergänzen.
* Feature-Plan: IP-33-Zeile und Abschnittsüberschrift mit ✅ versehen.
* Plandatei `.claude/plans/implementation/FP-001-IP-33-UndoAufDokumentTausch.md` per `git rm` entfernen.

## Ergebnis

* Jede Text- und Strukturänderung bleibt über den bestehenden `Document`-Tausch-Mechanismus
  undo- und redo-fähig, jetzt mit vollständiger Testabdeckung.
* Keine Änderung an der Undo-Infrastruktur aus IP-09.
