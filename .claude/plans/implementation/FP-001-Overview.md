# Implementierungspläne: Paper Writing Surface

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Reihenfolge und Abhängigkeitsgraph: Abschnitt 8 des Feature Plans.
* Ein Plan startet erst, wenn jede Voraussetzung `COMPLETED` ist.
* Den Stand jedes Plans führt `.claude/plans/features/FP-001-PaperWritingSurface-status.md`.

## Große Planabweichung (simPlay)

* Das Text-Layouting und das FX-Rendering werden nach simPlay ausgelagert
  (`org.pcsoft.framework:simplay-engine`, `simplay-fx`, GitHub Packages).
* `lib/ai-ghost-layouting` und `lib/ai-ghost-layouting-fx` werden per `git rm` entfernt (IP-29).
* `lib/ai-ghost-layouting-model` bleibt und wird auf das simPlay-Rohmodell umgestellt (IP-30).
* Abgeschlossen und unberührt: IP-01 (Messteil abgelöst), IP-02, IP-24, IP-09, IP-12, IP-13, IP-14,
  IP-17, IP-19 – ihre Dateien und Statusdateien sind bereits entfernt.
* Abgelöste Pläne (Dateien entfernt): IP-03, IP-04, IP-05, IP-06, IP-07, IP-08, IP-10, IP-11, IP-22,
  IP-25, IP-26 – Begründung in Abschnitt 6 des Feature Plans.
* Entfernte offene Pläne: IP-21 (in `PaperSheetView` nativ), IP-27 (simPlay besitzt das Styling),
  IP-28 (Bibliothek nicht mehr im Repository).

## Pläne

| ID | Plan | Datei | Voraussetzung |
|----|------|-------|---------------|
| IP-29 | simPlay-Integration | `FP-001-IP-29-SimPlayIntegration.md` | - |
| IP-30 | Buch zu simPlay-Dokument-Builder | `FP-001-IP-30-BuchZuSimPlayDokument.md` | IP-29, IP-02, IP-24 |
| IP-34 | Schriftermittlung und Metrik-Fingerabdruck auf simPlay | `FP-001-IP-34-SchriftUndFingerabdruckAufSimPlay.md` | IP-29 |
| IP-31 | Schreibfläche auf PaperSheetView | `FP-001-IP-31-SchreibflaecheAufPaperSheetView.md` | IP-30, IP-09, IP-34 |
| IP-32 | Absatz-Operationen auf dem Dokument | `FP-001-IP-32-AbsatzOperationenAufDokument.md` | IP-31 |
| IP-33 | Undo auf dem unveränderlichen Dokument-Tausch | `FP-001-IP-33-UndoAufDokumentTausch.md` | IP-31 |
| IP-15 | Editor-Aufteilung und Baum-Routing | `FP-001-IP-15-EditorAufteilungUndBaumRouting.md` | IP-31, IP-12 |
| IP-16 | Schreib- und Vorschaumodus | `FP-001-IP-16-SchreibUndVorschauModus.md` | IP-30, IP-31, IP-15 |
| IP-18 | KI-Schaltflächen an Absatz und Überschrift | `FP-001-IP-18-AiAktionenAmAbsatz.md` | IP-31 |
| IP-23 | Optionale Teile im Projektbaum | `FP-001-IP-23-OptionaleTeileImBaum.md` | IP-15, IP-24 |

## Ohne Voraussetzung startbar

* IP-29 - simPlay-Integration (Repository, Abhängigkeit, Lizenz-Allowlist, CI-Token, Modul-Entfernung)
