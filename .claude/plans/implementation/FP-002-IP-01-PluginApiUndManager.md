# IP-01: Plugin-API und Plugin-Manager

## Herkunft

* Feature Plan: `.claude/plans/features/FP-002-AiProviderPlugins.md`
* Plan-ID im Feature Plan: IP-01
* Status-Datei des Features: `.claude/plans/features/FP-002-AiProviderPlugins-status.md`

## Abhängigkeiten

* Voraussetzung: keine
* Blockiert: IP-02 (und darüber alle weiteren)
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `startup`
* `testing`
* `project-docs`

## Aufgaben

### 1. Plugin-API erweitern

* In `ai-ghost-plugin-api` das Paket `org.pcsoft.app.aighost.plugin.api.provider` anlegen und exportieren.
* `AiProvider`: `generate(systemPrompt, userPrompt, callback): AiProviderHandle`, `configType`, `configSchema(): List<ConfigField>? = null`.
* `AiProviderCallback` mit `onChunk`, `onComplete`, `onError`; `AiProviderHandle` mit `cancel()`.
* `@AiProviderInfo(id, name, contractVersion)`, Marker `AiProviderConfig`.
* `@AiProviderConfigField(label, help, secret, required, order)` als Property-Annotation, `RUNTIME`.
* `ConfigField` und `enum ConfigFieldType { STRING, BOOLEAN, INT, LONG, DOUBLE, ENUM }`.
* Abhängigkeit bleibt `kotlin.stdlib`; `module-info.java` um den Export ergänzen.

### 2. Manager-Modul anlegen

* `lib/plugin/manager` als `ai-ghost-plugin-manager` in `settings.gradle.kts` eintragen.
* Abhängigkeiten: `ai-ghost-plugin-api`, ClassGraph, `kotlin-reflect`; kein JavaFX.
* `module-info.java` mit `requires` und `exports` für das Manager-Paket.
* Zeile in `.claude/rules/architecture.md` um `lib/plugin/manager` ergänzen.

### 3. Entdeckung und isoliertes Laden

* `PluginLoader` baut je Plugin-JAR einen eigenen `URLClassLoader`.
* `AiProvider`-Implementierungen je Plugin über ClassGraph bzw. Service-Eintrag finden und instanziieren.
* Vertragsversion aus `@AiProviderInfo` prüfen; unpassende Plugins ablehnen.
* `AiProviderRegistry` füllen; bei Id-Kollision behält der zuerst geladene Eintrag Vorrang.
* Fehler je Plugin fangen, melden, Plugin überspringen; der Start läuft weiter.

### 4. Konfigurations-Reader

* `ConfigModelReader` liefert je Provider eine `List<ConfigField>`.
* `configSchema()` bevorzugen; sonst `configType` per `kotlin-reflect` auswerten.
* Nicht unterstützten Feldtyp als Fehler melden, Provider als fehlkonfiguriert überspringen.
* Instanz aus `Map<String, Any?>` befüllen und wieder auslesen, ohne Jackson.

### 5. Startschritt

* `PluginLoadStartupStep` in `org.pcsoft.app.aighost.app.startup.step`, konkrete Klasse mit no-arg-Konstruktor.
* `@StartupOrder` > 0, damit er hinter `PreferencesStartupStep` läuft.
* Verzeichnisliste ermitteln, dem Manager übergeben; Fehler über `StartupContext.onFxThread` melden.
* `app/ui` `module-info.java`: `requires org.pcsoft.app.aighost.plugin.manager`, Step-Paket für ClassGraph offen halten.

### 6. Verzeichnisermittlung

* Eingebautes Verzeichnis `<Installationsverzeichnis>/plugins` aus dem jpackage-/jlink-Image-Layout auflösen.
* Nutzer-Verzeichnis hier fest auf `~/.ai-ghost/plugins` (Konfigurierbarkeit in IP-03).
* Ein fehlendes Verzeichnis überspringen, nicht als Fehler behandeln.
* Eingebautes Verzeichnis zuerst in die Liste stellen.

### 7. Tests

* Fixture-Plugin-JARs im Testquellsatz von `ai-ghost-plugin-manager`.
* Entdeckung aus beiden Verzeichnissen, getrennte `URLClassLoader`, Registrierung.
* Vertragsversions-Prüfung, defektes Plugin übersprungen, `ConfigModelReader` je Feldtyp.
* Vorrang des eingebauten Verzeichnisses; zwei Nutzer-Plugins gleicher Id → zuerst geladenes gewinnt.
* Startschritt: no-arg-Konstruktor, `@StartupOrder`, `onFxThread`-Marshalling, Fehlerpfad.

### 8. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen; MkDocs bleibt bei IP-05.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-002-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Plugins aus beiden Verzeichnissen stehen nach dem Start in der Registrierung; defekte werden übersprungen.
* Die Kette bis zur Registrierung steht; UI und Persistenz folgen in IP-02 und IP-03.
