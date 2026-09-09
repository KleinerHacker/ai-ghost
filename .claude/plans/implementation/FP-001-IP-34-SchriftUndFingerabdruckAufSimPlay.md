# IP-34: Schriftermittlung und Metrik-Fingerabdruck auf simPlay

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-34
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`
* Löst IP-22 ab.

## Abhängigkeiten

* Voraussetzung: IP-29
* Blockiert: IP-31
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `font`
* `fx-model`
* `fx-component-lifecycle`
* `testing`
* `project-docs`

## Harte Einschränkung

* Keine Schriftdatei wird gelesen, geparst oder ausgeliefert; `Font.loadFont` wird nicht verwendet.
* Der Fingerabdruck wird aus derselben Messung gebildet, die simPlay im Renderpfad verwendet.
* Referenzzeichensatz und Größe sind für alle Zeiten fest.

## Aufgaben

### 1. Messrechner klären

* Prüfen, ob `simplay-fx` einen JavaFX-`FontMeasureCalculator` exponiert.
* Falls ja: diesen im ganzen Renderpfad und für den Fingerabdruck verwenden.
* Falls nein: kleiner lokaler `FontMeasureCalculator` in `app/ui` auf einem verborgenen `javafx.scene.text.Text`-Knoten.
* Knoten einmal erstellen und wiederverwenden; Breiten nicht aufrunden.

### 2. Schrift-Stack umziehen

* `FontCatalog`, `FontResolver`, `FontResolution` aus dem entfernten `lib/ai-ghost-layouting-fx` nach `app/ui` holen.
* Paket `org.pcsoft.app.aighost.ui.font` (oder bestehende Paketkonvention von `app/ui`).
* `TODO`: bei späterer Exposition durch `simplay-fx` dorthin auslagern.

### 3. Fingerabdruck bilden

* Referenzdokument aus dem festen Zeichensatz (ASCII, Latin-1, Latin Extended-A, Kyrillisch) bei 12 pt bauen.
* Über den `FontMeasureCalculator` messen: Ascent, Descent, Leading und Wortbreiten.
* Ergebnis deterministisch serialisieren (bestehendes `FontData`-Fingerabdruck-Feld).

### 4. Vergleich und Meldung

* `FontIdentity` und `FontIdentityCheck` auf den neuen Messweg umstellen.
* Fingerabdruck beim Speichern schreiben und nur dort, wo noch keiner steht.
* Beim Öffnen vergleichen; bei Abweichung ersetzte Familie und verwendeten Ersatz melden.
* `FontTranslation`/`FontFingerprintTranslation` an simPlay-`Font`/`FontDescription` anpassen.

### 5. module-info

* `app/ui` `requires simplay.fx` (JPMS-Name aus IP-29); `ai-ghost-layouting-fx` aus `requires` entfernen.

### 6. Tests

* Entwicklertest: gleicher Font ergibt gleichen Fingerabdruck; anders messender Font ergibt Abweichung.
* Referenzmenge und Größe sind fest verdrahtet und im Test gepinnt.
* Headless über TestFX; kein Fenster.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Schriftermittlung und Fallback leben in `app/ui`.
* Der Metrik-Fingerabdruck stammt aus der simPlay-Messung, nicht aus einem zweiten Messpfad.
* Eine ersetzte oder anders messende Familie wird mit ihrem Ersatz gemeldet.
