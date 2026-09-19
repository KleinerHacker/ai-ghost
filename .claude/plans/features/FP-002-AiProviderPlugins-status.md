# Feature-Status: AI Provider Plugins

Status: IN_PROGRESS

## Implementierungspläne

| ID    | Implementierungsplan                              | Status      |
|-------|--------------------------------------------------|-------------|
| IP-01 | Plugin API And Plugin Manager                     | COMPLETED   |
| IP-02 | Provider Modules And Stub Provider                | NOT_STARTED |
| IP-03 | Provider Selection, Configuration And Persistence | NOT_STARTED |
| IP-04 | Plugin Packaging And CI                           | NOT_STARTED |
| IP-05 | Documentation                                     | NOT_STARTED |
| IP-06 | LM Studio Provider                                | NOT_STARTED |
| IP-07 | OpenAI API Provider                               | NOT_STARTED |
| IP-08 | Anthropic API Provider                            | NOT_STARTED |
| IP-09 | llama.cpp Provider                                | NOT_STARTED |

## Gesamtfortschritt

11% (1/9)

## Anmerkungen

Jeder Plan ist atomar und endet mit einem vollständig nutzbaren Anwendungsfall. Der Kern (IP-01 bis
IP-05) ist ohne echte LLM-Verbindung vollständig; der Nachweis läuft über den Stub-Provider aus
IP-02, die Autoren-Anleitung über den Stub aus IP-05. IP-06 bis IP-09 sind voneinander unabhängig,
hängen nur an IP-03 und sind einzeln und später umsetzbar.

Getroffene Entscheidungen:

* Plugin-seitige API im bestehenden `ai-ghost-plugin-api`; kein weiteres plugin-seitiges Modul.
* Manager im neuen Modul `lib/plugin/manager` (`ai-ghost-plugin-manager`); Isolation über **einen
  `URLClassLoader` je Plugin**, keine JPMS-Modulschicht; Reflection der Provider-Konfiguration mit
  **`kotlin-reflect`**. Der Manager bekommt die Verzeichnisse übergeben. Kette: `app/ui` →
  `ai-ghost-plugin-manager` → `ai-ghost-plugin-api`.
* **Provider-Entdeckung läuft ausschließlich über das Manifest, keine Annotation, kein
  `ClassGraph`/`ServiceLoader`-Scan.** `PluginManifest.aiProviders` (aus `providers.ai` der YAML)
  nennt je Provider `id`, `implementation` (vollqualifizierter Klassenname), `name` und
  `contractVersion` direkt; der Manager lädt nur die genannte Klasse über
  `Class.forName(name, false, classLoader)` und prüft sie über den wiederverwendbaren
  `PluginClassRequirements.load(className, classLoader, requiredType, requiredAnnotations)` (MUSS
  ein Interface implementieren, MUSS optionale Pflicht-Annotationen tragen). `AiProviderInfo` als
  Annotation entfällt ersatzlos. `ai-ghost-plugin-manager` hat dadurch keine `ClassGraph`-Abhängigkeit.
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
  eigenständig versioniert. Kompatibilität über eine **Vertragsversion je `providers.ai`-Eintrag im
  Manifest** (`AiProviderDeclaration.contractVersion`), vom Manager gegen
  `AiProviderContract.VERSION` geprüft, steigt nur bei einem Bruch des Provider-Vertrags.
* IP-05 (Documentation): MkDocs-Menüpunkt „Plugins“ mit Übersichtsseite und Bereich „AI Provider“
  (API, Entwicklung, Beispiel Stub).
* **Nicht Teil des Features:** die Orchestrierung, das Verdrahten der `TODO("AI action: …")`-Rümpfe,
  eine echte LLM-Verbindung als Nachweis. `lib/ai` bleibt unberührt.

Weiterhin offen (Abschnitt 9): die Drittanbieter-Abhängigkeiten der Anbindungen inkl. nativer
llama.cpp-Bindung und einer etwaigen Schlüsselbund-Bibliothek (vor IP-03 bzw. IP-06..09); Name/Umfang
der `verifyPlugin`-Aufgabe (IP-04).
