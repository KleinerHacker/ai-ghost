# IP-37: Dokument-Persistenz und Migration

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-37
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-36, IP-29
* Blockiert: IP-38
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-model`
* `testing`
* `project-docs`

## Harte Einschränkung

* Ein vor dieser Abweichung gespeichertes Projekt öffnet mit genau dem Text, den es vorher hatte.
* Die Migration läuft genau einmal beim ersten Öffnen eines Altprojekts, nicht bei jedem Start.

## Aufgaben

### 1. Neues Feld

* `Book.document: Document` ergänzen (nullable oder mit leerem `Document` als Vorgabe - Entscheidung
  hier treffen und in der KDoc begründen).

### 2. Jackson-Verträglichkeit

* Prüfen, ob `Document`/`Page` (`FlowPage`/`SinglePage`)/`TextBlock`/`TextStyle`/`TextAnchor` mit
  Jacksons Standardmechanismus (de-)serialisieren, oder ob sie kotlinx-serialization voraussetzen.
* Je nach Befund: Jackson-Kotlin-Modul mit `@JsonTypeInfo`-Ergänzung, eigene
  `JsonSerializer`/`JsonDeserializer`, oder das `Document` über simPlays eigene Serialisierung in
  einen String/ByteArray wandeln und diesen als ein Jackson-Feld führen.
* Ergebnis und Begründung in der Statusdatei festhalten, da diese Entscheidung Folgeverträge (IP-38)
  betrifft.

### 3. Migration

* Ein Projekt ohne `document` (erkennbar an `Book.version` oder am fehlenden Feld) wird beim Laden aus
  seinen bisherigen `List<String>`-Feldern (Lese-Modell aus IP-36, Aufgabe 6) über `BookDocumentBuilder`
  einmalig zu einem `Document` gebaut.
* Nach der Migration werden nur noch `document` und die in IP-36 verbliebenen Felder
  (`Chapter.id`/`name`/`prompts` usw.) weitergeschrieben; die alten Textfelder werden nicht mehr
  gepflegt.
* `Book.version` wird erhöht, sobald diese Umstellung greift.

### 4. Rundreise-Erhalt

* Speichern und erneutes Laden eines migrierten Projekts liefert dasselbe `document` (bis auf die vom
  Laden immer neu berechneten `TextStyle`s, siehe IP-38).

### 5. Tests

* Migration eines Beispielprojekts im alten Format: Text, Kapitelreihenfolge und Namen bleiben
  erhalten.
* Rundreise eines neuen, bereits `document`-basierten Projekts.
* Fehlerfall: ein Projekt, dessen alte Felder ebenfalls fehlen (korrupt), bleibt wie bisher
  `Error.Corrupt`/`Error.Incomplete`.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen (README/CHANGELOG, Persistenzformat-Beschreibung falls
  vorhanden).

## Ergebnis

* `Book.document` ist der gespeicherte Fließtext, lesbar und schreibbar über die bestehende
  ZIP-Persistenz.
* Ein vor der Abweichung gespeichertes Projekt öffnet unverändert und trägt danach ein `document`.
