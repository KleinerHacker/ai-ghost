# Feature Status: Panelium Chrome & Menü

Status: NOT_STARTED

## Implementation Plans

| ID | Implementation Plan | Status |
|----|---------------------|--------|
| IP-01 | ChromePane-Einbindung | NOT_STARTED |
| IP-02 | MenuPane-Grundgerüst | NOT_STARTED |
| IP-03 | Reiter Bearbeiten | NOT_STARTED |
| IP-04 | Reiter Publish | NOT_STARTED |
| IP-05 | Datei-Menü: Neu | NOT_STARTED |
| IP-06 | Datei-Menü: Öffnen | NOT_STARTED |
| IP-07 | Datei-Menü: Speichern / Speichern unter | NOT_STARTED |
| IP-08 | Datei-Menü: Einmalige Aktionen | NOT_STARTED |
| IP-09 | ChromePane-Schnellaktionen | NOT_STARTED |

## Overall Progress

0%

## Notes

Feature Plan erstellt. Noch keine Implementation Plan gestartet.

Update 2026-09-19: panelium-fx wurde auf Version 0.4.0 aktualisiert. Laut Online-Doku
(`menu-pane/implementation`, `panelium-chrome/implementation`) liefert panelium-fx nun die
Backstage-Komponente `FXBackstageMenuPane` (Einträge `FXBackstageMenuItem`, Footer-Aktionen
`FXBackstageQuickAction`) für den eingebauten File-Tab von `FXMenuPane` mit. Der Feature Plan
wurde entsprechend korrigiert: Das Datei-Menü wird nicht mehr selbst konzipiert, sondern über
`FXBackstageMenuPane` umgesetzt; die Dependency-Version wurde von 0.3.1 auf 0.4.0 angepasst.

Update 2026-09-19 (Klärungen): Profile für "Neu" sind vorgefertigte Bücher mit optional
vorausgewählten Buchteilen (`BookPart`-Typen). "Zuletzt verwendete Speicherorte" (IP-07) wird
als eigenes Model analog zu `RecentOpened` mitgeführt. GitHub-Package-Repository-Zugang ist
bereits eingerichtet. CI-seitige Authentifizierung liegt in der Verantwortung des Nutzers.
IP-09 nutzt `captionLeftItems` und blendet den Standard-Fenstertitel aus
(`isDefaultTitleVisible = false`).
