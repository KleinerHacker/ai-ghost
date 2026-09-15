# Implementierungspläne: Paper Writing Surface

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Reihenfolge und Abhängigkeitsgraph: Abschnitt 8 des Feature Plans.
* Ein Plan startet erst, wenn jede Voraussetzung `COMPLETED` ist.
* Den Stand jedes Plans führt `.claude/plans/features/FP-001-PaperWritingSurface-status.md`.

## Erste Planabweichung (simPlay)

* Das Text-Layouting und das FX-Rendering wurden nach simPlay ausgelagert
  (`org.pcsoft.framework:simplay-engine`, `simplay-fx`, GitHub Packages).
* `lib/ai-ghost-layouting` und `lib/ai-ghost-layouting-fx` wurden per `git rm` entfernt (IP-29 ✅).
* Abgeschlossen und unberührt: IP-01 (Messteil abgelöst), IP-02, IP-24, IP-09, IP-12, IP-13, IP-14,
  IP-17, IP-19 – ihre Dateien und Statusdateien sind bereits entfernt.
* Abgelöste Pläne (Dateien entfernt): IP-03, IP-04, IP-05, IP-06, IP-07, IP-08, IP-10, IP-11, IP-22,
  IP-25, IP-26 – Begründung in Abschnitt 6 des Feature Plans.
* Entfernte offene Pläne: IP-21 (in `PaperSheetView` nativ), IP-27 (simPlay besitzt das Styling),
  IP-28 (Bibliothek nicht mehr im Repository).

## Zweite Planabweichung (TextAnchor, ab simPlay 0.3.1)

* Kein pro Buchteil gebautes `Document` mehr; `PaperSheetView` zeigt dauerhaft das ganze Buch als ein
  einziges `Document`. Eine Baumauswahl navigiert über `TextAnchor`, statt ein `Document` zu tauschen.
* `Book.document: Document` wird der gespeicherte Zustand des Fließtexts; `Book`, `BookPart`,
  `Chapter`, `Copyright` verlieren ihre Textfelder. `Chapter` bekommt eine gespeicherte `id: UUID` als
  Anker; statische Teile behalten feste Kennungen (`title`, `copyright`, `prolog`, `epilog`, `blurb`).
  `TextStyle` im `Document` wird beim Laden verworfen und aus `Design` neu berechnet.
* Vollständig abgelöst, Dateien per `git rm` entfernt: IP-15 (Editor-Aufteilung und Baum-Routing),
  IP-16 (Schreib- und Vorschaumodus) – beide durch IP-39 ersetzt.
* IP-31 bleibt abgeschlossen, sein Ergebnis (Dokument-Tausch je Auswahl) ist abgelöst; seine reinen
  Funktionen und sein Sync-Muster gehen in IP-39 über.
* Begründung: Abschnitt 6 des Feature Plans, „Abgelöste Pläne (TextAnchor)“.

## Pläne

| ID | Plan | Datei | Voraussetzung |
|----|------|-------|---------------|
| IP-29 ✅ | simPlay-Integration | (Datei entfernt) | - |
| IP-30 ✅ | Buch zu simPlay-Dokument-Builder | (Datei entfernt) | IP-29, IP-02, IP-24 |
| IP-34 ✅ | Schriftermittlung und Metrik-Fingerabdruck auf simPlay | (Datei entfernt) | IP-29 |
| IP-31 ✅ (Ergebnis abgelöst) | Schreibfläche auf PaperSheetView | (Datei entfernt) | IP-30, IP-09, IP-34 |
| IP-35 ✅ | Seitenzahl und Seitenmodi auf simPlay 0.3.0 | (Datei entfernt) | IP-30 |
| IP-36 | Modell-Umstellung auf Anker-Struktur | `FP-001-IP-36-ModellUmstellungAufAnkerStruktur.md` | IP-24, IP-02 |
| IP-37 | Dokument-Persistenz und Migration | `FP-001-IP-37-DokumentPersistenzUndMigration.md` | IP-36, IP-29 |
| IP-38 | Buch-Dokument als alleinige Basis | `FP-001-IP-38-BuchDokumentAlsAlleinigeBasis.md` | IP-37, IP-30, IP-34 |
| IP-39 ✅ | PaperSheetView dauerhaft im Zentrum | (Datei entfernt) | IP-38, IP-09 |
| IP-32 | Absatz-Operationen auf dem Dokument | `FP-001-IP-32-AbsatzOperationenAufDokument.md` | IP-39 |
| IP-33 | Undo auf dem unveränderlichen Dokument-Tausch | `FP-001-IP-33-UndoAufDokumentTausch.md` | IP-39 |
| IP-18 | KI-Schaltflächen an Absatz und Überschrift | `FP-001-IP-18-AiAktionenAmAbsatz.md` | IP-39 |
| IP-23 | Optionale Teile im Projektbaum | `FP-001-IP-23-OptionaleTeileImBaum.md` | IP-39, IP-24, IP-35 |

## Abgeschlossene Pläne

* IP-29 – simPlay-Integration. simPlay-Repository und exakte Version im Wurzel-Build; Eigenbaumodule
  entfernt. Plandatei entfernt.
* IP-30 – Buch zu simPlay-Dokument-Builder. `BookDocumentBuilder` und die Teil-Builder bleiben
  vollständig in Kraft, werden von IP-38 weiterverwendet. Plandatei entfernt.
* IP-34 – Schriftermittlung und Metrik-Fingerabdruck auf simPlay. Plandatei entfernt.
* IP-31 – Schreibfläche auf PaperSheetView. Ergebnis (Dokument-Tausch je Auswahl) durch die zweite
  Abweichung abgelöst; reine Funktionen (`splitParagraph` &c.) und das `documentProperty`-Sync-Muster
  gehen in IP-39 über. Plandatei entfernt.
* IP-35 – Seitenzahl und Seitenmodi auf simPlay 0.3.0. `PageMode.DISABLED`-Verdrahtung war auf eine
  künftige Buchvorschau vertagt; findet jetzt direkt in IP-23 statt. Plandatei entfernt.
* IP-36 – Modell-Umstellung auf Anker-Struktur. Plandatei entfernt.
* IP-37 – Dokument-Persistenz und Migration (ohne Migration alter Projekte). Plandatei entfernt.
* IP-38 – Buch-Dokument als alleinige Basis. Plandatei entfernt.
* IP-39 – PaperSheetView dauerhaft im Zentrum. Werkzeugleisten-Umschalter ohne Icon (Tooling-Blocker),
  Splitter-Positionen/Inspector-Einklappzustand nicht umgesetzt (offene TODOs). Plandatei entfernt.

## Startbereit (Voraussetzungen erfüllt)

* IP-32 – Absatz-Operationen auf dem Dokument (Voraussetzung IP-39 ✅)
* IP-33 – Undo auf dem unveränderlichen Dokument-Tausch (Voraussetzung IP-39 ✅)
* IP-18 – KI-Schaltflächen an Absatz und Überschrift (Voraussetzung IP-39 ✅)
* IP-23 – Optionale Teile im Projektbaum (Voraussetzungen IP-39 ✅, IP-24 ✅, IP-35 ✅)
