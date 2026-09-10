# Feature-Status: Paper Writing Surface

Status: IN_PROGRESS

## Implementierungspläne

| ID    | Implementierungsplan                             | Status      |
|-------|------------------------------------------------|-------------|
| IP-01 | Font Discovery And Text Measuring (teilw. abgelöst) | COMPLETED   |
| IP-02 | Design Page Format Model                       | COMPLETED   |
| IP-24 | Optional Parts In The Model                    | COMPLETED   |
| IP-09 | Undo And Redo Infrastructure                   | COMPLETED   |
| IP-12 | Inspector Shell And Content Sections           | COMPLETED   |
| IP-13 | Design Style Sections                          | COMPLETED   |
| IP-14 | Project Settings Dialog                        | COMPLETED   |
| IP-17 | AI Action Port                                 | COMPLETED   |
| IP-19 | AI Part Generation (button only)               | COMPLETED   |
| IP-29 | simPlay Integration                            | COMPLETED   |
| IP-30 | Book To simPlay Document Builder               | NOT_STARTED |
| IP-34 | Font Discovery And Metric Fingerprint On simPlay | NOT_STARTED |
| IP-31 | Writing Surface On PaperSheetView              | NOT_STARTED |
| IP-32 | Paragraph Structure Operations On Document     | NOT_STARTED |
| IP-33 | Undo On Immutable Document Swap                | NOT_STARTED |
| IP-15 | Editor Arrangement And Tree Routing            | NOT_STARTED |
| IP-16 | Writing And Preview Modes                      | NOT_STARTED |
| IP-18 | AI Actions On Paragraph And Heading            | NOT_STARTED |
| IP-23 | Optional Book Parts In The Tree                | NOT_STARTED |

## Abgelöste Pläne (simPlay-Abweichung)

Diese Pläne waren umgesetzt, ihre Ergebnisse werden durch simPlay ersetzt. Sie zählen nicht mehr zum
Fortschritt. Begründung im Feature-Plan, Abschnitt 6, „Abgelöste Pläne“.

| ID    | Früherer Plan                              | Ersetzt durch                        |
|-------|--------------------------------------------|--------------------------------------|
| IP-03 | Layout Core                                | `simplay-engine` (IP-29)             |
| IP-04 | Pagination And Page Break Policy           | `simplay-engine`; Restpolitik = TODO |
| IP-05 | Incremental Layout And Caching             | `PaperSheetView` / `measure` (IP-31) |
| IP-06 | Layout Regression Harness                  | entfällt; dünne Prüfung in IP-16     |
| IP-07 | Paper Page View                            | `PaperSheetView READONLY` (IP-16)    |
| IP-08 | Paper Flow View                            | `PaperSheetView EDITABLE` (IP-31)    |
| IP-10 | Book Part Writing Surface                  | IP-31                                |
| IP-11 | Paragraph Structure Operations             | IP-32                                |
| IP-22 | Font Identity And Substitution Reporting   | IP-34                                |
| IP-25 | Renderer Library Module                    | entfällt (IP-29 nimmt Regel zurück)  |
| IP-26 | Font And Measuring Migration               | entfällt; Schrift-Stack zu `app/ui`  |

## Gesamtfortschritt

53 %

## Anmerkungen

Große Planabweichung: Das gesamte Text-Layouting und FX-Rendering wird nach simPlay
(`org.pcsoft.framework:simplay-*`, GitHub Packages) ausgelagert. Vom Nutzer bestätigt:

* simPlay vollständig übernehmen; `lib/ai-ghost-layouting` und `lib/ai-ghost-layouting-fx` per
  `git rm` entfernen; `lib/ai-ghost-layouting-model` als Übersetzer behalten und auf das
  simPlay-Rohmodell umstellen.
* ai-ghost-Seitenpolitik (Nummerierung, gespiegelte Ränder, inaktive/leere Seiten,
  Klappentext-Kante) fügt der Nutzer nachträglich in simPlay ein; bis dahin trägt IP-30 nur eine
  Zwischenlösung, und die Lücke ist als TODO markiert.
* Schrift-Fingerabdruck (früher IP-22) bleibt, kommt seit simPlay 0.2.1 aber direkt aus
  `simplay-fx`s `FxFontProbe`, nicht aus eigenem JavaFX-Messcode. Nur die Ersatzfamilie-Meldung und
  die Familienliste für die Auswahl ziehen nach `app/ui` (IP-34).

Abgeschlossen und von der Abweichung unberührt: IP-01 (Messteil abgelöst), IP-02, IP-24, IP-09,
IP-12, IP-13, IP-14, IP-17, IP-19.

Offene Pläne, auf simPlay umgeschrieben: IP-15, IP-16, IP-18, IP-23. Neu: IP-29, IP-30, IP-31,
IP-32, IP-33, IP-34.

IP-29 abgeschlossen: simPlay-Repository und exakte Version (`0.2.1`) im Wurzel-Build, `mavenLocal()`
als tokenloser Weg; `lib/ai-ghost-layouting` und `lib/ai-ghost-layouting-fx` per `git rm` entfernt;
CI-Job `regression-test` (samt beider `regressionTest`-Gradle-Tasks) entfernt, `GITHUB_TOKEN` in alle
Gradle-Jobs gezogen; Architekturregel auf `app/ui` als einzigen JavaFX-Ort verengt. Die `api`/
`implementation`-Verdrahtung von `simplay-engine`/`simplay-fx` und die simPlay-Lizenzprüfung folgen
mit IP-30/IP-31/IP-34. Build: Konfiguration grün, `ai-ghost-layouting-model` und `app/ui` brechen
erwartungsgemäß am entfernten Modul; alle übrigen Module grün.

Nächster Schritt: IP-30 (Buch zu simPlay-Dokument-Builder) und IP-34 (Schrift-Stack auf `FxFontProbe`)
– beide hängen nur an IP-29 und heilen die erwarteten Kompilierbrüche.

Die entfernten Plandateien `FP-001-IP-21-SeitentrennungImAbsatz.md`,
`FP-001-IP-27-BibliotheksStyling.md` und `FP-001-IP-28-EigenstaendigeNutzung.md` wurden per `git rm`
gelöscht: IP-21 (Seitentrennung im Absatz) ist in `PaperSheetView` nativ, IP-27 (Bibliotheks-Styling)
gehört zu simPlay (Überschreibung der `-fx-`-Eigenschaften jetzt in IP-31), IP-28 (eigenständige
Bibliothek) entfällt, da die Bibliothek nicht mehr im ai-ghost-Repository liegt.
