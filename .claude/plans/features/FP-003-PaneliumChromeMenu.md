# Feature Plan: Panelium Chrome & Menü

## 1. Objective

* Ersetzung der bestehenden JavaFX `MenuBar` und `ToolBar` im Hauptfenster durch panelium-fx
* Einbindung des Panelium `ChromePane` als neuer Fensterrahmen
* Einbindung des Panelium `FXMenuPane` (MenuPane) als neues Datei-/Bearbeiten-/Publish-Menü
* Nutzung der von panelium-fx 0.4.0 bereitgestellten Backstage-Komponente (`FXBackstageMenuPane`) für das Datei-Menü, statt einer eigenen Nachbildung

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
* panelium-fx ist als Dependency noch nicht eingebunden; die verfügbare Version wurde zwischenzeitlich auf `0.4.0` aktualisiert (vorher `0.3.1`)
* Laut MkDocs-Doku (`menu-pane/implementation`, Stand panelium-fx 0.4.0) stellt `FXMenuPane` inzwischen einen eigenen "File"-Tab bereit, der beim Anklicken eine Backstage-Ansicht öffnet (`isFileTabActive`/`fileTabActiveProperty()`, `backstageContent`); als Standardkomponente dafür liefert panelium-fx `FXBackstageMenuPane` mit, bestehend aus einer Menüliste (`items`, `FXBackstageMenuItem`), einem Inhaltsbereich je ausgewähltem Eintrag und Schnellaktionen im Footer (`quickActions`, `FXBackstageQuickAction`); wird `backstageContent` nicht gesetzt, instanziiert `FXMenuPane` beim ersten Öffnen selbst eine Standardinstanz

## 3. Requirements

### Functional Requirements

* Fensterrahmen läuft vollständig über den Panelium `ChromePane`
* MenuPane besitzt zunächst die Reiter "Bearbeiten" und "Publish" sowie den eingebauten "File"-Tab für das Datei-Menü
* Reiter "Bearbeiten": Gruppe Einfügen (groß) + Ausschneiden/Kopieren (klein, untereinander); Gruppe Undo (groß) / Redo (groß)
* Reiter "Publish" bleibt zunächst leer (Platzhalter-Struktur)
* Datei-Menü (Backstage) behält alle bisherigen Funktionen: Neu, Öffnen, Speichern, Speichern unter, Preferences, About, Online Doku
* Neu: vordefinierte Profile als `FXBackstageMenuItem`-Einträge der Backstage; ein Profil ist ein vorgefertigtes Buch mit optional vorausgewählten Buchteilen (`BookPart`-Typen wie Prolog, Kapitel, Epilog); die konkreten Profile sind neu zu erstellen (Detailkonzept folgt in der zugehörigen Implementation Plan)
* Speichern / Speichern unter: Auswahl aus den zuletzt verwendeten Speicherorten, dargestellt im Inhaltsbereich des jeweiligen Backstage-Eintrags; die Speicherorte werden analog zu "Zuletzt geöffnet" (`RecentOpened`) als eigene Liste mitgeführt
* Öffnen: Liste der zuletzt geöffneten Dateien (Wiederverwendung des bestehenden Recent-Mechanismus), dargestellt im Inhaltsbereich des Backstage-Eintrags "Öffnen"
* Preferences, About und Online Doku erscheinen als Schnellaktionen (`FXBackstageQuickAction`) im Footer der Backstage; die zugehörigen Dialoge werden nur so gebaut, dass sie existieren, eine funktionale Verdrahtung ist NICHT Teil dieses Features
* ChromePane erhält Icon-Schaltflächen: Speichern, Separator, Undo, Redo, jeweils mit Anbindung an die bestehende ViewModel-Logik, platziert in `captionLeftItems`
* Der Standard-Fenstertitel der `ChromeCaptionBar` wird ausgeblendet

### Technical Requirements

* Dependency `org.pcsoft.framework:panelium:0.4.0` wird neu eingebunden, inklusive GitHub-Package-Repository-Zugang (`https://maven.pkg.github.com/KleinerHacker/panelium-fx`, GitHub-PAT mit `read:packages`); der Zugang ist bereits eingerichtet
* Die GitHub-Package-Repository-Authentifizierung der CI-Pipeline verantwortet der Nutzer selbst; kein Handlungsbedarf innerhalb dieses Features
* MVVM-FX-Trennung (View/ViewModel) bleibt bestehen, keine Business-Logik in der View
* I18N über bestehende Message Bundles, Übersetzung ausschließlich über den `translator`-Agenten
* Styling ausschließlich über das bestehende zentrale Stylesheet-/Palette-System, keine Inline-Styles
* Fehlende Icons (Ausschneiden, Kopieren, Einfügen, About/"i") werden über den `icon-creator`-Agenten erstellt
* `ui-styling`-Skill gilt für jede Änderung unter `app/ui`
* `fx-component-lifecycle`-Skill gilt, sobald eine neue View globale Listener/Subscriptions registriert
* Laut MkDocs-Doku (`menu-pane/implementation`) ist der `FXMenuPane`-Funktionsumfang selbst noch nicht vollständig implementiert ("current building block") - dies ist bei jeder Implementation Plan, die den MenuPane betrifft, gegenzuprüfen; die Backstage-Komponente (`FXBackstageMenuPane`) ist davon laut Doku als eigenständiger, bereits nutzbarer Baustein zu unterscheiden

## 4. Architecture

* Der bestehende `BorderPane` in `MainWindowView.fxml` verliert `MenuBar` und `ToolBar` im `top`-Bereich
* `ChromePane` übernimmt den Fensterrahmen: Einbindung entweder über `PaneliumStage` oder manuell als `ChromePane(content)` in einer transparenten `Scene` mit `StageStyle.TRANSPARENT`; in FXML als `<ChromePane>`-Wurzel- bzw. Kindelement
* Die Titelleiste (`ChromeCaptionBar`) stellt drei Bereichslisten bereit: `captionLeftItems`, `captionCenterItems`, `captionRightItems` (befüllbar per Kotlin `.add(...)` oder als FXML-Kindelemente); die OS-Fensterbuttons (Min/Max/Close) werden automatisch getrennt davon ergänzt
* `FXMenuPane` wird über `MenuChromePane` eingebettet (`menuChromePane.menuPane = menuPane; menuChromePane.body = content`); `MenuChromePane` legt die Backstage als Overlay über den Body, der angedockte Tab bleibt dabei sichtbar
* MenuPane-Aufbau: `FXMenuPane()` mit `FXMenuTab(id, title)`-Objekten (`menuPane.tabs.addAll(...)`, `menuPane.activate(tab)`) für die Reiter Bearbeiten/Publish; jeder Tab enthält `FXMenuGroup(vararg boxes, anchor = ...)` mit Titel; Boxen sind `FXMenuGroupLargeBox(...)` (große Buttons, z.B. Einfügen, Undo, Redo) bzw. `FXMenuGroupSmallBox(...)` (bis zu drei kleine, vertikal gestapelte Buttons, z.B. Ausschneiden/Kopieren); `anchor` markiert die nie einklappende Box, `priority` (`FXMenuGroupBoxPriority`) steuert das Einklappverhalten bei Platzmangel
* Das Datei-Menü ist der eingebaute "File"-Tab von `FXMenuPane`: Anklicken setzt `isFileTabActive`/`fileTabActiveProperty()` auf `true` und zeigt `backstageContent`; als `backstageContent` wird explizit eine `FXBackstageMenuPane`-Instanz gesetzt (statt der automatischen Standardinstanz), um projektspezifische Einträge zu steuern
* `FXBackstageMenuPane` wird über `items` (Liste von `FXBackstageMenuItem`, je mit `id`, `text`, optionalem Icon und `content`-Node) für Neu/Öffnen/Speichern/Speichern unter befüllt; `selectedItem`/`selectedItemProperty()` steuert die aktuelle Auswahl; über `menuWidth`/`menuWidthProperty()` (Default `300.0`) lässt sich die Breite der Menüliste bei Bedarf anpassen; Preferences/About/Online Doku werden als `quickActions` (`FXBackstageQuickAction`, je mit `id` und `onAction`-Callback) im Footer abgebildet
* Bestehende ViewModel-Bindings (`viewModel.undoStack`, `viewModel.openRecent`, `actionSave`, `actionOpen`, `actionSaveAs`) werden wiederverwendet und nur an die neuen View-Elemente (Reiter-Boxen bzw. `content`-Nodes der Backstage-Einträge) umgehängt, nicht neu konzipiert
* Neue Datenmodelle (letzte Speicherorte, Neu-Profile) werden, sofern nötig, unter `lib/model` mit FX-Pendant unter `lib/fx-model` angelegt (`fx-model`-Skill beachten, `model-explore`/`model-creator`-Agenten nutzen)

## 5. Implementation Plan Overview

| ID    | Implementation Plan | Objective | Dependencies |
| ----- | -------------------- | --------- | ------------ |
| IP-01 | ChromePane-Einbindung | Panelium-Dependency einbinden, ChromePane als Fensterrahmen aktivieren | - |
| IP-02 | MenuPane-Grundgerüst | Leere Reiter Bearbeiten/Publish sowie Backstage-Grundgerüst für den File-Tab anlegen | IP-01 |
| IP-03 | Reiter Bearbeiten | Gruppen Einfügen/Ausschneiden/Kopieren und Undo/Redo im Reiter Bearbeiten füllen | IP-02 |
| IP-04 | Reiter Publish | Struktur des leeren Publish-Reiters anlegen | IP-02 |
| IP-05 | Datei-Menü: Neu | Backstage-Eintrag mit vordefinierten Profilen für Neu | IP-02 |
| IP-06 | Datei-Menü: Öffnen | Liste zuletzt geöffneter Dateien im Backstage-Eintrag Öffnen | IP-02 |
| IP-07 | Datei-Menü: Speichern / Speichern unter | Auswahl der letzten Speicherorte für Speichern und Speichern unter | IP-02 |
| IP-08 | Datei-Menü: Einmalige Aktionen | Preferences, About, Online Doku als Backstage-Schnellaktionen | IP-02 |
| IP-09 | ChromePane-Schnellaktionen | Icon-Schaltflächen Speichern, Separator, Undo, Redo im ChromePane | IP-01, IP-03, IP-07 |

## 6. Implementation Plans

### IP-01: ChromePane-Einbindung

**Objective**

Vollständige Umstellung des Hauptfensters auf den Panelium `ChromePane` als Fensterrahmen.

**Scope**

* Einbindung der Dependency `org.pcsoft.framework:panelium:0.4.0` inklusive GitHub-Package-Repo-Zugang
* Ersetzen der bisherigen Fensterdekoration durch `ChromePane` (via `PaneliumStage` oder manuell mit `StageStyle.TRANSPARENT`)
* Grundlegende Titelleiste (`ChromeCaptionBar`) ohne Quick-Actions in `captionLeftItems`/`captionCenterItems`/`captionRightItems` (folgen erst in IP-09)
* NICHT enthalten: MenuPane, Datei-Menü/Backstage, Schnellaktionen

**Affected Areas**

* `app/ui/build.gradle.kts` (bzw. äquivalente Gradle-Datei)
* `MainWindowView.fxml` und zugehörige View-/ViewModel-Klasse
* `ci-pipeline`-Skill prüfen, da neue Dependency mit Auth die Pipeline betreffen kann

**Dependencies**

Keine.

**Expected Result**

Das Hauptfenster wird vollständig über `ChromePane` dargestellt, alte `MenuBar`/`ToolBar` sind entfernt, Fenster-Operationen (Move/Resize/Min/Max/Fullscreen) funktionieren.

**Technical Considerations**

* GitHub-Package-Repository-Zugang (Auth) ist bereits eingerichtet, Zuständigkeit für CI-seitige Authentifizierung liegt beim Nutzer
* Einbindung laut MkDocs-Doku `panelium-chrome/implementation`: `PaneliumStage` oder manuelles `ChromePane(content)` in transparenter `Scene`; FXML unterstützt `<ChromePane>` direkt

### IP-02: MenuPane-Grundgerüst

**Objective**

Anlegen des `FXMenuPane` mit den leeren, aber bereits benannten Reitern Bearbeiten und Publish sowie des Backstage-Grundgerüsts für den eingebauten File-Tab.

**Scope**

* Struktureller Einbau von `FXMenuPane`/`MenuChromePane` in `ChromePane` (`menuChromePane.menuPane = menuPane`, `menuChromePane.body = content`)
* Leere Reiter "Bearbeiten" und "Publish" als `FXMenuTab(id, title)`, registriert über `menuPane.tabs.addAll(...)`
* Eigene `FXBackstageMenuPane`-Instanz anlegen und als `menuPane.backstageContent` setzen (noch ohne Einträge/Quick-Actions)
* NICHT enthalten: Inhalte der Reiter, Einträge und Quick-Actions der Backstage

**Affected Areas**

* `MainWindowView.fxml`, View-/ViewModel-Klasse
* Message Bundles für Reiter-/Menütitel (`translation`-Skill, `translator`-Agent)

**Dependencies**

IP-01

**Expected Result**

MenuPane ist sichtbar mit den Reitern Bearbeiten/Publish (leer) und einem File-Tab, der eine leere Backstage öffnet.

**Technical Considerations**

* Struktur muss so angelegt sein, dass IP-03 bis IP-08 unabhängig voneinander Inhalte ergänzen können
* Laut Doku ist `FXMenuPane` selbst noch "current building block", nicht vollständig fertiggestellt - Funktionsumfang vor Umsetzung anhand aktueller Doku/API gegenprüfen; `FXBackstageMenuPane` gilt laut Doku als eigenständiger, bereits nutzbarer Baustein

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

Backstage-Eintrag "Neu" mit vordefinierten Profilen.

**Scope**

* Neuer `FXBackstageMenuItem` "Neu" mit eigenem `content`-Node in der Backstage aus IP-02
* Vordefinierte Profile als Auswahl im `content`-Bereich dieses Eintrags: ein Profil ist ein vorgefertigtes Buch mit optional vorausgewählten Buchteilen (`BookPart`-Typen wie Prolog, Kapitel, Epilog); die konkreten Profile werden in dieser Implementation Plan neu erstellt
* NICHT enthalten: Öffnen, Speichern, Speichern unter, Preferences/About/Online Doku

**Affected Areas**

* MainWindow-View/ViewModel
* ggf. neues Model unter `lib/model` + FX-Pendant unter `lib/fx-model` für Profile (`fx-model`-Skill, `model-explore`/`model-creator`-Agenten)
* Message Bundles

**Dependencies**

IP-02

**Expected Result**

Der Backstage-Eintrag "Neu" zeigt auswählbare, vordefinierte Profile (vorgefertigte Bücher mit optional vorausgewählten Buchteilen); Auswahl legt ein neues Projekt gemäß Profil an.

**Technical Considerations**

* Anzahl, Inhalt und genaue Buchteil-Vorauswahl je Profil sowie deren Persistenz sind zu Beginn dieser Implementation Plan zu konkretisieren

### IP-06: Datei-Menü: Öffnen

**Objective**

Liste der zuletzt geöffneten Dateien im Backstage-Eintrag "Öffnen".

**Scope**

* Neuer `FXBackstageMenuItem` "Öffnen" mit eigenem `content`-Node, der den bestehenden `viewModel.openRecent`-Mechanismus darstellt
* NICHT enthalten: Neu, Speichern/Speichern unter, Preferences/About/Online Doku

**Affected Areas**

* MainWindow-View/ViewModel

**Dependencies**

IP-02

**Expected Result**

Der Backstage-Eintrag "Öffnen" zeigt die bisherige Liste zuletzt geöffneter Dateien in äquivalenter Funktionalität.

**Technical Considerations**

* Bestehendes Datenmodell (`openRecent`) wird unverändert wiederverwendet, nur die Darstellung wechselt

### IP-07: Datei-Menü: Speichern / Speichern unter

**Objective**

Auswahl der zuletzt verwendeten Speicherorte bei Speichern und Speichern unter.

**Scope**

* Neues Model "zuletzt verwendete Speicherorte" analog zu `RecentOpened` (eigene Liste mit `max`/`entries`, unveränderliche `add`/`remove`/`clear`-Operationen, Persistenz in `Preferences`), unter `lib/model/pref` mit FX-Pendant unter `lib/fx-model/pref` (`fx-model`-Skill, `model-explore`/`model-creator`-Agenten)
* Neue `FXBackstageMenuItem`-Einträge "Speichern" und "Speichern unter" mit eigenen `content`-Nodes in der Backstage
* NICHT enthalten: Neu, Öffnen, Preferences/About/Online Doku

**Affected Areas**

* MainWindow-View/ViewModel
* Neues Model/Preferences-Feld unter `lib/model` + `lib/fx-model` (`fx-model`-Skill)

**Dependencies**

IP-02

**Expected Result**

Die Backstage-Einträge "Speichern" und "Speichern unter" bieten eine Auswahl der zuletzt verwendeten Speicherorte an, zusätzlich zur bisherigen Dateiauswahl.

**Technical Considerations**

* `RecentOpened` (`lib/model/pref/RecentOpened.kt`) dient als Vorbild für Struktur und Verhalten des neuen Modells (eigene, aber strukturell parallele Liste, kein gemeinsames Modell mit "Öffnen")

### IP-08: Datei-Menü: Einmalige Aktionen

**Objective**

Preferences, About und Online Doku als Backstage-Schnellaktionen.

**Scope**

* Menüpunkte Preferences, About, Online Doku als `FXBackstageQuickAction`-Einträge im Footer der Backstage aus IP-02
* Dialoge/Ziel werden nur so gebaut, dass sie existieren (Preferences-Dialog, About-Dialog als Platzhalter, Online-Doku-Ziel-URL); eine vollständige funktionale Ausgestaltung der Dialoge ist NICHT Teil dieses Features
* NICHT enthalten: Neu, Öffnen, Speichern/Speichern unter

**Affected Areas**

* MainWindow-View/ViewModel
* Neue Icons (About/"i") über `icon-creator`-Agent
* Message Bundles

**Dependencies**

IP-02

**Expected Result**

Preferences, About und Online Doku erscheinen als Quick-Actions im Footer der Backstage; die zugehörigen Dialoge/Ziele existieren als Platzhalter, ohne funktionale Verdrahtung.

**Technical Considerations**

* Ziel-URL für Online Doku ist vor Umsetzung zu klären
* Keine Business-Logik-Verdrahtung gemäß Nutzervorgabe

### IP-09: ChromePane-Schnellaktionen

**Objective**

Icon-Schaltflächen Speichern, Separator, Undo, Redo direkt im ChromePane.

**Scope**

* Icon-Buttons in `captionLeftItems` der `ChromeCaptionBar`
* Ausblenden des Standard-Fenstertitels (`isDefaultTitleVisible` / `defaultTitleVisibleProperty()` auf `false`)
* Anbindung an bestehende ViewModel-Aktionen (`actionSave`, `viewModel.undoStack`)
* Abschließender Schritt des Features gemäß Vorgabe

**Affected Areas**

* MainWindow-View/ViewModel

**Dependencies**

IP-01, IP-03, IP-07

**Expected Result**

ChromePane zeigt Icon-Schaltflächen für Speichern (+ Separator), Undo und Redo in `captionLeftItems`, funktional identisch zur bisherigen ToolBar; der Standard-Fenstertitel ist ausgeblendet.

**Technical Considerations**

* Laut MkDocs-Doku (`panelium-chrome/implementation`) wird der Titel über `isDefaultTitleVisible` (bzw. `*Property()`) deaktiviert, in FXML über `defaultTitleVisible="false"` am `ChromePane`

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

* Anzahl, Inhalt und Buchteil-Vorauswahl der vordefinierten Profile für "Neu" sind inhaltlich noch offen (Klärung zu Beginn von IP-05); Profile sind vorgefertigte Bücher mit optional vorausgewählten Buchteilen (`BookPart`-Typen)
* Die Backstage-Ansicht wird seit panelium-fx 0.4.0 nicht mehr selbst konzipiert, sondern über die mitgelieferte Komponente `FXBackstageMenuPane` (Einträge `FXBackstageMenuItem`, Footer-Aktionen `FXBackstageQuickAction`) umgesetzt; dies korrigiert die frühere Annahme (Version 0.3.1), panelium-fx liefere dafür keine fertige Komponente
* `FXMenuPane` ist laut Doku selbst noch nicht vollständig implementiert ("current building block") - Funktionsumfang bei jeder betroffenen Implementation Plan gegenprüfen; die Backstage-Komponente gilt laut Doku als eigenständiger, bereits nutzbarer Baustein
* GitHub-Package-Repository-Zugang (Auth) für panelium-fx ist bereits eingerichtet
* Ziel-URL für "Online Doku" ist offen
* Der CI-Pipeline-Zugriff auf das GitHub-Package-Repository liegt in der Verantwortung des Nutzers und ist kein offener Punkt dieses Features

## 9. Feature Completion Criteria

* Das Hauptfenster läuft vollständig über `ChromePane` und `FXMenuPane`, die alte `MenuBar`/`ToolBar` ist entfernt
* Reiter Bearbeiten zeigt beide Gruppen (Einfügen/Ausschneiden/Kopieren, Undo/Redo) funktionsfähig
* Reiter Publish existiert als leerer Platzhalter
* Das Datei-Menü (Backstage über `FXBackstageMenuPane`) bietet Neu (mit Profilen), Öffnen (mit Recent-Liste), Speichern/Speichern unter (mit letzten Speicherorten) sowie Preferences/About/Online Doku als Footer-Schnellaktionen
* ChromePane zeigt die Schnellaktionen Speichern, Separator, Undo, Redo in `captionLeftItems` mit funktionierender Anbindung
* Der Standard-Fenstertitel im ChromePane ist ausgeblendet
* Alle neuen bzw. geänderten UI-Texte sind über die Message Bundles übersetzt (inkl. Deutsch)
