# IP-34: Schriftermittlung und Metrik-Fingerabdruck auf simPlay

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-34
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`
* Löst IP-22 ab.
* Neu gefasst nach simPlay 0.2.1 (`FxFontProbe`/`FontFingerprint` in `engine`/`ui/fx`).

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
* Fingerabdruck und Messung laufen ausschließlich über `simplay-fx`s `FxFontProbe`; kein eigener `FontMeasureCalculator` in `app/ui`.
* Referenzzeichensatz und Normgröße sind `FontFingerprint.REFERENCE_GLYPHS`/`NORMALIZED_SIZE` aus simPlay, nicht mehr eigens definiert.
* `FxFontProbe`s Referenzsatz trägt kein Kyrillisch; Bedarf bei Ghost Writer wird geprüft (Feature Plan, Abschnitt 9).

## Aufgaben

### 1. Verfügbarkeit und Fingerabdruck verdrahten

* `FxFontProbe` (`org.pcsoft.framework.simplay.fx`) in `app/ui` instanziieren, einmal wiederverwendet.
* `checkAvailability(font)` für `FontAvailability` `AVAILABLE`/`SUBSTITUTED`/`MISSING` nutzen.
* `stamp(document)` beim Speichern aufrufen; nur wo noch kein Fingerabdruck steht (`overwrite = false`).
* `verify(font, expected)` bzw. `MeasuredDocument.fingerprintDeviations` beim Öffnen für den Vergleich nutzen.
* Kein eigener `FontMeasureCalculator`, kein eigenes Referenzdokument, keine eigene Serialisierung mehr bauen.

### 2. Restlücke schließen: Ersatzfamilie benennen

* `checkAvailability` liefert nur das Enum, keinen Namen der Ersatzfamilie.
* Kleinen Wrapper in `app/ui` bauen, der bei `SUBSTITUTED`/`MISSING` die tatsächlich aufgelöste `javafx.scene.text.Font`-Familie zusätzlich ermittelt.
* Ermittelten Namen zusammen mit dem `FontAvailability`-Ergebnis melden.
* Kein eigener `FontResolver`/`FontResolution`-Stack; nur diese eine Ergänzung zu `FxFontProbe`.

### 3. Familienliste für die Schriftauswahl

* `javafx.scene.text.Font.getFamilies()` direkt in der Design-Sektion des Inspectors nutzen.
* Kein `FontCatalog` mehr aus `lib/ai-ghost-layouting-fx` übernehmen.

### 4. FontIdentity/FontIdentityCheck neu fassen

* `FontIdentity`/`FontIdentityCheck` in `app/ui` als dünne Hülle um `FxFontProbe` plus Aufgabe 2 bauen.
* `FontData`-Fingerabdruck-Feld speichert `FontFingerprint.encode()`; beim Laden mit `FontFingerprint.decode(text)` zurückwandeln.
* `FontTranslation` setzt das dekodierte `FontFingerprint` auf `Font.fingerprint` beim Bau des simPlay-`Document`.
* `FontFingerprintTranslation` entfällt als eigener Typ; simPlays `Font.fingerprint` trägt es direkt.

### 5. module-info

* `app/ui` `requires simplay.fx` (JPMS-Name aus IP-29); `ai-ghost-layouting-fx` aus `requires` entfernen.

### 6. Tests

* Entwicklertest: `FxFontProbe.checkAvailability`/`fingerprint`/`verify` mit bekannter und mit fehlender Familie.
* Entwicklertest: `FontData`-Fingerabdruck-Feld übersteht Encode/Decode-Zyklus.
* Entwicklertest: Ersatzfamilie-Wrapper aus Aufgabe 2 meldet den korrekten Familiennamen.
* Headless über TestFX; kein Fenster.

### 7. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Schriftverfügbarkeit und Metrik-Fingerabdruck laufen vollständig über `simplay-fx`s `FxFontProbe`.
* `app/ui` ergänzt nur, was `FxFontProbe` nicht liefert: den Namen der Ersatzfamilie und die Familienliste für die Auswahl.
* Eine ersetzte oder anders messende Familie wird mit ihrem Ersatz gemeldet.
