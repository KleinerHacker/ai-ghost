# Implementierungspläne: Paper Writing Surface

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Reihenfolge und Abhängigkeitsgraph: Abschnitt 8 des Feature Plans.
* Ein Plan startet erst, wenn jede Voraussetzung `COMPLETED` ist.
* Ein erledigter Plan wird samt Statusdatei entfernt; erledigt sind IP-01, IP-02, IP-24, IP-03,
  IP-25, IP-22, IP-14 und IP-26.
* Den Stand jedes Plans führt `.claude/plans/features/FP-001-PaperWritingSurface-status.md`.
* IP-25 bis IP-28 tragen die eigenständige JavaFX-Renderer-Bibliothek `lib/layouting-fx`.
* IP-07 und IP-08 liegen in dieser Bibliothek; IP-06, IP-10, IP-13, IP-16 und IP-21 sind darauf
  angepasst.

## Pläne

| ID | Plan | Datei | Voraussetzung |
|----|------|-------|---------------|
| IP-04 | Seitenumbruch und Paginierung | `FP-001-IP-04-SeitenumbruchUndPaginierung.md` | IP-03 |
| IP-05 | Inkrementelles Layout und Zwischenspeicher | `FP-001-IP-05-InkrementellesLayout.md` | IP-04 |
| IP-06 | Layout-Regressionsprüfstand | `FP-001-IP-06-LayoutRegressionsPruefstand.md` | IP-04, IP-07, IP-08 |
| IP-07 | Seiten-Ansicht | `FP-001-IP-07-SeitenAnsicht.md` | IP-04, IP-26 |
| IP-08 | Schreibblatt-Ansicht | `FP-001-IP-08-SchreibblattAnsicht.md` | IP-04, IP-26 |
| IP-27 | Styling- und Theming-API der Bibliothek | `FP-001-IP-27-BibliotheksStyling.md` | IP-07, IP-08 |
| IP-28 | Eigenständige Nutzung und Dokumentation | `FP-001-IP-28-EigenstaendigeNutzung.md` | IP-27 |
| IP-09 | Undo- und Redo-Infrastruktur | `FP-001-IP-09-UndoRedoInfrastruktur.md` | - |
| IP-10 | Schreibfläche für Buchteile | `FP-001-IP-10-Schreibflaeche.md` | IP-08, IP-09 |
| IP-11 | Absatz-Operationen | `FP-001-IP-11-AbsatzOperationen.md` | IP-10 |
| IP-12 | Inspector-Grundgerüst und Inhaltsabschnitte | `FP-001-IP-12-InspectorGrundgeruest.md` | - |
| IP-13 | Design-Stilabschnitte | `FP-001-IP-13-DesignStilAbschnitte.md` | IP-02, IP-12, IP-26 |
| IP-15 | Editor-Aufteilung und Baum-Routing | `FP-001-IP-15-EditorAufteilungUndBaumRouting.md` | IP-11, IP-12 |
| IP-16 | Schreib- und Vorschaumodus | `FP-001-IP-16-SchreibUndVorschauModus.md` | IP-05, IP-07, IP-15 |
| IP-17 | AI-Aktionsport | `FP-001-IP-17-AiAktionsPort.md` | - |
| IP-18 | AI-Aktionen an Absatz und Überschrift | `FP-001-IP-18-AiAktionenAmAbsatz.md` | IP-10, IP-17 |
| IP-19 | AI-Generierung eines Teils | `FP-001-IP-19-AiTeilGenerierung.md` | IP-12, IP-17 |
| IP-21 | Seitentrennung innerhalb eines Absatzes | `FP-001-IP-21-SeitentrennungImAbsatz.md` | IP-11 |
| IP-23 | Optionale Teile im Projektbaum | `FP-001-IP-23-OptionaleTeileImBaum.md` | IP-15, IP-24 |

## Ohne Voraussetzung startbar

* IP-09 - Undo- und Redo-Infrastruktur
* IP-12 - Inspector-Grundgerüst und Inhaltsabschnitte
* IP-17 - AI-Aktionsport
