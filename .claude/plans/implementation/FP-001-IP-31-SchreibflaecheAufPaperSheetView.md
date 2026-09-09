# IP-31: Schreibfläche auf PaperSheetView

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-31
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`
* Löst IP-10 ab; übernimmt die Palette-Überschreibung aus dem entfernten IP-27.

## Abhängigkeiten

* Voraussetzung: IP-30, IP-09, IP-34
* Blockiert: IP-32, IP-33, IP-15, IP-18
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-component-lifecycle`
* `fx-model`
* `testing`
* `project-docs`

## Harte Einschränkung

* Alles ai-ghost-Spezifische wird über die simPlay-API beantwortet, nie über einen Eingriff in simPlay.
* Eine Bearbeitung ersetzt die `Document`-Instanz; die alte bleibt unangetastet.
* Der Text eines Teils bleibt `List<String>` im Modell; das `Document` ist abgeleitet.

## Aufgaben

### 1. Einbettung

* `BookPartEditor` (`app/ui`) bettet `PaperSheetView` im Modus `EDITABLE` ein.
* `PaperFlowView`-Nutzung und `PaperFlowListener` entfernen.
* `FontMeasureCalculator` aus IP-34 an simPlay übergeben (Engine bzw. `PaperSheetView`).

### 2. Dokument hinein

* Bei Teilwechsel und jeder Designänderung über die Builder aus IP-30 ein `Document` bauen.
* `Document` an `PaperSheetView` setzen.
* Titelseite und Copyright-Seite schreibgeschützt (Modus `READONLY` oder gesperrte Blöcke).

### 3. Bearbeitung heraus

* Auf den `Document`-Tausch der Komponente hören.
* Neues `Document` auf die `List<String>`-Absätze des Teils zurückrechnen.
* Rückabbildung je `TextBlock` -> ein Absatz; `TextBlock.toString()`-Normalisierung beachten.
* Modell über die bestehende `BookPartProperty`/`ChapterProperty.of`-Bindung aktualisieren.

### 4. Cursor und Caret

* `CaretModel` von `PaperSheetView` nutzen; Cursor als Blockindex plus Offset merken.
* Cursorposition über einen Rebuild aus einem frischen `Document` wiederherstellen.
* Fokus-Block über den Rebuild merken und wiederherstellen.

### 5. Eingabe-Regeln

* Eingefügter Inhalt geht als reine Zeichenkette hinein; Rich-Text wird verworfen.
* Keine Inline-Zeichenformatierung.
* Leerer beschreibbarer Teil erhält einen leeren Absatzblock als Tippziel.

### 6. Undo-Anschluss

* Jede Textänderung als Undo-Eintrag über den `Document`-Tausch aufzeichnen (Feinschliff in IP-33).
* Merge-Schlüssel `(partId, blockIndex)` und Tipp-Pause aus `Preferences.Editor` beibehalten.

### 7. Palette

* `paper-sheet-view`-`-fx-`-Eigenschaften in `styles/component/*.css` von `app/ui` überschreiben.
* Farben aus dem Farbschema, keine festen Werte.
* Szenen-Stylesheet überschreibt die mitgelieferten simPlay-Vorgaben.

### 8. Lebenszyklus

* Registriert `PaperSheetView` etwas Globales, greift das `showingBinding()`-Muster; sonst nicht.
* Bindungen beim Projektschluss sauber lösen.

### 9. Tests

* Headless über TestFX: Tippen ändert das Modell; Modelländerung baut das `Document` neu.
* Cursor- und Fokuserhalt über einen Rebuild.
* Rich-Text-Einfügung wird auf reinen Text reduziert.
* Titelseite/Copyright-Seite bleiben schreibgeschützt.

### 10. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen (`editor.md`, CHANGELOG).

## Ergebnis

* `BookPartEditor` schreibt auf einer `PaperSheetView` im Blattstapel-Layout.
* Eine Bearbeitung erzeugt ein neues `Document` und aktualisiert die `List<String>`-Absätze.
* Die Komponente trägt das Farbschema der Anwendung über CSS.
