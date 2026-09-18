# Feature Plan: Panelium Chrome & Menü

## 1. Objective

* Ersetzung der bestehenden JavaFX `MenuBar` und `ToolBar` im Hauptfenster durch panelium-fx
* Einbindung des Panelium `ChromePane` als neuer Fensterrahmen
* Einbindung des Panelium `FXMenuPane` (MenuPane) als neues Datei-/Bearbeiten-/Publish-Menü
* Nachbildung eines Office-artigen "Backstage"-Bereichs für das Datei-Menü auf eigener Basis

## 2. Current State

* `MainWindowView.fxml` nutzt eine klassische `MenuBar` + `ToolBar` in einer `VBox` im `top` des `BorderPane`
* Datei-Menü: Neu (Project, Chapter), Öffnen (`actionOpen`), `mnuOpenRecent` (dynamisch aus `viewModel.openRecent`), Speichern (`actionSave`), Speichern unter (`actionSaveAs`), Preferences (Menüpunkt ohne `onAction`), Project Settings (`actionProjectSettings`), Exit (`actionExit`)
* Bearbeiten-Menü: Undo/Redo, gebunden an `viewModel.undoStack` (`canUndoProperty`/`canRedoProperty`, `undoEntries`/`redoEntries`)
* Publish-Menü: Untermenü Export → PDF
* Help-Menü: Online Doku (`actionHelpOnline()` ohne Implementierung), About (ohne `onAction`)
* ToolBar spiegelt Chapter, Open, Save, SaveAs, Undo/Redo (als `SplitMenuButton` mit Historie-Dropdown), Preferences, ProjectSettings, Export, HelpOnline als Icon-Buttons
* Recent-Files-Mechanismus existiert bereits (`viewModel.openRecent`, Observable-Liste von `File`)
* Letzte Speicherorte für Speichern/Speichern unter existieren NICHT, nur ein einfacher `FileChooser`
* Kein Profil-Konzept für "Neu" vorhanden (nur feste Einträge Project/Chapter)
* Preferences- und About-Dialoge existieren nicht verdrahtet bzw. nicht auffindbar
* Icons werden über `AiGhostIcons` (Kotlin-Objekt, `fx:factory`) eingebunden; vorhanden u.a. `open`, `save`, `save-as`, `preferences`, `undo`, `redo`, `export`, `help-online`; fehlend: Ausschneiden, Kopieren, Einfügen, About/"i"
* panelium-fx ist als Dependency noch nicht eingebunden

## 3. Requirements

### Functional Requirements

* Fensterrahmen läuft vollständig über den Panelium `ChromePane`
* MenuPane besitzt zunächst die Reiter "Bearbeiten" und "Publish" sowie einen separaten Datei-Menüpunkt
* Reiter "Bearbeiten": Gruppe Einfügen (groß) + Ausschneiden/Kopieren (klein, untereinander); Gruppe Undo (groß) / Redo (groß)
* Reiter "Publish" bleibt zunächst leer (Platzhalter-Struktur)
* Datei-Menü behält alle bisherigen Funktionen: Neu, Öffnen, Speichern, Speichern unter, Preferences, About, Online Doku
* Neu: vordefinierte Profile auf einer Backstage-artigen Ansicht (Detailkonzept folgt in der zugehörigen Implementation Plan)
* Speichern / Speichern unter: Auswahl aus den zuletzt verwendeten Speicherorten
* Öffnen: Liste der zuletzt geöffneten Dateien (Wiederverwendung des bestehenden Recent-Mechanismus)
* Preferences, About und Online Doku erscheinen unten im Datei-Menü als einmalige Aktionen; die zugehörigen Dialoge werden nur so gebaut, dass sie existieren, eine funktionale Verdrahtung ist NICHT Teil dieses Features
* ChromePane erhält Icon-Schaltflächen: Speichern, Separator, Undo, Redo, jeweils mit Anbindung an die bestehende ViewModel-Logik

### Technical Requirements

* Dependency `org.pcsoft.framework:panelium:0.3.1` wird neu eingebunden, inklusive GitHub-Package-Repository-Zugang (`https://maven.pkg.github.com/KleinerHacker/panelium-fx`, GitHub-PAT mit `read:packages`)
* MVVM-FX-Trennung (View/ViewModel) bleibt bestehen, keine Business-Logik in der View
* I18N über bestehende Message Bundles, Übersetzung ausschließlich über den `translator`-Agenten
* Styling ausschließlich über das bestehende zentrale Stylesheet-/Palette-System, keine Inline-Styles
* Fehlende Icons (Ausschneiden, Kopieren, Einfügen, About/"i") werden über den `icon-creator`-Agenten erstellt
* `ui-styling`-Skill gilt für jede Änderung unter `app/ui`
* `fx-component-lifecycle`-Skill gilt, sobald eine neue View globale Listener/Subscriptions registriert
* Laut MkDocs-Doku (`menu-pane/implementation`) ist der `FXMenuPane`-Funktionsumfang selbst noch nicht vollständig implementiert ("current building block") - dies ist bei jeder Implementation Plan, die den MenuPane betrifft, gegenzuprüfen

## 4. Architecture

* Der bestehende `BorderPane` in `MainWindowView.fxml` verliert `MenuBar` und `ToolBar` im `top`-Bereich
* `ChromePane` übernimmt den Fensterrahmen: Einbindung entweder über `PaneliumStage` oder manuell als `ChromePane(content)` in einer transparenten `Scene` mit `StageStyle.TRANSPARENT`; in FXML als `<ChromePane>`-Wurzel- bzw. Kindelement
* Die Titelleiste (`ChromeCaptionBar`) stellt drei Bereichslisten bereit: `captionLeftItems`, `captionCenterItems`, `captionRightItems` (befüllbar per Kotlin `.add(...)` oder als FXML-Kindelemente); die OS-Fensterbuttons (Min/Max/Close) werden automatisch getrennt davon ergänzt
* `FXMenuPane` wird über `MenuChromePane` eingebettet (`menuChromePane.menuPane = menuPane; menuChromePane.body = content`)
* MenuPane-Aufbau: `FXMenuPane()` mit `FXMenuTab(id, title)`-Objekten (`menuPane.tabs.addAll(...)`, `menuPane.activate(tab)`); jeder Tab enthält `FXMenuGroup(vararg boxes, anchor = ...)` mit Titel; Boxen sind `FXMenuGroupLargeBox(...)` (große Buttons, z.B. Einfügen, Undo, Redo) bzw. `FXMenuGroupSmallBox(...)` (bis zu drei kleine, vertikal gestapelte Buttons, z.B. Ausschneiden/Kopieren); `anchor` markiert die nie einklappende Box, `priority` (`FXMenuGroupBoxPriority`) steuert das Einklappverhalten bei Platzmangel
* Das Datei-Menü ist kein regulärer MenuPane-Tab, sondern ein eigener Menüpunkt mit Backstage-artiger Detailansicht, die auf Basis eines eigenen Popups/Overlays selbst konzipiert wird, da panelium-fx dafür keine fertige Komponente liefert
* Bestehende ViewModel-Bindings (`viewModel.undoStack`, `viewModel.openRecent`, `actionSave`, `actionOpen`, `actionSaveAs`) werden wiederverwendet und nur an die neuen View-Elemente umgehängt, nicht neu konzipiert
* Neue Datenmodelle (letzte Speicherorte, Neu-Profile) werden, sofern nötig, unter `lib/model` mit FX-Pendant unter `lib/fx-model` angelegt (`fx-model`-Skill beachten, `model-explore`/`model-creator`-Agenten nutzen)

## 5. Implementation Plan Overview

| ID    | Implementation Plan | Objective | Dependencies |
| ----- | -------------------- | --------- | ------------ |
| IP-01 | ChromePane-Einbindung | Panelium-Dependency einbinden, ChromePane als Fensterrahmen aktivieren | - |
| IP-02 | MenuPane-Grundgerüst | Leere Reiter Bearbeiten/Publish sowie Datei-Menüpunkt anlegen | IP-01 |
| IP-03 | Reiter Bearbeiten | Gruppen Einfügen/Ausschneiden/Kopieren und Undo/Redo im Reiter Bearbeiten füllen | IP-02 |
| IP-04 | Reiter Publish | Struktur des leeren Publish-Reiters anlegen | IP-02 |
| IP-05 | Datei-Menü: Neu | Backstage-Ansicht mit vordefinierten Profilen für Neu | IP-02 |
| IP-06 | Datei-Menü: Öffnen | Liste zuletzt geöffneter Dateien im neuen Datei-Menü | IP-02 |
| IP-07 | Datei-Menü: Speichern/Speichern unter | Auswahl der letzten Speicherorte für Speichern und Speichern unter | IP-02 |
| IP-08 | Datei-Menü: Einmalige Aktionen | Preferences, About, Online Doku unten im Datei-Menü | IP-02 |
| IP-09 | ChromePane-Schnellaktionen | Icon-Schaltflächen Speichern, Separator, Undo, Redo im ChromePane | IP-01, IP-03, IP-07 |

## 6. Implementation Plans

### IP-01: ChromePane-Einbindung

**Objective**

Vollständige Umstellung des Hauptfensters auf den Panelium `ChromePane` als Fensterrahmen.

**Scope**

* Einbindung der Dependency `org.pcsoft.framework:panelium:0.3.1` inklusive GitHub-Package-Repo-Zugang
* Ersetzen der bisherigen Fensterdekoration durch `ChromePane` (via `PaneliumStage` oder manuell mit `StageStyle.TRANSPARENT`)
* Grundlegende Titelleiste (`ChromeCaptionBar`) ohne Quick-Actions in `captionLeftItems`/`captionCenterItems`/`captionRightItems` (folgen erst in IP-09)
* NICHT enthalten: MenuPane, Datei-Menü, Schnellaktionen

**Affected Areas**

* `app/ui/build.gradle.kts` (bzw. äquivalente Gradle-Datei)
* `MainWindowView.fxml` und zugehörige View-/ViewModel-Klasse
* `ci-pipeline`-Skill prüfen, da neue Dependency mit Auth die Pipeline betreffen kann

**Dependencies**

Keine.

**Expected Result**

Das Hauptfenster wird vollständig über `ChromePane` dargestellt, alte `MenuBar`/`ToolBar` sind entfernt, Fenster-Operationen (Move/Resize/Min/Max/Fullscreen) funktionieren.

**Technical Considerations**

* GitHub-Package-Repository benötigt Authentifizierung; Credentials-Handling muss projektkonform (Gradle-Properties/Secrets) erfolgen
* Vor Umsetzung ist zu prüfen, ob CI-Pipeline (`ci-pipeline`-Skill) Zugriff auf das Package-Repo hat
* Einbindung laut MkDocs-Doku `platinum-chrome/implementation`: `PaneliumStage` oder manuelles `ChromePane(content)` in transparenter `Scene`; FXML unterstützt `<ChromePane>` direkt

### IP-02: MenuPane-Grundgerüst

**Objective**

Anlegen des `FXMenuPane` mit den leeren, aber bereits benannten Reitern Bearbeiten und Publish sowie des separaten Datei-Menüpunkts.

**Scope**

* Struktureller Einbau von `FXMenuPane`/`MenuChromePane` in `ChromePane` (`menuChromePane.menuPane = menuPane`, `menuChromePane.body = content`)
* Leere Reiter "Bearbeiten" und "Publish" als `FXMenuTab(id, title)`, registriert über `menuPane.tabs.addAll(...)`
* Datei-Menüpunkt als eigener Einstiegspunkt (noch ohne Backstage-Inhalt)
* NICHT enthalten: Inhalte der Reiter, Inhalte des Datei-Menüs

**Affected Areas**

* `MainWindowView.fxml`, View-/ViewModel-Klasse
* Message Bundles für Reiter-/Menütitel (`translation`-Skill, `translator`-Agent)

**Dependencies**

IP-01

**Expected Result**

MenuPane ist sichtbar mit den Reitern Bearbeiten/Publish (leer) und einem Datei-Menüpunkt (leer/Platzhalter).

**Technical Considerations**

* Struktur muss so angelegt sein, dass IP-03 bis IP-08 unabhängig voneinander Inhalte ergänzen können
* Laut Doku ist `FXMenuPane` selbst noch "current building block", nicht vollständig fertiggestellt - Funktionsumfang vor Umsetzung anhand aktueller Doku/API gegenprüfen

### IP-03: Reiter Bearbeiten

**Objective**

Befüllen des Reiters Bearbeiten mit den geforderten Gruppen.

**Scope**

* Gruppe Zwischenablage: `FXMenuGroup` mit `FXMenuGroupLargeBox(Einfügen)` als `anchor` sowie `FXMenuGroupSmallBox(Ausschneiden, Kopieren)`
* Gruppe Undo/Redo: `FXMenuGroup` mit zwei `FXMenuGroupLargeBox`-Elementen (Undo, Redo)
* Anbindung an bestehende ViewModel-Aktionen (`viewModel.undoStack`, Zwischenablage-Aktionen)
* NICHT enthalten: Schnellaktionen im ChromePane (IP-09)

**Affected Areas**

* MainWindow-View/ViewModel
* Icons: neue Icons für Ausschneiden, Kopieren, Einfügen (`icon-creator`-Agent, `icons`-Skill)
* Message Bundles

**Dependencies**

IP-02

**Expected Result**

Reiter Bearbeiten zeigt beide Gruppen funktionsfähig an, Undo/Redo-Historie bleibt wie bisher nutzbar.

**Technical Considerations**

* Prüfen, ob eine Zwischenablage-Anbindung (Ausschneiden/Kopieren/Einfügen) bereits an anderer Stelle existiert oder neu geschaffen werden muss
* `priority` (`FXMenuGroupBoxPriority`, Default `MEDIUM`) je Box passend setzen, damit Einklappverhalten bei Platzmangel sinnvoll bleibt

### IP-04: Reiter Publish

**Objective**

Anlegen der Struktur des Publish-Reiters als bewusst leerer Platzhalter.

**Scope**

* Reiter bleibt inhaltlich leer
* Struktur so vorbereiten, dass spätere Features (außerhalb dieses Feature Plans) Inhalte ergänzen können

**Affected Areas**

* MainWindow-View/ViewModel

**Dependencies**

IP-02

**Expected Result**

Reiter Publish ist sichtbar, aber ohne Inhalt.

**Technical Considerations**

Keine besonderen.

### IP-05: Datei-Menü: Neu

**Objective**

Backstage-artige Ansicht für "Neu" mit vordefinierten Profilen.

**Scope**

* Eigene Backstage-Ansicht (Popup/Overlay auf Basis vorhandener panelium-fx-Bausteine, da kein fertiges Backstage-Control existiert)
* Vordefinierte Profile als Auswahl (Detailkonzept der Profile wird in dieser Implementation Plan konkretisiert)
* NICHT enthalten: Öffnen, Speichern, Speichern unter, Preferences/About/Online Doku

**Affected Areas**

* MainWindow-View/ViewModel
* ggf. neues Model unter `lib/model` + FX-Pendant unter `lib/fx-model` für Profile (`fx-model`-Skill, `model-explore`/`model-creator`-Agenten)
* Message Bundles

**Dependencies**

IP-02

**Expected Result**

"Neu" öffnet eine Backstage-Ansicht mit auswählbaren, vordefinierten Profilen; Auswahl legt ein neues Projekt/Kapitel gemäß Profil an.

**Technical Considerations**

* Konzept der Profile (Inhalt, Anzahl, Persistenz) ist zu Beginn dieser Implementation Plan zu konkretisieren

### IP-06: Datei-Menü: Öffnen

**Objective**

Liste der zuletzt geöffneten Dateien im neuen Datei-Menü.

**Scope**

* Wiederverwendung des bestehenden `viewModel.openRecent`-Mechanismus im neuen Datei-Menü
* NICHT enthalten: Neu, Speichern/Speichern unter, Preferences/About/Online Doku

**Affected Areas**

* MainWindow-View/ViewModel

**Dependencies**

IP-02

**Expected Result**

"Öffnen" zeigt im neuen Datei-Menü die bisherige Liste zuletzt geöffneter Dateien in äquivalenter Funktionalität.

**Technical Considerations**

* Bestehendes Datenmodell (`openRecent`) wird unverändert wiederverwendet, nur die Darstellung wechselt

### IP-07: Datei-Menü: Speichern / Speichern unter

**Objective**

Auswahl der zuletzt verwendeten Speicherorte bei Speichern und Speichern unter.

**Scope**

* Neuer Mechanismus zur Nachverfolgung zuletzt verwendeter Speicherorte
* Integration in "Speichern" und "Speichern unter" im neuen Datei-Menü
* NICHT enthalten: Neu, Öffnen, Preferences/About/Online Doku

**Affected Areas**

* MainWindow-View/ViewModel
* ggf. neues Model/Preferences-Feld unter `lib/model` + `lib/fx-model` (`fx-model`-Skill)

**Dependencies**

IP-02

**Expected Result**

"Speichern" und "Speichern unter" bieten eine Auswahl der zuletzt verwendeten Speicherorte an, zusätzlich zur bisherigen Dateiauswahl.

**Technical Considerations**

* Prüfen, ob der bestehende Recent-Files-Mechanismus für "Öffnen" strukturell wiederverwendet werden kann oder ein eigenständiges Modell nötig ist

### IP-08: Datei-Menü: Einmalige Aktionen

**Objective**

Preferences, About und Online Doku als einmalige Aktionen unten im Datei-Menü.

**Scope**

* Menüpunkte Preferences, About, Online Doku im neuen Datei-Menü
* Dialoge/Ziel werden nur so gebaut, dass sie existieren (Preferences-Dialog, About-Dialog als Platzhalter, Online-Doku-Ziel-URL); eine vollständige funktionale Ausgestaltung der Dialoge ist NICHT Teil dieses Features
* NICHT enthalten: Neu, Öffnen, Speichern/Speichern unter

**Affected Areas**

* MainWindow-View/ViewModel
* Neue Icons (About/"i") über `icon-creator`-Agent
* Message Bundles

**Dependencies**

IP-02

**Expected Result**

Preferences, About und Online Doku erscheinen unten im Datei-Menü; die zugehörigen Dialoge/Ziele existieren als Platzhalter, ohne funktionale Verdrahtung.

**Technical Considerations**

* Ziel-URL für Online Doku ist vor Umsetzung zu klären
* Keine Business-Logik-Verdrahtung gemäß Nutzervorgabe

### IP-09: ChromePane-Schnellaktionen

**Objective**

Icon-Schaltflächen Speichern, Separator, Undo, Redo direkt im ChromePane.

**Scope**

* Icon-Buttons in `captionRightItems` (vor den automatisch ergänzten OS-Fensterbuttons) bzw. `captionCenterItems` der `ChromeCaptionBar`
* Anbindung an bestehende ViewModel-Aktionen (`actionSave`, `viewModel.undoStack`)
* Abschließender Schritt des Features gemäß Vorgabe

**Affected Areas**

* MainWindow-View/ViewModel

**Dependencies**

IP-01, IP-03, IP-07

**Expected Result**

ChromePane zeigt Icon-Schaltflächen für Speichern (+ Separator), Undo und Redo, funktional identisch zur bisherigen ToolBar.

**Technical Considerations**

* Platzierung `captionRightItems` vs. `captionCenterItems` anhand des tatsächlichen Erscheinungsbilds bei Umsetzungsbeginn final festlegen

## 7. Dependency Graph

```text
IP-01
├── IP-02
│   ├── IP-03
│   ├── IP-04
│   ├── IP-05
│   ├── IP-06
│   ├── IP-07
│   └── IP-08
└── IP-09 (zusätzlich abhängig von IP-03, IP-07)
```

## 8. Risks and Open Questions

* Konzept der vordefinierten Profile für "Neu" ist inhaltlich noch offen (Klärung zu Beginn von IP-05, laut Nutzervorgabe bewusst später zu konkretisieren)
* Backstage-Ansicht muss vollständig selbst konzipiert werden, da panelium-fx keine fertige Komponente dafür liefert (bestätigt für Version 0.3.1, auch laut `menu-pane/implementation`-Doku)
* `FXMenuPane` ist laut Doku selbst noch nicht vollständig implementiert ("current building block") - Funktionsumfang bei jeder betroffenen Implementation Plan gegenprüfen
* GitHub-Package-Repository-Zugang (Auth) für panelium-fx muss projektkonform eingerichtet werden, inklusive möglicher Auswirkung auf die CI-Pipeline
* Ziel-URL für "Online Doku" ist offen
* Mechanismus für "zuletzt verwendete Speicherorte" (IP-07) ist neu zu konzipieren, ggf. in Anlehnung an den bestehenden Recent-Files-Mechanismus

## 9. Feature Completion Criteria

* Das Hauptfenster läuft vollständig über `ChromePane` und `FXMenuPane`, die alte `MenuBar`/`ToolBar` ist entfernt
* Reiter Bearbeiten zeigt beide Gruppen (Einfügen/Ausschneiden/Kopieren, Undo/Redo) funktionsfähig
* Reiter Publish existiert als leerer Platzhalter
* Das Datei-Menü bietet Neu (mit Profilen), Öffnen (mit Recent-Liste), Speichern/Speichern unter (mit letzten Speicherorten) sowie Preferences/About/Online Doku als einmalige Aktionen
* ChromePane zeigt die Schnellaktionen Speichern, Separator, Undo, Redo mit funktionierender Anbindung
* Alle neuen bzw. geänderten UI-Texte sind über die Message Bundles übersetzt (inkl. Deutsch)
