# IP-36 + IP-37 + IP-38: Anker-Struktur, Dokument-Persistenz, Buch-Dokument als Basis

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Zusammengeführte Plan-IDs: IP-36, IP-37, IP-38
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`
* Ersetzt die vormals drei Einzelpläne unter `.claude/plans/implementation/` als eine
  durchgängige Umsetzung
* Gegen simPlay-Quellcode (`D:\Workspace\Application_Workspaces\Frameworks\simPlay`, Stand
  0.3.1) geprüft

## Abhängigkeiten

* Voraussetzung gesamt: IP-24, IP-02, IP-29, IP-30, IP-34
* Blockiert nach Abschluss: IP-39
* Interne Reihenfolge zwingend: erst Teil A, dann Teil B, dann Teil C
* simPlay 0.3.1 liegt bereits im Framework-Repo vor (`TextAnchor` existiert, siehe unten) - keine
  Wartezeit mehr nötig

## Zu ladende Skills

* `fx-model`
* `testing`
* `project-docs`

## Erkenntnisse aus simPlay (0.3.1) - bindend für Teil B und Teil C

* `Document`, `Page` (`FlowPage`/`SinglePage`), `TextPart`, `TextStyle` sind ausschließlich
  `kotlinx.serialization`-`@Serializable`; es gibt KEINE Jackson-Kompatibilität und keine
  vorgefertigte String/ByteArray-Brücke - diese Brücke muss ai-ghost selbst bauen
* `TextAnchor` existiert bereits in `engine/.../model/TextPart.kt` als `TextPart`-Variante mit
  einzigem Feld `id: String`, eingebettet als `${id}`-Token IM TEXT eines `TextBlock`
  (nicht als Feld von `Page`/`FlowPage`/`SinglePage`)
* Anker sind zero-width, werden nicht gerendert und nicht mitgezählt; Auflösung erfolgt über
  `DocumentTextIndex` (`anchorsById`, `startOfAnchor`/`endOfAnchor`) bzw. `CaretModel.moveToAnchor`
* `id` muss dokumentweit eindeutig sein - Kollisionsbehandlung liegt beim Aufrufer (ai-ghost)
* Eine `StyleTranslation`-Klasse existiert NICHT in simPlay - sie ist eine ai-ghost-eigene Klasse
  aus IP-30 und wird als solche wiederverwendet, nicht aus simPlay bezogen

## Harte Einschränkungen

* Kein Buchteil trägt am Ende noch Fließtext im Modell; Text lebt nur im simPlay-`Document`
* `Chapter.id` wird einmal vergeben und danach nie geändert
* Ein vor der Umstellung gespeichertes Projekt öffnet mit unverändertem Text
* Migration läuft genau einmal beim ersten Öffnen eines Altprojekts
* `BookDocumentBuilder` ist einzige Stelle, die ein `Document` von Grund auf baut
* Eine Design-Änderung baut nicht neu, sie ersetzt nur `TextStyle` betroffener Blöcke
* Anker-Zuordnung nutzt simPlays `TextAnchor`-Token im Text, nicht ein eigenes Seitenfeld

## Teil A: Modell-Umstellung auf Anker-Struktur (vormals IP-36)

* `title`, `titleAppendix`, `paragraph` aus `BookPart`-Interface entfernen, `prompts` bleibt
* `Chapter`: gleiche Felder entfernen, neues `id: UUID` bei Erstanlage vergeben, unveränderlich
* `Prolog`/`Epilog`: Text entfernen über `BookPart`, `included`/`prompts` bleiben
* `Book.title`/`Book.titleAppendix` entfernen, `Copyright` von Fließtext befreien
* FX-Modell-Gegenstücke verschlanken, `chapter.idProperty` ergänzen, Mapper-Tests nachziehen
* Entfernte Felder als reines Lese-Modell für Migration erreichbar halten (Vorbereitung Teil B)
* Tests: Feldentfernung, `Chapter.id`-Stabilität, FX-Mapper/Property-Tests je `fx-model`-Skill
* Build über Agent ausführen, KDoc/README/CHANGELOG nach `project-docs` prüfen

## Teil B: Dokument-Persistenz ohne Migration (vormals IP-37, Umfang per Nutzerentscheidung reduziert)

* ABWEICHUNG: Nutzer hat Migration alter Projekte ausdrücklich abgelehnt - kein Legacy-Lesen,
  kein `BookDocumentBuilder`-Fallback, kein Sonderfall in `ProjectStorage`/`StorageIo` in Teil B
* Laden eines Altprojekts ohne `document`-Feld bleibt unbehandelt und ist NICHT Teil von Teil B
* Migration wird als offener Punkt an Teil C weitergereicht
* Bestätigt: neue Dependencies in `lib/model` erlaubt (kotlinx-serialization-Plugin,
  kotlinx-serialization-json, `simplay-engine-jvm` als `api`)
* Bestätigt: Kodierformat JSON via kotlinx-serialization
* Bestätigt: `Book.document` nicht-nullable, Default `Document()`
* [x] Root-Build: `simplayVersion` auf 0.3.1 heben, Kotlin-Serialization-Plugin ergänzt
* [x] `lib/model/build.gradle.kts`: Serialization-Plugin, `kotlinx-serialization-json`,
  `simplay-engine-jvm` (`api`) ergänzt
* [x] `DocumentCodec.kt` (neu, `lib/model/.../project/book/`): encode/decode `Document`<->`String`
  über ein wiederverwendetes `Json`-Konfigurationsobjekt
* [x] `Book.kt`: `documentPayload: String` (Jackson-sichtbar) + berechnete `document`-Property
  (`@JsonIgnore`, get/set über `DocumentCodec`), `VERSION` auf 2, KDoc zur Nullable-Entscheidung
* [x] `BookProperty.kt`: `documentProperty` als `reference` registriert (kein eigenes FX-Modell
  für `Document`), Registrierung nach `blurb`
* [x] Kein eigener Jackson-Serializer für `Document` selbst - Jackson sieht nur `documentPayload`
* [x] Tests: `DocumentCodecTest`, `BookTest` (Getter/Setter-Rundreise), `ProjectStorageTest`
  (Rundreise neues Format, korruptes `documentPayload` -> `Error.Corrupt`, wie jeder andere
  unlesbare Standardteil), `BookPropertyTest` (`documentProperty` nach `fx-model`-Skill-Checkliste)
* [x] Build über Agent ausgeführt (mehrere Iterationen, siehe "Was wirklich gebaut wurde" unten),
  BUILD SUCCESSFUL
* [x] CHANGELOG unverändert gelassen - `document` ist für den Endnutzer noch nicht sichtbar
  (keine UI-Anbindung in Teil B)

### Was wirklich gebaut wurde, abweichend vom obigen Plantext

* Korruptes `documentPayload` des Standardteils `book.json` führt zu `ProjectStorage.Error.Corrupt`
  (fehlender Standardteil), NICHT zu `Error.Malformed` - `StorageIo.loadFromZip` fängt jeden
  Parse-Fehler eines Standardteils bereits selbst ab und behandelt ihn wie einen fehlenden Teil;
  `Error.Malformed` bleibt in der aktuellen Architektur unerreichbar und wurde nicht künstlich
  erschlossen
* Nebenbei behoben, da build-blockierend und aus vorheriger, unabhängiger Arbeit stammend: in
  `InspectorView.fxml` fehlte der `<?import ... AiTextField?>`, wodurch `MainWindowIT` in app/ui
  scheiterte - Ein-Zeilen-Fix, kein Bezug zu Teil B selbst

## Teil C: Buch-Dokument als alleinige Basis (vormals IP-38)

* Rollen-Anker als `${id}`-Token an den Anfang des jeweiligen `TextBlock`-Texts setzen
  (simPlay-`TextAnchor`, kein eigenes Seitenfeld)
* Statische Teile mit fester, eindeutiger `id` (`title`, `copyright`, `prolog`, `epilog`, `blurb`)
* Kapitel referenzieren über `chapter.id.toString()` als Anker-`id` statt `chapter-<index>`
* Eindeutigkeit der Anker-`id` dokumentweit sicherstellen (ai-ghost-seitige Prüfung, simPlay
  prüft das nicht)
* Neue Stil-Auffrischungs-Funktion: über `DocumentTextIndex`/Anker-`id` den zugehörigen
  `TextBlock` finden, Rolle bestimmen, `TextStyle` ersetzen, Text unverändert lassen
* Aufruf nach Laden und nach jeder Design-Änderung, bestehende ai-ghost-`StyleTranslation`
  (IP-30) wiederverwenden
* Neues Kapitel: `UUID` erzeugen, leere `FlowPage` mit Anker-Token im ersten `TextBlock` bauen,
  einfügen, `chapters`/`document` synchron halten
* Kapitel entfernen: Anker-Token und zugehörige Seite(n) löschen, Rückfrage-Verhalten laut
  Feature-Plan-Abschnitt 9 umsetzen
* Migration aus Teil B nutzt dieselbe Anker-Vergabe wie ein neues Projekt
* Tests: Anker-Vergabe/Eindeutigkeit, Stil-Auffrischung ändert nur `TextStyle`, Kapitel
  anlegen/entfernen bleibt synchron

## Abschluss gesamt

* Build über Agent nach jedem Teil ausführen (nicht erst am Ende)
* Dokumentation nach `project-docs` nach jedem Teil prüfen
* Nach Abschluss aller drei Teile: Feature-Plan-Tabelle, Statusdatei und Abhängigkeitsgraph für
  IP-36/37/38 als erledigt markieren
* Diese Plan-Datei per `git rm` entfernen, sobald alle drei Teile fertig sind

## Ergebnis

* Kein Modell-POJO trägt mehr Fließtext, jedes Kapitel trägt eine stabile `UUID`
* `Book.document` ist der gespeicherte Fließtext (kotlinx-serialisiert, in Jackson-ZIP
  eingebettet), Altprojekte öffnen unverändert und erhalten ein `document`
* `BookDocumentBuilder` ist einzige dauerhafte Quelle für Anker-Token und Anfangs-`Document`
* Design-Änderungen wirken auf bestehende Blöcke, ohne sie neu aufzubauen
