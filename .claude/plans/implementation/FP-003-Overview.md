# Implementierungspläne: Panelium Chrome & Menü

* Feature Plan: `.claude/plans/features/FP-003-PaneliumChromeMenu.md`
* Reihenfolge und Abhängigkeitsgraph: Abschnitt 7 des Feature Plans.
* Ein Plan startet erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Ein erledigter Plan wird samt seiner Zeile hier entfernt; den Stand führt
  `FP-003-PaneliumChromeMenu-status.md`.

## Pläne

| ID | Plan | Datei | Voraussetzung |
|----|------|-------|---------------|
| IP-01 | ChromePane-Einbindung | `FP-003-IP-01-ChromePaneEinbindung.md` | - |
| IP-02 | MenuPane-Grundgerüst | `FP-003-IP-02-MenuPaneGrundgeruest.md` | IP-01 |
| IP-03 | Reiter Bearbeiten | `FP-003-IP-03-ReiterBearbeiten.md` | IP-02 |
| IP-04 | Reiter Publish | `FP-003-IP-04-ReiterPublish.md` | IP-02 |
| IP-05 | Datei-Menü: Neu | `FP-003-IP-05-DateiMenuNeu.md` | IP-02 |
| IP-06 | Datei-Menü: Öffnen | `FP-003-IP-06-DateiMenuOeffnen.md` | IP-02 |
| IP-07 | Datei-Menü: Speichern / Speichern unter | `FP-003-IP-07-DateiMenuSpeichern.md` | IP-02 |
| IP-08 | Datei-Menü: Einmalige Aktionen | `FP-003-IP-08-DateiMenuEinmaligeAktionen.md` | IP-02 |
| IP-09 | ChromePane-Schnellaktionen | `FP-003-IP-09-ChromePaneSchnellaktionen.md` | IP-01, IP-03, IP-07 |

## Ohne Voraussetzung startbar

* IP-01 - ChromePane-Einbindung
