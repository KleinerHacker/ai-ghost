# IP-02: Provider-Module und Stub-Provider

## Herkunft

* Feature Plan: `.claude/plans/features/FP-002-AiProviderPlugins.md`
* Plan-ID im Feature Plan: IP-02
* Status-Datei des Features: `.claude/plans/features/FP-002-AiProviderPlugins-status.md`

## Abhängigkeiten

* Voraussetzung: IP-01
* Start erst, wenn IP-01 im Feature-Status `COMPLETED` ist.
* Blockiert: IP-03
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `project-docs`

## Aufgaben

### 1. Container und Stub-Modul

* Container `lib/plugin/provider` anlegen.
* `lib/plugin/provider/stub` als `ai-ghost-provider-stub` in `settings.gradle.kts` eintragen.
* Abhängigkeit nur `ai-ghost-plugin-api`; kein JavaFX.
* `.claude/rules/architecture.md` um `lib/plugin/provider/*` ergänzen.

### 2. Provider-Modul-Baustein

* Wiederverwendbaren Gradle-Baustein (Convention-Plugin bzw. `buildSrc`) für ein Provider-Modul erstellen.
* Baustein erzeugt ein Plugin-JAR mit Service- bzw. Manifest-Eintrag zur Entdeckung.
* IP-06 bis IP-09 verwenden denselben Baustein.

### 3. Stub-Provider

* `StubProvider` mit `@AiProviderInfo(id = "stub", …, contractVersion = aktuell)`.
* `StubProviderConfig`: `responseText: String` mit Vorgabe, `chunkDelayMillis: Int` mit Vorgabe.
* Beide Felder `@AiProviderConfigField`; `responseText` als mehrzeiliger Text.
* `generate()` streamt `responseText` in Chunks, ignoriert die Prompts, honoriert `cancel()`.
* `en`- und `de`-`ResourceBundle` für `label` und `help`.

### 4. Auslieferung

* Build-Verdrahtung: das Stub-JAR in das eingebaute `plugins/`-Verzeichnis des jlink-Image legen.
* Denselben Pfad für den lokalen Lauf ohne Image auffindbar machen.

### 5. Tests

* Der Stub wird vom Manager aus dem eingebauten Verzeichnis entdeckt.
* `List<ConfigField>` enthält `responseText` und `chunkDelayMillis` mit korrektem `ConfigFieldType`.
* `generate()` liefert den erwarteten Chunk-Strom; `cancel()` bricht ab.
* Bundle-Auflösung für `en` und `de`.

### 6. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-002-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Der Stub-Provider steht nach dem Start in der Registrierung und streamt seinen konfigurierten Text.
* Der Provider-Modul-Baustein steht für IP-06 bis IP-09 bereit.
