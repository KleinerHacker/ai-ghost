# IP-01: ChromePane-Einbindung

## Herkunft

* Feature Plan: `.claude/plans/features/FP-003-PaneliumChromeMenu.md`
* Plan-ID im Feature Plan: IP-01
* Status-Datei des Features: `.claude/plans/features/FP-003-PaneliumChromeMenu-status.md`

## Abhängigkeiten

* Voraussetzung: keine
* Blockiert: IP-02 (und darüber alle weiteren), zusätzlich IP-09
* Reihenfolge und Graph stehen in Abschnitt 7 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `ci-pipeline`
* `testing`
* `project-docs`

## Aufgaben

### 1. Dependency einbinden

* `org.pcsoft.framework:panelium:0.4.0` in `app/ui/build.gradle.kts` ergänzen.
* GitHub-Package-Repository `https://maven.pkg.github.com/KleinerHacker/panelium-fx` als Maven-Repo eintragen.
* Vorhandenen GitHub-PAT-Zugang (`read:packages`) für den lokalen Build nutzen.
* `module-info.java` von `app/ui` um `requires` für panelium-fx ergänzen.

### 2. ChromePane einbauen

* `MenuBar` und `ToolBar` aus dem `top`-Bereich des `BorderPane` in `MainWindowView.fxml` entfernen.
* `ChromePane` als Fensterrahmen einbauen, laut MkDocs `panelium-chrome/implementation`.
* Entscheidung `PaneliumStage` vs. manuelles `ChromePane(content)` treffen und umsetzen.
* Bei manueller Einbindung `Scene` mit `StageStyle.TRANSPARENT` verwenden.

### 3. Titelleiste-Grundgerüst

* `ChromeCaptionBar` ohne Inhalte in `captionLeftItems`/`captionCenterItems`/`captionRightItems` belassen.
* Fenster-Operationen Move/Resize/Min/Max/Fullscreen manuell verifizieren.
* OS-Fensterbuttons (Min/Max/Close) auf korrekte automatische Anzeige prüfen.

### 4. CI-Pipeline prüfen

* `ci-pipeline`-Skill laden und Auswirkung der neuen authentifizierten Dependency auf `ci.yml`/`release.yml` prüfen.
* Repository-Zugang in der Pipeline nur dokumentieren, Umsetzung bleibt beim Nutzer.

### 5. Tests

* Bestehende UI-Tests auf Referenzen zu `MenuBar`/`ToolBar` durchsuchen und anpassen.
* `testing`-Skill beachten; Smoke-Test für den `ChromePane`-Aufbau ergänzen, falls sinnvoll.

### 6. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-003-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Das Hauptfenster läuft vollständig über `ChromePane`, alte `MenuBar`/`ToolBar` sind entfernt.
* Fenster-Operationen funktionieren; MenuPane und Schnellaktionen folgen in IP-02 und IP-09.
