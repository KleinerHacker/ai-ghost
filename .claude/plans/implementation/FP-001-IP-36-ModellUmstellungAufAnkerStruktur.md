# IP-36: Modell-Umstellung auf Anker-Struktur

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-36
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`
* Erster Plan der zweiten großen Planabweichung (TextAnchor, Abschnitt 6/7 des Feature Plans).

## Abhängigkeiten

* Voraussetzung: IP-24, IP-02
* Blockiert: IP-37
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-model`
* `testing`
* `project-docs`

## Harte Einschränkung

* Kein Buchteil trägt nach diesem Plan noch seinen Fließtext im Modell; der Fließtext lebt
  ausschließlich im simPlay-`Document` (IP-37/IP-38).
* Die neue `Chapter.id` wird bei der Anlage eines Kapitels einmal vergeben und danach nie geändert.

## Aufgaben

### 1. `BookPart`-Interface

* `title`, `titleAppendix`, `paragraph` aus `BookPart` entfernen.
* `prompts` bleibt einziges gemeinsames Feld.
* KDoc auf die neue Bedeutung ("ein Teil, der Prompts trägt, aber seinen Text nicht mehr selbst")
  anpassen.

### 2. `Chapter`

* `title`, `titleAppendix`, `paragraph` entfernen.
* Neues Feld `id: UUID` ergänzen, bei Erstanlage vergeben (`UUID.randomUUID()`), danach unveränderlich.
* `name` und `prompts` bleiben.

### 3. `Prolog`, `Epilog`

* `title`, `titleAppendix`, `paragraph` entfernen (über das geänderte `BookPart`-Interface).
* `included`, `prompts` bleiben unverändert (IP-24).

### 4. `Book`, `Copyright`

* `Book.title`, `Book.titleAppendix` entfernen.
* `Copyright` von seinem Text befreien; was kein Fließtext ist (falls vorhanden), bleibt.

### 5. FX-Modell

* `lib/fx-model`-Gegenstücke (`ChapterProperty`, `PrologProperty`, `EpilogProperty`, `BookProperty`,
  `CopyrightProperty`) entsprechend verschlanken; `chapter.idProperty` ergänzen.
* Mapper- und Property-Tests nach `fx-model`-Skill nachziehen.

### 6. Vorbereitung für IP-37

* Die entfernten Felder bleiben als reines Lese-Modell für die Migration erreichbar (eigene,
  produktiv ungenutzte DTO-Klasse oder Versions-Gate in der bestehenden Jackson-Deserialisierung) -
  IP-37 entscheidet die konkrete Form, dieser Plan hält die Werte nur lesbar.

### 7. Tests

* Für jede geänderte Modellklasse: Feldentfernung, neue `Chapter.id`, Stabilität der `id` über
  Kopieren/Ändern anderer Felder.
* FX-Modell-Mapper- und Property-Tests je `fx-model`-Skill.

### 8. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen (KDoc der geänderten Klassen, README/CHANGELOG).

## Ergebnis

* Kein Modell-POJO trägt mehr Fließtext; jedes Kapitel trägt eine stabile `UUID`.
* Das Modell ist bereit, den Fließtext an ein simPlay-`Document` abzugeben (IP-37).
