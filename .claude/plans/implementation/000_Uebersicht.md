# Implementierungspläne: Paper Writing Surface

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Reihenfolge und Abhängigkeitsgraph: Abschnitt 8 des Feature Plans.
* Ein Plan startet erst, wenn jede Voraussetzung `COMPLETED` ist.

## Pläne

| ID | Plan | Datei | Voraussetzung |
|----|------|-------|---------------|
| IP-01 | Font-Ermittlung und Textmessung | `IP-01_FontErmittlungUndTextmessung.md` | - |
| IP-02 | Design mit Seitenformat | `IP-02_DesignSeitenformat.md` | - |
| IP-24 | Optionale Teile im Modell | `IP-24_OptionaleTeileImModell.md` | - |
| IP-03 | Layout-Kern | `IP-03_LayoutKern.md` | IP-01, IP-02, IP-24 |
| IP-04 | Seitenumbruch und Paginierung | `IP-04_SeitenumbruchUndPaginierung.md` | IP-03 |
| IP-05 | Inkrementelles Layout und Zwischenspeicher | `IP-05_InkrementellesLayout.md` | IP-04 |
| IP-06 | Layout-Regressionsprüfstand | `IP-06_LayoutRegressionsPruefstand.md` | IP-04, IP-07, IP-08 |
| IP-07 | Seiten-Ansicht | `IP-07_SeitenAnsicht.md` | IP-04 |
| IP-08 | Schreibblatt-Ansicht | `IP-08_SchreibblattAnsicht.md` | IP-04 |
| IP-09 | Undo- und Redo-Infrastruktur | `IP-09_UndoRedoInfrastruktur.md` | - |
| IP-10 | Schreibfläche für Buchteile | `IP-10_Schreibflaeche.md` | IP-08, IP-09 |
| IP-11 | Absatz-Operationen | `IP-11_AbsatzOperationen.md` | IP-10 |
| IP-12 | Inspector-Grundgerüst und Inhaltsabschnitte | `IP-12_InspectorGrundgeruest.md` | - |
| IP-13 | Design-Stilabschnitte | `IP-13_DesignStilAbschnitte.md` | IP-01, IP-02, IP-12 |
| IP-14 | Projekteinstellungen-Dialog | `IP-14_ProjekteinstellungenDialog.md` | IP-02 |
| IP-15 | Editor-Aufteilung und Baum-Routing | `IP-15_EditorAufteilungUndBaumRouting.md` | IP-11, IP-12 |
| IP-16 | Schreib- und Vorschaumodus | `IP-16_SchreibUndVorschauModus.md` | IP-05, IP-07, IP-15 |
| IP-17 | AI-Aktionsport | `IP-17_AiAktionsPort.md` | - |
| IP-18 | AI-Aktionen an Absatz und Überschrift | `IP-18_AiAktionenAmAbsatz.md` | IP-10, IP-17 |
| IP-19 | AI-Generierung eines Teils | `IP-19_AiTeilGenerierung.md` | IP-12, IP-17 |
| IP-21 | Seitentrennung innerhalb eines Absatzes | `IP-21_SeitentrennungImAbsatz.md` | IP-11 |
| IP-22 | Schrift-Identität und Ersatzmeldung | `IP-22_SchriftIdentitaetUndErsatzmeldung.md` | IP-01 |
| IP-23 | Optionale Teile im Projektbaum | `IP-23_OptionaleTeileImBaum.md` | IP-15, IP-24 |

## Ohne Voraussetzung startbar

* IP-01 - Font-Ermittlung und Textmessung
* IP-02 - Design mit Seitenformat
* IP-24 - Optionale Teile im Modell
* IP-09 - Undo- und Redo-Infrastruktur
* IP-12 - Inspector-Grundgerüst und Inhaltsabschnitte
* IP-17 - AI-Aktionsport
