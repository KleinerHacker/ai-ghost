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
| IP-30 | Book To simPlay Document Builder               | COMPLETED   |
| IP-34 | Font Discovery And Metric Fingerprint On simPlay | COMPLETED   |
| IP-31 | Writing Surface On PaperSheetView              | COMPLETED   |
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

68 %

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

IP-30 abgeschlossen: `lib/ai-ghost-layouting-model` übersetzt `Book`/`Design`/`Meta` in ein simPlay-
`Document` (`BookDocumentBuilder`, vier Block-Builder, `StyleTranslation`, `PageLayoutTranslation`).
Auf simPlay 0.2.2 gehoben (Artefakt-Umbenennung `simplay-engine-jvm`); `licensee` läuft ohne
Ausnahme grün. Die fehlende ai-ghost-Seitenpolitik (Nummerierung, gespiegelte Ränder, inaktive/leere
Seiten, Klappentext-Kante) ist durchgehend als `TODO(simPlay page policy)` markiert, kein Eigenbau.
`requires transitive org.pcsoft.framework.simplay.engine` im `module-info.java` bleibt auf ein
automatisches Modul gerichtet – `simplay-engine-jvm` liefert (Stand 0.2.2) noch keinen eigenen
Moduldeskriptor; der abgeleitete Name stimmt, ist aber nicht vertraglich stabil. Build:
`lib/ai-ghost-layouting-model` komplett grün (Tests, `koverVerify`, `licensee`); `app/ui` bricht
weiter erwartungsgemäß an den entfernten `layouting`/`layouting.fx`-Importen.

IP-34 abgeschlossen: `app/ui`s Schrift-Stack läuft vollständig über `simplay-fx`s `FxFontProbe`.
`FontData.metrics: FontMetricsData?` entfällt zugunsten von `FontData.fingerprint: String?` (roher
`FontFingerprint.encode()`-String); `FontMetricsData`/`FontMetricsDataProperty` sind per `git rm`
entfernt. `FontIdentity`/`FontIdentityCheck`/`FontTranslation` sind auf simPlays Typen umgestellt,
`FontSubstitution` ergänzt die Ersatzfamilie, `StyleDataEditorView`/`ViewModel` nutzen
`Font.getFamilies()` statt des entfernten `FontCatalog`. simPlay 0.2.2s Gradle-Metadaten führen
`simplay-common` (`FontAvailability`) nur in der `runtimeElements`-, nicht der `apiElements`-Variante
von `simplay-fx`; `app/ui` führt `simplay-common` deshalb als eigene, vom Nutzer bestätigte
Abhängigkeit. Build: `lib/ai-ghost-model`, `lib/ai-ghost-fx-model`, `lib/ai-ghost-layouting-model` und
der Schrift-Teil von `app/ui` komplett grün; `app/ui` bricht weiter erwartungsgemäß an
`BookPartEditorView`/`BookPartEditorViewModel`/`BookPartEditorController` (`PaperFlowView`,
`PaperFlowListener`, `JavaFxTextMetrics`, `DocumentLayout`, ... - IP-31s Aufgabe).

IP-31 abgeschlossen: `BookPartEditor` schreibt auf einer `PaperSheetView` je Teil ein eigenes
`Document` (`SinglePage` für Titel-/Copyright-Seite, `FlowPage` sonst); `paperSheetView.mode`
wechselt `EDITABLE`/`READONLY` auf derselben Komponente. Kein manueller Layout-Stack mehr in
`app/ui` - `PaperSheetView` bricht Zeilen und paginiert selbst; `IncrementalLineBreaker`/
`GreedyLineBreaker`/`JavaFxTextMetrics`/`DocumentLayout`/`LayoutEngine`/`PageGeometry`/
`NonePageBreakPolicy` entfallen ersatzlos. Bearbeitungen werden über einen
`documentProperty`-`ChangeListener` erkannt und index-weise zurückgeschrieben, statt über das
entfallene `PaperFlowListener`. `splitParagraph`/`mergeParagraph`/`removeParagraph`/
`moveParagraph` bleiben ungenutzt für IP-32 in `BookPartEditorController` stehen.

**Beim Testen in simPlay 0.2.2 gefunden und dem Nutzer gemeldet:** `TextBlock.toString()` fügt ein
Leerzeichen vor einem `TextWord` ein, das direkt auf ein Symbol ohne echtes Leerzeichen folgt, und
verschluckt ein alleinstehendes angehängtes Leerzeichen beim Retokenisieren; beides bringt
`DocumentEditor.splice()`s `caretIndex` gegenüber dem gespeicherten Text aus dem Takt und lässt beim
Tippen jedes weitere Zeichen eine Position zu früh landen. `BookPartEditorTest` umgeht das mit
reinen Buchstaben-Fortsetzungen ohne Symbol/Leerzeichen-Übergang; **IP-32 braucht den Fix
upstream**, da es echte Trennzeichen tippt. README/CHANGELOG-Einträge, die Absatz-Trennen/
-Verschmelzen/-Verschieben (IP-32) bereits als umgesetzt auswiesen, wurden auf den tatsächlichen
Stand zurückgesetzt.

Build: `lib:*`, `app:ai-ghost-ui` komplett grün (Kompilierung, Tests, `koverVerify`, `licensee`).

Nächster Schritt: IP-32 (Absatz-Operationen auf `Document`) oder IP-33 (Undo auf Dokument-Tausch) -
beide hängen nur noch an IP-31, jetzt abgeschlossen.

Die entfernten Plandateien `FP-001-IP-21-SeitentrennungImAbsatz.md`,
`FP-001-IP-27-BibliotheksStyling.md` und `FP-001-IP-28-EigenstaendigeNutzung.md` wurden per `git rm`
gelöscht: IP-21 (Seitentrennung im Absatz) ist in `PaperSheetView` nativ, IP-27 (Bibliotheks-Styling)
gehört zu simPlay (Überschreibung der `-fx-`-Eigenschaften jetzt in IP-31), IP-28 (eigenständige
Bibliothek) entfällt, da die Bibliothek nicht mehr im ai-ghost-Repository liegt.
