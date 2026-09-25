# Feature-Status: AI Provider Plugins

Status: IN_PROGRESS

## Implementierungspläne

| ID    | Implementierungsplan                              | Status      |
|-------|--------------------------------------------------|-------------|
| IP-01 | Plugin API And Plugin Manager (ersetzt durch IP-10) | COMPLETED |
| IP-10 | Pluggiat-Migration                                | COMPLETED   |
| IP-02 | Provider Modules And Stub Provider                | NOT_STARTED |
| IP-03 | Provider Selection, Configuration And Persistence | NOT_STARTED |
| IP-04 | Plugin Packaging And CI                           | NOT_STARTED |
| IP-05 | Documentation                                     | NOT_STARTED |
| IP-06 | LM Studio Provider                                | NOT_STARTED |
| IP-07 | OpenAI API Provider                               | NOT_STARTED |
| IP-08 | Anthropic API Provider                            | NOT_STARTED |
| IP-09 | llama.cpp Provider                                | NOT_STARTED |

## Gesamtfortschritt

20% (2/10)

## Anmerkungen

Jeder Plan ist atomar und endet mit einem vollständig nutzbaren Anwendungsfall. Der Kern (IP-01 bis
IP-05) ist ohne echte LLM-Verbindung vollständig; der Nachweis läuft über den Stub-Provider aus
IP-02, die Autoren-Anleitung über den Stub aus IP-05. IP-06 bis IP-09 sind voneinander unabhängig,
hängen nur an IP-03 und sind einzeln und später umsetzbar.

Getroffene Entscheidungen:

* Plugin-seitige API im bestehenden `ai-ghost-plugin-api`; kein weiteres plugin-seitiges Modul.
* **Loader, Discovery und Isolation kommen aus `pluggiat` (extern), nicht aus Eigenbau** – IP-10 ist
  **COMPLETED**. `lib/plugin/system` (`ai-ghost-plugin-system`) ist jetzt eine dünne Integration
  auf pluggiats `PluginManager`: der eigene `URLClassLoader`-Aufbau, das eigene Manifest-Schema
  (`PluginManifest`/`PluginManifestReader`) und `PluginClassRequirements` sind vollständig entfallen
  zugunsten von pluggiats eigenem `META-INF/plugin.yml` (`extensions.ai`-Schlüssel, von pluggiat
  selbst geparst und validiert) und dessen Sicherheitsstrategie (`InsecureSecurityStrategy` je Ort,
  `SingleJarScanStrategy` als Scan-Strategie). Reflection der Provider-Konfiguration bleibt bei
  **`kotlin-reflect`** (`ConfigModelReader`, app-eigen, unverändert). Der Manager bekommt die
  Verzeichnisse übergeben. Kette: `app/ui` → `ai-ghost-plugin-system` → `pluggiat` +
  `ai-ghost-plugin-api`. `lib/plugin/system` benötigt zusätzlich das Gradle-Plugin
  `org.javamodularity.moduleplugin` (bereits über `app/ui` im Repo etabliert), da pluggiats JAR
  keinen `Automatic-Module-Name` trägt und sonst nicht auf den JPMS-Modulpfad gelangt.
* **Provider-Entdeckung läuft ausschließlich über das Manifest** (pluggiats `extensions.ai`), keine
  Annotation, kein `ClassGraph`/`ServiceLoader`-Scan. `AiProviderInfo` als Annotation entfällt
  ersatzlos; `contractVersion` bleibt je Provider-Eintrag im Manifest, transportiert über ein eigenes
  Feld auf `AiProviderExtensionConfig`. Das Manifest braucht zusätzlich ein von pluggiat intern
  verlangtes, in der öffentlichen Doku nicht gelistetes Pflichtfeld `$version` (Migrationszweck) –
  Provider-Autoren müssen das in der Anleitung (IP-05) erfahren.
* **Tests decken nur unseren eigenen Code ab, nicht pluggiat**: Ein ursprünglich für IP-10 gebauter
  Satz aus Fixture-Plugin-JARs, der pluggiats Scan/Load/Classloader-Isolation end-to-end nachtestete,
  wurde wieder entfernt – das ist pluggiats eigene, bereits getestete Verantwortung. `lib/plugin/system`
  testet nur noch `AiProviderRegistry` (Id-Kollision, `contractVersion`-Prüfung) und
  `ConfigModelReader` direkt, ohne über pluggiat zu laden.
* **Weitere Vereinfachung von IP-10:** `lib/plugin/system` enthält jetzt nur noch
  `AiProviderExtensionConfig` (die pluggiat-Extension-Point-Konfiguration für `"ai"`). Der eigene
  `PluginManager`-Wrapper und `PluginLoadException` sind ersatzlos entfallen; `AiProviderRegistry` und
  `ConfigModelReader` sind nach `app/ui` (Paket `org.pcsoft.app.aighost.app.plugin`) umgezogen.
  `app/ui`s `PluginLoadStartupStep` baut pluggiats `PluginManagerConfiguration`/`PluginManager` jetzt
  selbst auf und ruft direkt `AiProviderRegistry.buildFrom(pluginManager)` auf – kein Umweg mehr über
  `lib/plugin/system`. Kette: `app/ui` → `pluggiat` + `ai-ghost-plugin-api`, mit
  `ai-ghost-plugin-system` nur noch als Träger der Extension-Point-Deklaration.
* Zwei Plugin-Orte: eingebautes `plugins`-Verzeichnis im Installationsverzeichnis (fix, aufgelöst aus
  `java.home` des laufenden jlink-Images) und ein Nutzer-Verzeichnis (Vorgabe `~/.ai-ghost/plugins`),
  über `Preferences`/UI änderbar (IP-03), Wirkung beim nächsten Start. **Id-Kollision: „zuerst
  geladen gewinnt“** – eingebaut vor Nutzer, bei zwei Nutzer-Plugins das zuerst geladene. Der
  Startschritt `PluginLoadStartupStep` ermittelt beide in `app/ui` und rangiert hinter
  `PreferencesStartupStep`.
* **Je mitgeliefertem Provider ein eigenes Modul** unter dem neuen Container `lib/plugin/provider`
  mit eigenem Plugin-JAR: `ai-ghost-provider-stub` (voll, IP-02), `-lm-studio`, `-openai`,
  `-anthropic`, `-llama-cpp` (Modell + UI hier, Client in IP-06..IP-09). Kein geteilter Klassenpfad;
  die Abhängigkeit eines Providers ist im Modul-JAR und durch den `URLClassLoader` isoliert. Der
  Stub ersetzt den früheren „Echo“ und bleibt dauerhaft.
* Provider-Konfiguration als annotiertes Datenmodell (`AiProviderConfig` + `@AiProviderConfigField`)
  bzw. `configSchema()`; interner Vertrag `List<ConfigField>` in `ai-ghost-plugin-api`; die UI
  verarbeitet nur diese Liste. **Feldtypmenge `STRING`..`ENUM` reicht** für alle fünf Provider.
* **i18n:** wird in IP-03 umgesetzt; `label`/`help` gegen ein plugin-eigenes `ResourceBundle`
  (Fallback Schlüssel-Klartext), mitgelieferte Provider mit `en` und `de`; App-Bundles nie
  beteiligt.
* **Geheimnis-Speicher:** `SecretStore` nach dem Muster von IntelliJ PasswordSafe – OS-Schlüsselbund
  primär, verschlüsselte Datei als Fallback, schmale Schnittstelle, zunächst klein.
* **Versionierung:** alle Artefakte tragen die Repo-Version; `ai-ghost-plugin-api` nicht
  eigenständig versioniert. Kompatibilität über eine **Vertragsversion je `extensions.ai`-Eintrag im
  Manifest** (`AiProviderExtensionConfig.contractVersion`), von `AiProviderRegistry` gegen
  `AiProviderContract.VERSION` geprüft, steigt nur bei einem Bruch des Provider-Vertrags.
* IP-05 (Documentation): MkDocs-Menüpunkt „Plugins“ mit Übersichtsseite und Bereich „AI Provider“
  (API, Entwicklung, Beispiel Stub).
* **Nicht Teil des Features:** die Orchestrierung, das Verdrahten der `TODO("AI action: …")`-Rümpfe,
  eine echte LLM-Verbindung als Nachweis. `lib/ai` bleibt unberührt.

Weiterhin offen (Abschnitt 9): die Drittanbieter-Abhängigkeiten der Anbindungen inkl. nativer
llama.cpp-Bindung und einer etwaigen Schlüsselbund-Bibliothek (vor IP-03 bzw. IP-06..09); Name/Umfang
der `verifyPlugin`-Aufgabe (IP-04).
