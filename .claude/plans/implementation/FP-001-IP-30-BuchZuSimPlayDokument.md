# IP-30: Buch zu simPlay-Dokument-Builder

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-30
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`
* Neu gefasst nach simPlay 0.2.1 (Quelltext Tag `0.2.1`).

## Abhängigkeiten

* Voraussetzung: IP-29 ✅, IP-02 ✅, IP-24 ✅
* Blockiert: IP-31, IP-16

## Zu ladende Skills

* `fx-model`
* `testing`
* `project-docs`

## Harte Einschränkung

* `lib/ai-ghost-layouting-model` hängt nur an `ai-ghost-model` und `simplay-engine`, an keinem Toolkit.
* Das simPlay-`Document` ist eine abgeleitete Sicht, kein gespeicherter Zustand.
* Fehlende ai-ghost-Seitenpolitik wird als `TODO(simPlay page policy)` markiert, nicht in ai-ghost nachgebaut.
* Keine neue Drittanbieter-Abhängigkeit außer `simplay-engine`; fehlende Lizenz dem Nutzer vorlegen.

## simPlay 0.2.1 API (aus Quelltext Tag 0.2.1)

* `org.pcsoft.framework.simplay.engine.model`: `Document(pages)`, `Page` (sealed: `layout`, `blocks`),
  `FlowPage(layout, blocks)`, `SinglePage(layout, blocks)`, `PageLayout(size, margins)`,
  `TextBlock.of(text, style)` / `.withStyle(style)`, `toString()` fügt die Teile wieder zusammen,
  `TextStyle(font, lineSpacing = LineSpacing(), alignment = LEFT)`,
  `LineSpacing(factor = 1.0, extraLeading = 0.0)`,
  `Font(family, size, weight = NORMAL, style = NORMAL, fingerprint = null)`,
  `FontWeight { NORMAL, BOLD }`, `FontStyle { NORMAL, ITALIC }`,
  `TextAlignment { LEFT, RIGHT, CENTER, JUSTIFY }`
* `org.pcsoft.framework.simplay.engine.geometry`: `Size(width, height)`, `Margins(left, top, right, bottom)`
* `SimpLayEngine` / `Document.measure(...)` wird von IP-30 NICHT benutzt (Messung ist IP-31).

## Aufgaben

### 1. Modul-Build und module-info

* [x] `lib/layouting-model/build.gradle.kts`: `api("org.pcsoft.framework:simplay-engine:$simplayVersion")`
  (Version aus `rootProject.extra`), `api(project(":lib:ai-ghost-model"))` behalten.
* [ ] JPMS-Namen von `simplay-engine` am aufgelösten Jar prüfen (`jar --describe-module`) — angenommen
  `org.pcsoft.framework.simplay.engine`, im `module-info.java` als `TODO(IP-30 verify)` markiert.
* [x] `module-info.java`: `requires transitive org.pcsoft.app.aighost.layouting` durch den
  simPlay-Engine-Modulnamen ersetzt; neues Paket `...layouting.model.project` exportiert.
* [ ] `requires kotlinx.serialization.core` — als `TODO(IP-30 verify)` im `module-info.java`
  vermerkt; erst beim ersten Build mit Token bestätigen.
* [ ] Lizenzbericht über Agent (braucht Token); fehlende Einträge dem Nutzer vorlegen.

### 2. StyleTranslation umgestellt

* [x] `common/StyleTranslation.kt`: `StyleData.toTextStyle()` ohne `spaceBefore`/`spaceAfter`.
* [x] `Font(family = font.name, size = font.size.toDouble(), weight = bold?BOLD:NORMAL, style = italic?ITALIC:NORMAL, fingerprint = null)`.
* [x] `LineSpacing(factor = textLineSpacing)`; `Alignment.toTextAlignment()` beibehalten (`BLOCK -> JUSTIFY`).
* [x] `BlockSpacing`-Objekt entfernt; frühere Punktwerte als `TODO(simPlay page policy)` im KDoc.

### 3. PageLayout-Übersetzung

* [x] `common/PageGeometryTranslation.kt` per `git mv` zu `PageLayoutTranslation.kt`.
* [x] `fun PageFormat.toPageLayout(): PageLayout` mit `Size(width, height)`,
  `Margins(left = innerMargin, top = topMargin, right = outerMargin, bottom = bottomMargin)`.
* [x] `TODO(simPlay page policy)`: `mirroredMargins` nicht abbildbar, jede Seite gleiche `Margins`.

### 4. Die vier Block-Builder auf simPlay-Typen

* [x] `BookPartBuilder`, `TitlePageBuilder`, `BlurbBuilder`, `CopyrightPageBuilder`:
  `TextBlock.of(text, style)`, `toTextStyle()` ohne Argumente, `BlockSpacing`-Nutzung entfernt.
* [x] Inklusions- und Leerzeilen-Logik unverändert (leerer Absatz bleibt Block, leere Überschrift entfällt,
  `CopyrightPageBuilder` liefert bei `!included` weiter leere Liste).

### 5. Neuer BookDocumentBuilder

* [x] `project/BookDocumentBuilder.kt`, `fun build(book, design, meta): Document`.
* [x] Ein geteiltes `PageLayout`; Reihenfolge: Titel (`SinglePage`), Copyright (`SinglePage`, nur wenn
  nicht leer), Prolog, Kapitel, Epilog, Klappentext (je `FlowPage`).
* [x] Prolog/Epilog erzeugen ihre Seite unabhängig von `included`
  (`TODO(simPlay page policy)`: Nummerierung/Deaktivierung später).

### 6. Seitenpolitik als TODO

* [x] `TODO(simPlay page policy)` für Nummerierung, gespiegelte Ränder, inaktive Seiten,
  Klappentext-Kante, führende/abschließende Leerseite.

### 7. Tests

* [x] `StyleTranslationTest`, `PageLayoutTranslationTest` (umbenannt), vier Builder-Tests auf simPlay-Typen.
* [x] Neuer `BookDocumentBuilderTest`: Seitenreihenfolge, Seitentypen, `included`-Fälle, Designwirkung,
  Blockinhalt je Seite gegen den jeweiligen Builder.
* [x] Kein `SimpLayEngine`-Lauf; Entwicklertests, Paketspiegelung.
* [ ] Ausführung: braucht `simplay-engine` aus GitHub Packages (Token) — noch offen.

### 8. Dokumentation und Abschluss

* [ ] `README.md`: Zeile `ai-ghost-layouting-model` auf „Implemented", Text „auf simPlay-Rohmodell".
* [ ] `CHANGELOG.md`: unverändert (nicht endnutzersichtbar). MkDocs: keine Modulseite betroffen.
* [ ] Build über Agent: `ai-ghost-layouting-model` grün; `app/ui` bricht weiter (IP-31/IP-34).
* [ ] Feature-Status/Overview/Feature-Plan: IP-30 auf `COMPLETED`/✅; Abweichungen festhalten.
* [ ] Diese Plandatei im selben Change-Set per `git rm` entfernen.

## Offen bis Token vorhanden (Nutzer)

* GitHub-Packages-Token (`gpr.user`/`gpr.key` in `~/.gradle/gradle.properties`, NICHT eingecheckt).
* Danach: `./gradlew :lib:ai-ghost-layouting-model:build` über Agent; JPMS-Modulname und
  `kotlinx.serialization`-`requires` am realen Jar bestätigen; Lizenzbericht; Aufgabe 8 abschließen.

## Ergebnis

* `lib/ai-ghost-layouting-model` erzeugt aus `Book`/`Design`/`Meta` ein simPlay-`Document`.
* Jeder Buchteil beginnt auf einer eigenen Seite; Titel- und Copyright-Seite sind `SinglePage`.
* Die fehlende ai-ghost-Seitenpolitik ist überall als `TODO(simPlay page policy)` markiert.

## Verifikation

* `./gradlew :lib:ai-ghost-layouting-model:build` grün, inkl. `koverVerify` und `licensee`.
* `BookDocumentBuilderTest` deckt Seitenreihenfolge, Seitentypen, `included`-Fälle und Designwirkung ab.
* `jar --describe-module` am `simplay-engine`-Jar bestätigt den in `module-info.java` genutzten Namen.
* Voller `./gradlew build`: nur noch `app/ui` bricht (unaufgelöste `layouting.fx`-Importe → IP-31/IP-34).
