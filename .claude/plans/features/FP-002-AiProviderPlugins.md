# Feature-Plan: AI Provider Plugins

> Nur zur Orientierung. Jede Aufgabe, Einschränkung und jeder Test gehört zu den
> Implementierungsplänen unter `.claude/plans/implementation`; ein `FP-002-Overview.md` listet sie,
> sobald die Pläne ausgeschrieben werden, und Abschnitt 7 benennt die Datei jedes einzelnen.
> Der Fortschritt steht in `FP-002-AiProviderPlugins-status.md`, nicht hier.
>
> Die Pläne sind vertikal geschnitten und **atomar**: jeder Plan ist als Ganzes umsetzbar, endet mit
> einem vollständig nutzbaren Anwendungsfall und hinterlässt keine halbe Verkabelung.

## 1. Ziel

Die Anwendung soll ihr zugrundeliegendes LLM über austauschbare Plugins ansprechen. Ein
**AI Provider** ist ein Plugin, das die AI-API der Anwendung erfüllt: Es bekommt einen System-Prompt
und einen normalen User-Prompt und liefert Text zurück – als Strom, abbrechbar, mit Fehlermeldung.

Im Plugin findet **keine Orchestrierung** statt. Es baut keine Prompts, kennt weder Absatz noch
Kapitel noch `AiActionRequest`, es prüft keine Zeichenlimits und es zerlegt keine Antwort in Absätze.
Das Plugin ist die reine Verbindung zum Modell.

**Nicht Teil dieses Feature-Plans:**

* die Orchestrierung selbst – die Abbildung `AiActionRequest` → (System-Prompt, User-Prompt), die
  Limitprüfung, das Streaming in die Schreibfläche, das Aufteilen der Antwort. Das leistet ein
  eigenes, späteres Feature.
* das Verdrahten der `TODO("AI action: …")`-Rümpfe in `app/ui`. Sie bleiben nach diesem Feature
  unverändert stehen; das Orchestrierungs-Feature verdrahtet sie.
* eine echte Verbindung zu einem LLM als Nachweis. Die Plugin-Funktionalität wird allein über einen
  **Stub-Provider** nachgewiesen. Die konkrete Anbindung an LM Studio, OpenAI, Anthropic und
  llama.cpp liegt in eigenen, atomaren Plänen (IP-06 bis IP-09), die ans Ende gehängt sind und den
  Kern des Features nicht voraussetzen.

Was dieses Feature liefert: die Plugin-API, den Plugin-Manager (Scan, isoliertes Laden,
Registrierung), die generische Konfigurations-Oberfläche samt Persistenz, je mitgeliefertem Provider
ein eigenes Modul unter `lib/plugin/provider`, einen voll funktionierenden Stub-Provider als
Nachweis und eine MkDocs-Anleitung zur Entwicklung eigener AI-Provider-Plugins.

## 2. Aktueller Stand

* `lib/plugin/api` (`ai-ghost-plugin-api`) – trägt nur `ProjectPart` und `ProjectPartInfo`. Keine
  Plugin-Schnittstelle, kein Loader, keine Registrierung, kein Lebenszyklus. Abhängigkeit
  ausschließlich `kotlin.stdlib`; das Artefakt ist als öffentliche Plugin-Autoren-API gedacht.
* `lib/ai` (`ai-ghost-ai`) – trägt den Aktions-Port aus dem abgeschlossenen Feature Paper Writing
  Surface (ehem. IP-17): `AiAction`, `AiActionRequest`, `AiActionCallback`, `AiActionHandle`,
  `AiActionError`, `AiActionLimits`, `ParagraphSplitter`, `TokenUtils`. **Keine Implementierung** von
  `AiAction`. Dieses Feature **ändert `lib/ai` nicht** – die Implementierung ist Sache des
  Orchestrierungs-Features.
* `app/ui` – die KI-Schaltflächen rufen leere `*View`-Methoden mit `TODO("AI action: …")`. Umgesetzt
  ist nur `InspectorView.generatePart()` (abgeschlossenes Feature Paper Writing Surface, ehem. IP-19).
  Dieses Feature lässt sie unberührt.
* Startbereich – `Startup.discoverSteps()` scannt `org.pcsoft.app.aighost.app.startup.step` mit
  ClassGraph. Der `startup`-Skill nennt „Plugins laden“ als künftigen Schritt. Heute nur
  `PreferencesStartupStep`.
* Einstellungen – `Preferences` als YAML unter `~/.ai-ghost/preferences.yml`. Gruppe `Ai` trägt nur
  `maxStoryCharacters`, `maxStyleCharacters`. Keine Provider-Auswahl, keine Zugangsdaten, kein
  Plugin-Verzeichnis.
* `.claude/rules/architecture.md` – kennt `lib/plugin` noch nicht als eigenen Baustein; unter
  `lib/plugin` liegt heute nur `api`.
* Doku – MkDocs unter `docs/docs`; es gibt noch keinen Menüpunkt „Plugins“.
* CI – `.github/workflows/ci.yml` und `release.yml`; der `ci-pipeline`-Skill nennt einen Schritt
  „Verify plugin“, für den es heute keine Gradle-Aufgabe gibt.
* Versionen – alle Artefakte des Repos tragen dieselbe Version aus dem Release; es gibt kein Modul
  mit eigener Versionierung.

Fehlt: jede Plugin-Schnittstelle für einen Provider, ein annotiertes Konfigurationsmodell samt
seiner Auswertung, die zwei Plugin-Orte (eingebautes `plugins`-Verzeichnis, einstellbares
Nutzer-Verzeichnis), ein Loader mit Isolation, eine Provider-Registrierung, ein Startschritt, die
Persistenz einer Provider-Wahl samt Geheimnissen und des Verzeichnispfads, eine Auswahl- und
Konfigurations-Oberfläche, der Container `lib/plugin/provider` samt Stub-Modul, die MkDocs-Anleitung
und die vier konkreten Anbindungen.

## 3. Zielzustand

* **Plugin-API.** Jede für Plugin-Implementierungen bereitgestellte API liegt im **bereits
  vorhandenen** Modul `ai-ghost-plugin-api`; es entsteht kein weiteres plugin-seitiges Modul.
  `ai-ghost-plugin-api` trägt zusätzlich zu `ProjectPart` einen Provider-Vertrag: eine Schnittstelle,
  die einen System-Prompt und einen User-Prompt entgegennimmt und einen Textstrom zurückgibt (Chunks,
  Abschluss, Fehler, Abbruch), eine `@AiProviderInfo`-Kennzeichnung (stabile Id, Anzeigename,
  Vertragsversion) und die Nennung des Konfigurationsmodells. Weiterhin keine Abhängigkeit über
  `kotlin.stdlib` hinaus. Registrierung, Loader, Selektion kommen **nicht** hinein.
* **Konfigurationsmodell mit Annotations.** Ein Provider liefert seine Konfiguration als schlichte
  Kotlin-Klasse (`AiProviderConfig`-Marker, argumentloser Konstruktor bzw. durchgängige
  Vorgabewerte). Jede Formular-Eigenschaft trägt `@AiProviderConfigField` mit Beschriftung,
  Hilfetext, `secret`, `required`, `order`. Feldtyp und Vorgabe kommen aus der Eigenschaft.
  Unterstützte Feldtypen: `String`, `Boolean`, `Int`, `Long`, `Double`, `enum` (Auswahlfeld) – das
  reicht für alle fünf mitgelieferten Provider. Der Provider nennt seine Klasse über
  `AiProvider.configType`; der Plugin-Manager liest sie bei der Entdeckung.
* **`ConfigField`-Liste als interner Vertrag.** Der annotierte Weg ist nur eine Art, eine
  `List<ConfigField>` (Name, `ConfigFieldType`, Vorgabe, Beschriftung, Hilfetext, `secret`,
  `required`, `order`) zu erzeugen. `ConfigField` und `ConfigFieldType` liegen in
  `ai-ghost-plugin-api`; ein Provider, dem die Annotations nicht reichen (berechnete Werte, bedingte
  Felder), überschreibt `AiProvider.configSchema(): List<ConfigField>?` (Vorgabe `null`) und baut
  die Liste selbst. Die Konfigurations-UI verarbeitet **nur** diese Liste.
* **Beschriftungen und i18n.** `label` und `help` sind Schlüssel; der Manager schlägt sie in einem
  `ResourceBundle` nach, das das Plugin mitbringt, und fällt auf den Schlüssel-Klartext zurück, wenn
  keiner passt. Die mitgelieferten Provider liefern zunächst `en` und `de`. Die Message-Bundles der
  Anwendung werden dafür nie benutzt.
* **Zwei Plugin-Orte.** (1) Das **eingebaute** Verzeichnis `plugins` im Installationsverzeichnis –
  fix, nicht änderbar; hierin liegen die Plugin-JARs der Module unter `lib/plugin/provider`. (2) Ein
  **Nutzer**-Verzeichnis für zusätzliche Plugins, Vorgabe `~/.ai-ghost/plugins`, über die
  Preferences-UI änderbar. Beide Orte werden beim Start gelesen; das eingebaute zuerst. Kollidieren
  zwei Provider auf derselben Id, gewinnt der zuerst geladene – also der eingebaute vor einem
  Nutzer-Plugin, und bei zwei Nutzer-Plugins das zuerst geladene. Eine Änderung des
  Nutzer-Verzeichnisses wirkt beim nächsten Start.
* **Plugin-Manager.** Ein neues Modul `lib/plugin/manager` (`ai-ghost-plugin-manager`, zweites Modul
  unter `lib/plugin` neben `api`) bekommt die zu durchsuchenden Verzeichnisse **übergeben** (es liest
  weder Installationsort noch Preferences selbst), lädt jedes Plugin über einen eigenen
  `URLClassLoader` (keine JPMS-Modulschicht – bewusst einfach gehalten), instanziiert die Provider,
  leitet ihre `List<ConfigField>` per `kotlin-reflect` ab (`ConfigModelReader`) und füllt eine
  Provider-Registrierung. Er prüft die Vertragsversion aus `@AiProviderInfo` und lehnt ein zu altes
  oder zu neues Plugin ab. Abhängigkeitskette: `app/ui` → `ai-ghost-plugin-manager` →
  `ai-ghost-plugin-api`. Ein defektes oder inkompatibles Plugin wird gemeldet und übersprungen, nie
  bricht es den Start ab.
* **Startschritt.** Ein `StartupStep` in `org.pcsoft.app.aighost.app.startup.step` (`app/ui`)
  ermittelt das eingebaute Verzeichnis (`<Installationsverzeichnis>/plugins`) und das
  Nutzer-Verzeichnis (aus `Preferences`, Vorgabe `~/.ai-ghost/plugins`), übergibt beide dem
  Plugin-Manager und ruft ihn vor dem ersten Fenster auf. Er rangiert hinter
  `PreferencesStartupStep` (`@StartupOrder` > 0), weil er die Einstellungen braucht.
  Fehlerbehandlung nach dem `startup`-Skill.
* **Mitgelieferte Provider – ein Modul je Provider.** Ein neuer Container `lib/plugin/provider`,
  dritter Zweig unter `lib/plugin` neben `api` und `manager`. Darunter je Provider ein eigenes
  Gradle-Modul, jedes allein gegen `ai-ghost-plugin-api` gebaut und zu **einem eigenen Plugin-JAR**
  im eingebauten Verzeichnis gepackt; die Abhängigkeiten eines Providers bleiben so – zusätzlich
  isoliert durch den `URLClassLoader` je Plugin – von den anderen getrennt:
  1. `lib/plugin/provider/stub` (`ai-ghost-provider-stub`) – **Stub**: gibt einen Standardtext
     zurück; der Text steht im Config-Modell (`responseText`) und ist über die generische
     Konfigurations-UI änderbar. `generate()` streamt diesen Text, ignoriert die Prompts.
     Vollständig in diesem Feature umgesetzt; er ist der Nachweis der Plugin-Funktionalität und
     bleibt dauerhaft bestehen.
  2. `lib/plugin/provider/lm-studio` (`ai-ghost-provider-lm-studio`) – **LM Studio**: Config-Modell
     (Basis-URL, Port, Modellname …) und UI in diesem Feature, der Client in IP-06.
  3. `lib/plugin/provider/openai` (`ai-ghost-provider-openai`) – **OpenAI API**: Config-Modell und
     UI hier, der Client in IP-07.
  4. `lib/plugin/provider/anthropic` (`ai-ghost-provider-anthropic`) – **Anthropic API**:
     Config-Modell und UI hier, der Client in IP-08.
  5. `lib/plugin/provider/llama-cpp` (`ai-ghost-provider-llama-cpp`) – **llama.cpp**: direkter
     Zugriff auf lokale Modelldateien; Config-Modell und UI hier, die native Anbindung in IP-09.
* **Persistenz der Wahl.** Die gewählte Provider-Id und die Werte der nicht geheimen Config-Felder
  je Provider werden gespeichert und gemäß `fx-model` gespiegelt. Geheime Felder (`secret = true`)
  liegen getrennt vom Projektdokument und getrennt von `preferences.yml`.
* **Geheimnis-Speicher.** Ein `SecretStore` nach dem Muster von IntelliJ PasswordSafe: primär der
  OS-Schlüsselbund (macOS Keychain, Windows Credential Manager, Secret Service/KWallet unter Linux),
  Fallback eine verschlüsselte Datei. Zunächst klein gehalten – eine schmale Schnittstelle, eine
  Implementierung plus Datei-Fallback.
* **Plugin-Verzeichnis-Einstellung.** `Preferences` bekommt einen Pfad für das Nutzer-Plugin-
  Verzeichnis (Vorgabe `~/.ai-ghost/plugins`), gemäß `fx-model` gespiegelt; das eingebaute
  Verzeichnis ist nicht Teil der Einstellungen.
* **Oberfläche.** Ein Einstellungsabschnitt listet die entdeckten Provider und baut aus der
  `List<ConfigField>` des gewählten Providers generisch ein Formular (Textfeld, Passwortfeld,
  Kontrollkästchen, Zahl, Auswahlfeld), prüft `required` und Typ, speichert und bietet einen
  Verbindungstest (ein direkter Einzelaufruf des Providers, keine Orchestrierung). Ein Feld für das
  Nutzer-Plugin-Verzeichnis (mit Ordnerauswahl) gehört ebenfalls hierher; ein Hinweis nennt, dass
  eine Änderung beim nächsten Start greift.
* **Paketierung.** Eine Gradle-Aufgabe paketiert ein Plugin-JAR und prüft es, die CI führt sie aus,
  die Architekturregel benennt `lib/plugin` mit `api`, `manager` und `provider/*`. Alle Artefakte
  tragen die Repo-Version; die Kompatibilität regelt die Vertragsversion in `@AiProviderInfo`.
* **MkDocs-Anleitung.** Unter dem Menüpunkt „Plugins“ eine Übersichtsseite zum Plugin-Mechanismus
  und darunter ein Bereich „AI Provider“ mit drei Seiten: die API, die allgemeine Entwicklung und
  ein durchgearbeitetes Beispiel (der Stub).

## 4. Anforderungen

### Funktionale Anforderungen

* Ein Plugin, das den Provider-Vertrag erfüllt und im eingebauten Verzeichnis
  (`<Installationsverzeichnis>/plugins`) oder im Nutzer-Verzeichnis liegt, erscheint nach einem
  Neustart in der Provider-Auswahl.
* Das Nutzer-Plugin-Verzeichnis ist über die Preferences-UI änderbar (Vorgabe `~/.ai-ghost/plugins`);
  eine Änderung greift beim nächsten Start. Das eingebaute Verzeichnis ist fix.
* Kollidieren zwei Provider auf derselben Id, gewinnt der zuerst geladene (eingebautes Verzeichnis
  vor Nutzer-Verzeichnis; bei zwei Nutzer-Plugins das zuerst geladene).
* Die Feldbeschriftungen der mitgelieferten Provider liegen in Englisch und Deutsch vor; die
  Anwendungssprache entscheidet, welches Bundle greift.
* Ein Provider bekommt bei einem Aufruf ausschließlich einen System-Prompt und einen User-Prompt und
  liefert die Antwort als abbrechbaren Strom; ein Fehler beendet die Anwendung nicht.
* Ein Provider beschreibt seine Konfiguration als annotiertes Datenmodell **oder** über ein eigenes
  `configSchema()`; beide Wege ergeben dieselbe `List<ConfigField>`. Die Anwendung baut das Formular
  allein daraus, ohne den Provider zu kennen, und rendert ein `secret`-Feld als Passwortfeld.
* Ein annotiertes Config-Modell mit nicht unterstütztem Feldtyp führt dazu, dass der Provider als
  fehlkonfiguriert gemeldet und übersprungen wird – wie ein defektes Plugin.
* Die gewählte Provider-Id und die Werte der nicht geheimen Config-Felder überleben einen Neustart;
  ein geheimes Feld wird nicht in das Projektdokument und nicht in `preferences.yml` geschrieben.
* Ein defektes oder inkompatibles Plugin (lädt nicht, falsche Vertragsversion, wirft beim
  Instanziieren) wird beim Start gemeldet und übersprungen; die übrigen Plugins und die Anwendung
  starten normal.
* Der Stub-Provider gibt den im Config-Modell hinterlegten, über die UI änderbaren Standardtext
  gestreamt zurück; der „Verbindung testen“-Aufruf zeigt genau diesen Text.
* Unter `lib/plugin/provider` besteht je mitgeliefertem Provider ein Modul mit eigenem Plugin-JAR;
  für Stub ist alles umgesetzt, für LM Studio, OpenAI, Anthropic und llama.cpp Config-Modell und UI,
  die Client-Anbindung je im eigenen Plan.
* Die MkDocs-Anleitung führt einen Autor vom Plugin-Mechanismus über die AI-Provider-API und die
  allgemeine Entwicklung bis zu einem lauffähigen Beispiel (dem Stub).
* Die `TODO("AI action: …")`-Rümpfe bleiben unverändert; dieses Feature erzeugt keinen KI-Text im
  Editor.

### Technische Anforderungen

* Kotlin und Gradle. `ai-ghost-plugin-api` behält `kotlin.stdlib` als einzige Abhängigkeit; die
  Config-Annotations kommen ohne Reflection aus, nur Manager und UI werten sie aus.
* Jedes Plugin wird über einen eigenen `URLClassLoader` geladen; eine Config-Klasse in einem
  nicht-modularen Plugin-JAR ist damit von `kotlin-reflect` von sich aus erreichbar. Ein modulares
  Plugin muss sein Config-Paket `opens` – die Autoren-Anleitung sagt das.
* Kein JavaFX in `lib/plugin/api`, `lib/plugin/manager`, den Modulen unter `lib/plugin/provider`
  oder einem anderen `lib`-Modul außerhalb der in `.claude/rules/architecture.md` genannten
  Ausnahmen.
* `lib/ai` wird nicht verändert; dieses Feature hat keine Abhängigkeit von `lib/ai` und umgekehrt.
* Das jlink-Image bleibt lauffähig; der `URLClassLoader` lädt die Plugin-JARs neben der Modulschicht
  der Anwendung.
* Ein Provider-Aufruf läuft nicht auf dem FX-Thread.
* Modelländerungen folgen `fx-model`; UI-Arbeit folgt `ui-styling`, `fx-component-lifecycle`,
  `icons` und `font`; ein Startschritt folgt `startup`; Tests folgen `testing`; Dokumentation folgt
  `project-docs`; Workflows folgen `ci-pipeline`.
* Der Startschritt liegt in `org.pcsoft.app.aighost.app.startup.step`; keine Plugin-Arbeit in
  `Launcher`, `main` oder einem View-Modell.
* Jede neue Drittanbieter-Abhängigkeit – HTTP-Client, LLM-SDK, native llama.cpp-Bindung, eine
  Bibliothek für den OS-Schlüsselbund – wird zuerst mit dem Nutzer abgestimmt. Der Kern des Features
  (IP-01 bis IP-05) kommt ohne solche aus, mit Ausnahme einer möglichen Schlüsselbund-Bibliothek in
  IP-03.
* Alle Artefakte tragen dieselbe Version wie das übrige Repo; `ai-ghost-plugin-api` wird nicht
  eigenständig versioniert. Die Kompatibilität zwischen einem älter gebauten Plugin und einer neueren
  Anwendung regelt eine **Vertragsversion** in `@AiProviderInfo`, die nur bei einer
  binärinkompatiblen Änderung des Provider-Vertrags steigt und vom Manager geprüft wird.

## 5. Architektur

```text
ai-ghost-plugin-api            (bestehendes Modul, nur kotlin.stdlib)
  ProjectPart (vorhanden) + AiProvider (configType, configSchema()),
  @AiProviderInfo (mit Vertragsversion), Prompt-Strom-Vertrag,
  AiProviderConfig-Marker, @AiProviderConfigField, ConfigField + ConfigFieldType
  -- NUR plugin-seitige Typen; keine Registrierung, kein Loader, keine Reflection --
        ▲                                   ▲
        │ (nur zum Bauen)                   │
lib/plugin/provider/<name>           ai-ghost-plugin-manager   (neues Modul lib/plugin/manager)
  je Provider ein Modul + JAR:         bekommt Verzeichnisse übergeben, ein
   provider-stub (voll),               URLClassLoader je Plugin, Vertragsversions-Prüfung,
   provider-lm-studio, -openai,        Provider-Registrierung, ConfigModelReader (kotlin-reflect),
   -anthropic, -llama-cpp             Fehlerisolation
  -> jeweils ins eingebaute plugins/                   ▲
                                                       │
                                app/ui ────────────────┘
                                  ermittelt <Install>/plugins (fix) + Nutzer-Dir
                                    (aus Preferences, Vorgabe ~/.ai-ghost/plugins),
                                  Provider-Auswahl + generische Konfig-UI,
                                  StartupStep, SecretStore, Persistenz-Verdrahtung

Kette: app/ui -> ai-ghost-plugin-manager -> ai-ghost-plugin-api
       lib/plugin/provider/<name> -> ai-ghost-plugin-api   (wie jedes fremde Plugin)
lib/ai bleibt unberührt.
```

* **`ai-ghost-plugin-api`** – der einzige Ort für plugin-seitige APIs. Bekommt den `AiProvider`-
  Vertrag (mit `configType` und optional überschreibbarem `configSchema(): List<ConfigField>?`),
  `@AiProviderInfo` (samt Vertragsversion), `AiProviderConfig`-Marker, `@AiProviderConfigField`,
  `ConfigField` + `ConfigFieldType` neben `ProjectPart`.
* **`ai-ghost-plugin-manager`** – neues Modul `lib/plugin/manager`. Bekommt die zu scannenden
  Verzeichnisse übergeben (es kennt weder Installationsort noch `Preferences`). Hält den
  Verzeichnis-Scan, einen `URLClassLoader` je Plugin (keine JPMS-Modulschicht), die Prüfung der
  Vertragsversion, die Provider-Registrierung mit der Regel „zuerst geladen gewinnt“ bei
  Id-Kollision, `ConfigModelReader` (Annotations → `List<ConfigField>` per `kotlin-reflect`), das
  Befüllen/Auslesen einer `AiProviderConfig`-Instanz und die Fehlerisolation je Plugin. Hängt von
  `ai-ghost-plugin-api`, ClassGraph und `kotlin-reflect` ab, trägt kein JavaFX.
* **`lib/plugin/provider/<name>`** – je mitgeliefertem Provider ein eigenes Gradle-Modul
  (`ai-ghost-provider-stub`, `-lm-studio`, `-openai`, `-anthropic`, `-llama-cpp`), gebaut wie ein
  fremdes Plugin (nur gegen `ai-ghost-plugin-api`) und zu einem eigenen Plugin-JAR im eingebauten
  Verzeichnis gepackt. Kein JavaFX; ein Provider mit Netz-/Nativ-Anbindung bringt seine eigene
  Abhängigkeit mit, die durch den `URLClassLoader` je Plugin von den anderen getrennt bleibt.
  `en`/`de`-`ResourceBundle`s für die Feldbeschriftungen.
* **`app/ui`** – ermittelt die beiden Plugin-Verzeichnisse (eingebautes `plugins/` im
  Installationsverzeichnis, fix; Nutzer-Verzeichnis aus `Preferences`) und übergibt sie dem Manager;
  Startschritt, Auswahl- und Konfigurations-Oberfläche inklusive des Feldes für das
  Nutzer-Plugin-Verzeichnis, `SecretStore` (IntelliJ-PasswordSafe-Muster), die Persistenz-Verdrahtung
  (Feldliste des Managers ↔ `Preferences` ↔ `SecretStore`). Verdrahtet **keine** KI-Schaltfläche.
* **`docs/docs`** – neuer Menüpunkt „Plugins“ mit Übersichtsseite und dem Bereich „AI Provider“
  (API, Entwicklung, Beispiel Stub).

**Konfigurationsmodell und Formular.** Der Plugin-Manager stellt die effektive `List<ConfigField>`
eines Providers her: `configSchema()` wird bevorzugt, sonst reflektiert der `ConfigModelReader` die
`@AiProviderConfigField`-Eigenschaften von `configType`. Aus der `List<ConfigField>` baut `app/ui`
das Formular und die Persistenz-Abbildung. Die befüllte `AiProviderConfig`-Instanz wird dem Provider
bei `connect(config, …)` gereicht; die UI kennt weder Reflection noch die Config-Klasse.

**Modell- und Einstellungserweiterung.** `Preferences` bekommt die gewählte Provider-Id, eine
Abbildung `providerId -> (feldName -> wert)` für die nicht geheimen Felder und den
`userPluginDirectory`-Pfad, gemäß `fx-model` gespiegelt. Geheime Felder liegen im `SecretStore`
(OS-Schlüsselbund, verschlüsselte Datei als Fallback).

## 6. Übersicht der Implementierungspläne

Jeder Plan ist atomar und endet mit einem vollständig nutzbaren Anwendungsfall. IP-06 bis IP-09
tragen die konkreten Anbindungen, hängen am Ende und setzen den Kern (IP-01 bis IP-05) nicht als
Nutzen voraus – der ist über den Stub bereits erbracht.

| ID    | Implementierungsplan                              | Anwendungsfall, der danach vollständig läuft                                                     | Abhängigkeiten |
|-------|--------------------------------------------------|-----------------------------------------------------------------------------------------------|----------------|
| IP-01 | Plugin API And Plugin Manager (**COMPLETED**)     | Die Anwendung entdeckt und lädt Provider-Plugins beim Start; ein defektes Plugin wird übersprungen (nachgewiesen über ein Fixture-Plugin) | -              |
| IP-02 | Provider Modules And Stub Provider                | Beim Start steht der ausgelieferte Stub-Provider in der Registrierung; `generate()` streamt seinen konfigurierten Text | IP-01          |
| IP-03 | Provider Selection, Configuration And Persistence | Ein Nutzer wählt den Stub, ändert dessen Antworttext, testet ihn, speichert – die Wahl übersteht einen Neustart | IP-02          |
| IP-04 | Plugin Packaging And CI                           | `./gradlew build` paketiert und prüft die Plugin-JARs; die CI führt den Schritt aus          | IP-03          |
| IP-05 | Documentation                                     | Ein Autor findet unter „Plugins“ die vollständige Anleitung: Mechanismus, AI-Provider-API, Entwicklung, Beispiel Stub | IP-04          |
| IP-06 | LM Studio Provider                                | Der LM-Studio-Provider verbindet sich mit einem lokalen LM-Studio-Server und liefert Text     | IP-03          |
| IP-07 | OpenAI API Provider                               | Der OpenAI-Provider verbindet sich mit der ChatGPT-API und liefert Text                        | IP-03          |
| IP-08 | Anthropic API Provider                            | Der Anthropic-Provider verbindet sich mit der Claude-API und liefert Text                      | IP-03          |
| IP-09 | llama.cpp Provider                                | Der llama.cpp-Provider lädt eine lokale Modelldatei direkt und liefert Text                    | IP-03          |

## 7. Implementierungspläne

### IP-01: Plugin API And Plugin Manager (COMPLETED)

**Ziel**

Die Anwendung kann AI-Provider-Plugins entdecken und isoliert laden. Nach dem Plan füllt der
Startschritt eine Provider-Registrierung; kein Plugin ist halb verdrahtet.

**Umfang**

Enthalten:

* `ai-ghost-plugin-api` (bestehendes Modul) bekommt den vollständigen plugin-seitigen Vertrag:
  `AiProvider` (System-/User-Prompt hinein, Textstrom mit Abschluss/Fehler/Abbruch heraus; Handle
  zum Abbrechen; `configType`; `configSchema(): List<ConfigField>?` mit Vorgabe `null`),
  `@AiProviderInfo` (stabile Id, Anzeigename, **Vertragsversion**), `AiProviderConfig`-Marker,
  `@AiProviderConfigField`, `ConfigField` + `ConfigFieldType`. Neues exportiertes Paket im
  `module-info.java`. Abhängigkeit bleibt `kotlin.stdlib`.
* neues Modul `lib/plugin/manager` (`ai-ghost-plugin-manager`): nimmt eine **übergebene** Liste von
  Verzeichnissen entgegen, lädt jedes Plugin über einen eigenen `URLClassLoader` (keine
  JPMS-Modulschicht), entdeckt die `AiProvider`-Implementierungen, prüft ihre Vertragsversion,
  `AiProviderRegistry` mit der Regel „zuerst geladen gewinnt“ bei Id-Kollision, `ConfigModelReader`
  (`configSchema()` bevorzugt, sonst Reflexion der Annotations zu `List<ConfigField>` per
  `kotlin-reflect`, nicht unterstützter Feldtyp → Plugin fehlkonfiguriert), Befüllen/Auslesen einer
  Instanz per Reflektion, Fehlerisolation je Plugin. Abhängigkeiten des Moduls:
  `ai-ghost-plugin-api`, ClassGraph, `kotlin-reflect`. Eintrag in `settings.gradle.kts`; geschärfte
  Zeile in `.claude/rules/architecture.md`.
* die Ermittlung der beiden Plugin-Orte in `app/ui`: das eingebaute Verzeichnis
  `<Installationsverzeichnis>/plugins` (fix – Auflösung über das jpackage-/jlink-Image-Layout) und
  das Nutzer-Verzeichnis (hier noch fest auf `~/.ai-ghost/plugins`; die Konfigurierbarkeit über
  `Preferences` kommt in IP-03); ein fehlendes Nutzer-Verzeichnis ist kein Fehler. Beide werden dem
  Manager übergeben, das eingebaute zuerst.
* `StartupStep` in `org.pcsoft.app.aighost.app.startup.step` (`app/ui`), rangiert hinter
  `PreferencesStartupStep` (`@StartupOrder` > 0), ermittelt die Verzeichnisse und ruft den Manager;
  `app/ui` bekommt `requires org.pcsoft.app.aighost.plugin.manager`.
* Tests: Fixture-Plugins in beiden Verzeichnissen (nur im Testquellsatz); Nachweis von Entdeckung
  aus beiden Orten, Isolation über getrennte `URLClassLoader`, Vertragsversions-Prüfung,
  Registrierung, `ConfigModelReader`, dem Überspringen eines defekten Plugins, dem Vorrang des
  eingebauten Verzeichnisses und – bei zwei Nutzer-Plugins gleicher Id – dem Sieg des zuerst
  geladenen.

Nicht enthalten: ein ausgelieferter Provider, die Auswahl-/Konfigurations-UI, die
`Preferences`-Erweiterung um den Verzeichnispfad (IP-03), jede KI-Aktion.

**Betroffene Bereiche**

`lib/plugin/api`, neues `lib/plugin/manager`, `settings.gradle.kts`, `app/ui` (`build.gradle.kts`,
`module-info.java`, Startschritt-Paket, Verzeichnisermittlung), `.claude/rules/architecture.md`.

**Abhängigkeiten**

Keine.

**Erwartetes Ergebnis**

Beim Start scannt die Anwendung beide Plugin-Verzeichnisse; ein Fixture-Plugin steht mit seiner
`List<ConfigField>` in der Registrierung, ein defektes oder in der Vertragsversion unpassendes
Fixture steht in der Meldungsausgabe und fehlt in der Registrierung, ohne den Start zu stoppen.
Vollständig über Fixture-Tests nachweisbar; der erste **ausgelieferte** Provider kommt in IP-02.

**Technische Überlegungen**

Der Loader ist ein `URLClassLoader` je Plugin – keine JPMS-Modulschicht, bewusst einfach gehalten.
Eine Config-Klasse in einem nicht-modularen Plugin-JAR ist damit für `kotlin-reflect` von sich aus
erreichbar. `AiProvider` trägt Abbruch und Fehler von Anfang an. Die Vertragsversion in
`@AiProviderInfo` ist von der Artefaktversion getrennt und steigt nur bei einem Bruch des
Provider-Vertrags. Das Manager-Modul bleibt frei von JavaFX. Der Startschritt läuft auf dem
Hintergrund-Thread und meldet Fehler über `onFxThread`, wie `PreferencesStartupStep`. Offen bleibt
nur die verlässliche Auflösung von `<Install>/plugins` aus dem Image-Layout.

### IP-02: Provider Modules And Stub Provider

**Ziel**

Den Container `lib/plugin/provider` anlegen, das Stub-Modul darin aufbauen und den Stub-Provider
voll ausliefern – der Nachweis, dass die Plugin-Kette trägt.

**Umfang**

Enthalten:

* der neue Container `lib/plugin/provider` und das Modul `lib/plugin/provider/stub`
  (`ai-ghost-provider-stub`), Einträge in `settings.gradle.kts`, geschärfte
  `.claude/rules/architecture.md`; das Modul baut ein eigenes Plugin-JAR, allein gegen
  `ai-ghost-plugin-api`. Ein Convention-/Gradle-Baustein, den die späteren Provider-Module (IP-06
  bis IP-09) wiederverwenden.
* der **Stub-Provider**: `@AiProviderInfo` (mit Vertragsversion), `StubProviderConfig` mit
  `responseText: String` (`@AiProviderConfigField`, Standardwert gesetzt) und optional
  `chunkDelayMillis: Int` zum Sichtbarmachen des Streamings; `generate()` streamt `responseText` in
  Chunks, ignoriert die Prompts, meldet Abschluss/Abbruch sauber; `en`- und `de`-`ResourceBundle`
  für seine Feldbeschriftungen.
* das Einpacken des Stub-JARs in das eingebaute Plugin-Verzeichnis; die Build-Verdrahtung, die das
  jlink-Image mit dem Verzeichnis ausliefert.
* Tests: der Stub wird vom Manager entdeckt, seine `List<ConfigField>` enthält `responseText`,
  `generate()` liefert den erwarteten Strom.

Nicht enthalten: die Auswahl-/Konfigurations-UI (IP-03), die vier konkreten Anbindungen.

**Betroffene Bereiche**

Neuer Container `lib/plugin/provider` und Modul `lib/plugin/provider/stub`, `settings.gradle.kts`,
`.claude/rules/architecture.md`, `app/ui` bzw. der Build (Zusammenstellen des
Image-Plugin-Verzeichnisses).

**Abhängigkeiten**

IP-01.

**Erwartetes Ergebnis**

Nach dem Start steht der Stub-Provider mit seiner `List<ConfigField>` in der Registrierung; ein
direkter `generate()`-Aufruf streamt den hinterlegten Standardtext. Über Tests vollständig
nachweisbar; für einen Nutzer sichtbar wird die Kette in IP-03.

**Technische Überlegungen**

Je Provider ein eigenes Modul und Plugin-JAR – so bleiben die späteren Netz-/Nativ-Abhängigkeiten
(IP-06 bis IP-09) getrennt, zusätzlich isoliert durch den `URLClassLoader` je Plugin; die Frage
eines geteilten Klassenpfads stellt sich nicht. Der Stub bringt keine Abhängigkeit mit. Er ersetzt
den früher angedachten „Echo“-Provider und bleibt dauerhaft bestehen. Der wiederverwendbare
Gradle-Baustein für ein Provider-Modul entsteht hier einmal.

### IP-03: Provider Selection, Configuration And Persistence

**Ziel**

Ein Nutzer wählt in den Einstellungen einen Provider, füllt dessen Konfiguration, prüft sie und die
Wahl übersteht einen Neustart.

**Umfang**

Enthalten:

* `Preferences`: neue Gruppe `AiProviders` (gewählte Provider-Id + `providerId -> (feldName -> wert)`
  für nicht geheime Felder + `userPluginDirectory: String`, Vorgabe `~/.ai-ghost/plugins`), gemäß
  `fx-model` gespiegelt, mit Property-Tests; der Startschritt aus IP-01 liest den Pfad jetzt von hier
  statt von der festen Vorgabe;
* ein `SecretStore` nach dem Muster von IntelliJ PasswordSafe für die `secret`-Felder: primär der
  OS-Schlüsselbund (macOS Keychain, Windows Credential Manager, Secret Service/KWallet), Fallback
  eine verschlüsselte Datei; eine schmale Schnittstelle plus eine Implementierung, zunächst klein;
* Persistenz-Verdrahtung in `app/ui`: `List<ConfigField>` des Managers ↔ `Preferences` (nicht
  geheim) bzw. `SecretStore` (geheim); die Werte gehen an den Manager zum Befüllen einer
  `AiProviderConfig`-Instanz;
* Einstellungsabschnitt (`app/ui`, MVVM-FX-Trio + FXML + CSS + Nachrichtenbündel): Provider-Liste,
  je `ConfigFieldType` ein Steuerelement (Textfeld für `STRING`, Passwortfeld bei `secret`,
  Kontrollkästchen für `BOOLEAN`, Zahlenfeld für `INT`/`LONG`/`DOUBLE`, Auswahlfeld für `ENUM`),
  `help` als Hinweis, `required`-Markierung, Validierung, Speichern;
* der Bundle-Lookup von `label`/`help` gegen ein plugin-eigenes `ResourceBundle`, mit Fallback auf
  den Schlüssel-Klartext; die mitgelieferten Provider bringen `en` und `de` mit;
* ein Feld für das **Nutzer-Plugin-Verzeichnis** mit Ordnerauswahl (`DirectoryChooser`), gebunden an
  `Preferences.userPluginDirectory`, mit dem Hinweis, dass eine Änderung beim nächsten Start greift;
  das eingebaute Verzeichnis erscheint nicht als Feld;
* „Verbindung testen“: ein direkter Einzelaufruf des Providers mit einem kurzen festen Prompt (keine
  Orchestrierung), zeigt Ergebnis bzw. Fehler, blockiert den FX-Thread nicht.

Nicht enthalten: jede KI-Schaltfläche, jede Prompt-Orchestrierung, `lib/ai`; ein Live-Neuscan des
Verzeichnisses ohne Neustart.

**Betroffene Bereiche**

`lib/model` (`ai-ghost-model`), `lib/fx-model` (`ai-ghost-fx-model`), neuer `SecretStore`
(OS-Schlüsselbund-Zugriff, ggf. eine kleine Bibliothek dafür – Rückfrage falls Abhängigkeit),
`ai-ghost-plugin-manager` (direkter Testaufruf-Pfad, `ResourceBundle`-Lookup), `app/ui`
(Einstellungsabschnitt + Verdrahtung).

**Abhängigkeiten**

IP-02.

**Erwartetes Ergebnis**

Ein Nutzer wählt den Stub-Provider, ändert `responseText`, „Verbindung testen“ zeigt genau diesen
Text, speichert; nach einem Neustart ist die Wahl samt Werten wieder aktiv, ein `secret`-Feld kommt
aus dem `SecretStore`. Setzt der Nutzer das Plugin-Verzeichnis auf einen anderen Ordner, legt dort
ein Plugin-JAR ab und startet neu, erscheint dessen Provider zusätzlich in der Auswahl. Die
Plugin-Funktionalität ist damit von Auswahl über Konfiguration bis Persistenz für einen Nutzer
vollständig sichtbar.

**Technische Überlegungen**

Das Befüllen der Instanz macht der Manager per `kotlin-reflect`, nicht über Jackson; die Persistenz
kennt nur `feldName`, Wert und `secret`. Ein fehlendes Feld fällt auf die Vorgabe aus der
`List<ConfigField>` zurück, ein unbekannter Schlüssel wird ignoriert. Der `SecretStore` folgt dem
Muster von IntelliJ PasswordSafe: OS-Schlüsselbund zuerst, verschlüsselte Datei als Fallback, hinter
einer schmalen Schnittstelle; zunächst klein gehalten. Ein `secret`-Feld zeigt nie den gespeicherten
Wert.

### IP-04: Plugin Packaging And CI

**Ziel**

Ein Plugin-JAR bauen, prüfen und in der CI abdecken.

**Umfang**

Enthalten: eine Gradle-Aufgabe, die ein Provider-Plugin-JAR paketiert und gegen die Vertragsversion
prüft (der „Verify plugin“-Schritt des `ci-pipeline`-Skills), angewandt auf jedes Modul unter
`lib/plugin/provider`; die CI-Verdrahtung in `ci.yml`/`release.yml`; die geschärfte
`.claude/rules/architecture.md` (`lib/plugin` trägt `api`, `manager` und `provider/*`, alle ohne
JavaFX); KDoc auf der Plugin-API; ein CHANGELOG-Eintrag.

Nicht enthalten: die MkDocs-Anleitung (IP-05), ein Marktplatz oder Verteilkanal.

**Betroffene Bereiche**

`build.gradle.kts` bzw. `buildSrc`/Convention-Plugin, `.github/workflows`, `.claude/rules`,
`CHANGELOG.md`.

**Abhängigkeiten**

IP-03.

**Erwartetes Ergebnis**

`./gradlew build` paketiert und prüft die Plugin-JARs der Module unter `lib/plugin/provider`; die CI
führt den Schritt aus. Ein von außen gebautes Provider-JAR, in `~/.ai-ghost/plugins` abgelegt,
erscheint nach dem Neustart in der Auswahl.

**Technische Überlegungen**

Die Gradle-Aufgabe wird über einen Agent ausgeführt, nicht als Hintergrundbefehl der Shell
(`CLAUDE.md`, Concurrency). Name und Umfang einer `verifyPlugin`-Aufgabe sind offen. Die Aufgabe
baut auf dem in IP-02 entstandenen Provider-Modul-Baustein auf.

### IP-05: Documentation

**Ziel**

Eine MkDocs-Anleitung, die einen Autor von der Funktionsweise des Plugin-Mechanismus bis zu einem
lauffähigen eigenen AI-Provider-Plugin führt.

**Umfang**

Enthalten – neue Seiten unter `docs/docs` und die passende Navigation in der MkDocs-Konfiguration,
gemäß `project-docs`-Skill:

* **Menüpunkt „Plugins“** mit einer **Übersichtsseite**: allgemeine Beschreibung des
  Plugin-Mechanismus – die beiden Plugin-Orte (das eingebaute `plugins`-Verzeichnis im
  Installationsverzeichnis, fix; das Nutzer-Verzeichnis, Vorgabe `~/.ai-ghost/plugins`, über die
  Preferences-UI änderbar, Wirkung beim nächsten Start; Vorrang des eingebauten bei gleicher
  Provider-Id, sonst „zuerst geladen gewinnt“), wie Plugins eingelesen werden (Scan beim Start, ein
  `URLClassLoader` je Plugin, Vertragsversions-Prüfung, Registrierung, Fehlerisolation), was ein
  Plugin-JAR mitbringen muss und wie ein defektes Plugin behandelt wird.
* Darunter ein Bereich **„AI Provider“** mit drei Seiten:
  1. **API** – Erklärung der AI-Provider-Plugin-API: `AiProvider` (Prompt-Strom-Vertrag, Handle,
     Abbruch, Fehler), `@AiProviderInfo` (samt Vertragsversion), `AiProviderConfig`,
     `@AiProviderConfigField`, `ConfigField`/`ConfigFieldType`, `configType`, `configSchema()`. Jeder
     Typ mit Zweck und Signatur.
  2. **Entwicklung** – allgemeine Beschreibung: welche Abhängigkeiten nötig sind (nur
     `ai-ghost-plugin-api` plus die eigenen des Providers), welche Klassen man baut (Provider,
     Config-Modell), wo und wie sie registriert werden (`configType`, Service-/Manifest-Eintrag zur
     Entdeckung, `opens` des Config-Pakets bei modularen Plugins), wie das JAR gebaut und wohin es
     gelegt wird, i18n über ein plugin-eigenes `ResourceBundle` (mindestens `en` und `de`, wie die
     mitgelieferten Provider).
  3. **Beispiel: Stub** – die konkrete Implementierung des Stub-Providers durchgearbeitet:
     `StubProviderConfig` mit seinen Annotations, `generate()` mit dem gestreamten `responseText`,
     die JAR-Paketierung und das Ergebnis in der Konfigurations-UI.

Nicht enthalten: Nutzerdoku zur Bedienung der Provider-Auswahl (gehört zum
Einstellungsabschnitt-Kapitel), Doku der vier konkreten Anbindungen (in IP-06 bis IP-09).

**Betroffene Bereiche**

`docs/docs` (neue Seiten), die MkDocs-Navigationskonfiguration, ggf. `CHANGELOG.md`.

**Abhängigkeiten**

IP-04.

**Erwartetes Ergebnis**

Der MkDocs-Build enthält den Menüpunkt „Plugins“ mit Übersicht und dem Bereich „AI Provider“
(API, Entwicklung, Beispiel Stub); ein Leser kann anhand der Beispielseite den Stub nachbauen und im
Plugin-Verzeichnis wiederfinden. Der „Build and verify MkDocs“-Schritt der CI bleibt grün.

**Technische Überlegungen**

Die Beispielseite verweist auf das echte `lib/plugin/provider/stub`-Modul aus IP-02, damit Anleitung
und Auslieferung nicht auseinanderlaufen. Die Seiten beschreiben das Laden über `URLClassLoader` so,
wie es umgesetzt wurde (insbesondere die `opens`-Anforderung für modulare Plugins).

### IP-06: LM Studio Provider

**Ziel**

Der LM-Studio-Provider verbindet sich mit einem lokalen LM-Studio-Server und liefert Text.

**Umfang**

Enthalten: das neue Modul `lib/plugin/provider/lm-studio` (`ai-ghost-provider-lm-studio`) auf dem
Provider-Modul-Baustein aus IP-02, mit `@AiProviderInfo`, `LmStudioProviderConfig` (Basis-URL, Port,
Modellname, ggf. Timeout) samt `@AiProviderConfigField`-Annotations und `en`/`de`-`ResourceBundle`,
und `generate()`, das die Prompts an den lokalen Server sendet und den Strom zurückgibt; Tests gegen
einen lokalen Fake-Server.

Nicht enthalten: die Orchestrierung, das Verdrahten einer KI-Schaltfläche.

**Betroffene Bereiche**

Neues Modul `lib/plugin/provider/lm-studio`, `settings.gradle.kts`, das eingebaute
Plugin-Verzeichnis, ggf. eine neue HTTP-Client-Abhängigkeit (isoliert im Modul-JAR).

**Abhängigkeiten**

IP-03 (die Config-UI- und Persistenz-Strecke). Atomar und unabhängig von IP-04, IP-05, IP-07 bis
IP-09.

**Erwartetes Ergebnis**

Nach dem Start steht der LM-Studio-Provider in der Auswahl; konfiguriert liefert „Verbindung testen“
echten Text aus dem lokalen Server.

**Technische Überlegungen**

LM Studio spricht ein OpenAI-kompatibles HTTP-API; der HTTP-Client ist eine **neue
Drittanbieter-Abhängigkeit** (`dependencies.md`, Rückfrage; Lizenz auf der Allowlist) und liegt
allein im Modul-JAR. Der Provider führt keine Orchestrierung aus.

### IP-07: OpenAI API Provider

**Ziel**

Der OpenAI-Provider verbindet sich mit der ChatGPT-API und liefert Text.

**Umfang**

Enthalten: das neue Modul `lib/plugin/provider/openai` (`ai-ghost-provider-openai`) mit
`@AiProviderInfo`, `OpenAiProviderConfig` (API-Schlüssel als `secret`, Modellname, Basis-URL für
kompatible Endpunkte, Organisation optional) samt UI-Annotations und `en`/`de`-`ResourceBundle`, und
`generate()` gegen die OpenAI-Chat-Completions-API mit Streaming und Abbruch; Tests gegen einen
Fake-Endpunkt.

Nicht enthalten: die Orchestrierung, das Verdrahten einer KI-Schaltfläche.

**Betroffene Bereiche**

Neues Modul `lib/plugin/provider/openai`, `settings.gradle.kts`, das eingebaute Plugin-Verzeichnis,
eine neue HTTP-Client- bzw. SDK-Abhängigkeit (isoliert im Modul-JAR).

**Abhängigkeiten**

IP-03. Atomar und unabhängig von den übrigen Provider-Plänen.

**Erwartetes Ergebnis**

Nach dem Start steht der OpenAI-Provider in der Auswahl; mit hinterlegtem Schlüssel liefert
„Verbindung testen“ echten Text von der API.

**Technische Überlegungen**

HTTP-Client oder OpenAI-SDK ist eine **neue Drittanbieter-Abhängigkeit** (Rückfrage; Lizenz auf der
Allowlist), allein im Modul-JAR. Der Schlüssel liegt ausschließlich im `SecretStore` aus IP-03.

### IP-08: Anthropic API Provider

**Ziel**

Der Anthropic-Provider verbindet sich mit der Claude-API und liefert Text.

**Umfang**

Enthalten: das neue Modul `lib/plugin/provider/anthropic` (`ai-ghost-provider-anthropic`) mit
`@AiProviderInfo`, `AnthropicProviderConfig` (API-Schlüssel als `secret`, Modellname,
`anthropic-version`, Basis-URL) samt UI-Annotations und `en`/`de`-`ResourceBundle`, und `generate()`
gegen die Anthropic-Messages-API mit Streaming und Abbruch; Tests gegen einen Fake-Endpunkt.

Nicht enthalten: die Orchestrierung, das Verdrahten einer KI-Schaltfläche.

**Betroffene Bereiche**

Neues Modul `lib/plugin/provider/anthropic`, `settings.gradle.kts`, das eingebaute
Plugin-Verzeichnis, eine neue HTTP-Client- bzw. SDK-Abhängigkeit (isoliert im Modul-JAR).

**Abhängigkeiten**

IP-03. Atomar und unabhängig von den übrigen Provider-Plänen.

**Erwartetes Ergebnis**

Nach dem Start steht der Anthropic-Provider in der Auswahl; mit hinterlegtem Schlüssel liefert
„Verbindung testen“ echten Text von der API.

**Technische Überlegungen**

HTTP-Client oder Anthropic-SDK ist eine **neue Drittanbieter-Abhängigkeit** (Rückfrage; Lizenz auf
der Allowlist), allein im Modul-JAR.

### IP-09: llama.cpp Provider

**Ziel**

Der llama.cpp-Provider lädt eine lokale Modelldatei direkt und liefert Text.

**Umfang**

Enthalten: das neue Modul `lib/plugin/provider/llama-cpp` (`ai-ghost-provider-llama-cpp`) mit
`@AiProviderInfo`, `LlamaCppProviderConfig` (Pfad zur Modelldatei, Kontextgröße, Thread-Zahl,
GPU-Layer …) samt UI-Annotations und `en`/`de`-`ResourceBundle`, und `generate()` über einen
direkten Zugriff auf llama.cpp (native Bindung bzw. gebündelte Bibliothek) mit Streaming und
Abbruch; Tests, soweit ohne Modelldatei möglich, plus ein optionaler, kennzeichnungspflichtiger
Integrationstest mit einem kleinen Modell.

Nicht enthalten: die Orchestrierung, das Verdrahten einer KI-Schaltfläche.

**Betroffene Bereiche**

Neues Modul `lib/plugin/provider/llama-cpp`, `settings.gradle.kts`, das eingebaute
Plugin-Verzeichnis, eine neue native llama.cpp-Bindung bzw. Bibliothek (isoliert im Modul-JAR).

**Abhängigkeiten**

IP-03. Atomar und unabhängig von den übrigen Provider-Plänen.

**Erwartetes Ergebnis**

Nach dem Start steht der llama.cpp-Provider in der Auswahl; mit gültigem Modellpfad liefert
„Verbindung testen“ echten Text aus dem lokalen Modell.

**Technische Überlegungen**

Die native Bindung ist die größte Abhängigkeitsentscheidung des Features: JNI/FFM-Bindung gegenüber
gebündelter Bibliothek, plattformabhängige Artefakte, Auswirkung auf das jlink-Image – vor Beginn
mit dem Nutzer abzustimmen (`dependencies.md`). Der Provider führt keine Orchestrierung aus.

## 8. Abhängigkeitsgraph

```text
IP-01 (COMPLETED) ── IP-02 ── IP-03 ─┬─ IP-04 ── IP-05  (Packaging/CI, dann Documentation)
                                      ├─ IP-06  (LM Studio)
                                      ├─ IP-07  (OpenAI)
                                      ├─ IP-08  (Anthropic)
                                      └─ IP-09  (llama.cpp)
```

* Der Kern (IP-01 bis IP-05) ist ohne echte LLM-Verbindung vollständig; der Nachweis läuft über den
  Stub aus IP-02, die Anleitung über den Stub aus IP-05.
* IP-06 bis IP-09 sind je für sich atomar, voneinander unabhängig, hängen nur an IP-03 und lassen
  sich einzeln und später umsetzen. Jeder bringt ein eigenes Modul unter `lib/plugin/provider` mit
  eigenem Plugin-JAR und eine eigene, im JAR isolierte Drittanbieter-Abhängigkeit.
* `lib/ai` und die `TODO("AI action: …")`-Rümpfe werden von keinem dieser Pläne berührt; ihr
  Verdrahten ist ein eigenes Orchestrierungs-Feature.

## 9. Risiken und offene Fragen

### Getroffene Entscheidungen

* **Isolation:** ein `URLClassLoader` je Plugin, keine JPMS-Modulschicht (klein gehalten). Eine
  Config-Klasse im nicht-modularen Plugin-JAR ist damit von sich aus reflektierbar; ein modulares
  Plugin muss `opens`.
* **Reflection:** `kotlin-reflect` im Manager-Modul.
* **Id-Kollision:** „zuerst geladen gewinnt“ – das eingebaute Verzeichnis wird zuerst gelesen, also
  gewinnt ein eingebauter Provider vor einem gleichnamigen Nutzer-Plugin; bei zwei Nutzer-Plugins
  das zuerst geladene.
* **Provider-Paketierung:** je mitgeliefertem Provider ein eigenes Modul unter `lib/plugin/provider`
  mit eigenem Plugin-JAR. Die Abhängigkeit eines Providers bleibt so – zusätzlich zur
  `URLClassLoader`-Isolation – von den anderen getrennt; ein geteilter Klassenpfad entsteht nicht.
* **Geheimnis-Ablage:** ein `SecretStore` nach dem Muster von IntelliJ PasswordSafe –
  OS-Schlüsselbund primär, verschlüsselte Datei als Fallback, hinter einer schmalen Schnittstelle;
  zunächst klein.
* **i18n:** wird in IP-03 umgesetzt; `label`/`help` werden gegen ein plugin-eigenes `ResourceBundle`
  aufgelöst (Fallback: Schlüssel-Klartext), die mitgelieferten Provider bringen zunächst `en` und
  `de` mit; die App-Bundles sind nie beteiligt.
* **Feldtypen:** `STRING`, `BOOLEAN`, `INT`, `LONG`, `DOUBLE`, `ENUM` reichen für alle fünf
  mitgelieferten Provider; für Sonderfälle bleibt `configSchema()` der Ausweg.
* **Versionierung:** alle Artefakte tragen die Repo-Version; `ai-ghost-plugin-api` wird nicht
  eigenständig versioniert. Die Kompatibilität regelt eine Vertragsversion in `@AiProviderInfo`, die
  nur bei einem Bruch des Provider-Vertrags steigt und vom Manager geprüft wird.
* **Verzeichnis-Änderung:** wirkt beim nächsten Start; kein Live-Neuscan.
* **Provider-Wahl:** global, nicht je Aktion (Letzteres gehört zum Orchestrierungs-Feature).

### Weiterhin offen

* **Ermittlung des Installationsverzeichnisses** – der Ort von `<Install>/plugins` muss aus dem
  jpackage-/jlink-Image-Layout zuverlässig auf allen Plattformen gefunden werden; ein
  Entwicklungslauf ohne Image braucht einen Ersatz. In IP-01 zu klären.
* **Drittanbieter-Abhängigkeiten der Anbindungen** – HTTP-Client (IP-06/07/08), OpenAI-/Anthropic-SDK
  (IP-07/08), native llama.cpp-Bindung (IP-09). Jede braucht die Zustimmung des Nutzers und eine
  Lizenz auf der Allowlist, bevor der jeweilige Plan beginnt. Ebenso eine etwaige Bibliothek für den
  OS-Schlüsselbund-Zugriff in IP-03.
* **llama.cpp-Bindung** – JNI/FFM gegenüber gebündelter Bibliothek, plattformabhängige Artefakte,
  jlink-Image. Die schwerste Einzelentscheidung; betrifft nur IP-09.
* **Name und Umfang der `verifyPlugin`-Aufgabe** – `CLAUDE.md` und der `ci-pipeline`-Skill nennen
  sie, der Build kennt sie nicht. IP-04 legt sie an.

## 10. Abschlusskriterien des Features

Der Kern gilt als abgeschlossen, wenn IP-01 bis IP-05 erfüllt sind:

* Ein Plugin, das den Provider-Vertrag erfüllt und im eingebauten Verzeichnis
  (`<Installationsverzeichnis>/plugins`, fix) oder im Nutzer-Verzeichnis liegt, erscheint nach einem
  Neustart in der Provider-Auswahl; ein defektes oder in der Vertragsversion unpassendes Plugin wird
  gemeldet und übersprungen, ohne den Start zu stoppen.
* Das Nutzer-Plugin-Verzeichnis ist in der Preferences-UI einstellbar (Vorgabe
  `~/.ai-ghost/plugins`), wird gemäß `fx-model` gespiegelt und persistiert, und eine Änderung wirkt
  beim nächsten Start.
* Ein Provider beschreibt seine Konfiguration als annotiertes Datenmodell **oder** über ein eigenes
  `configSchema()`; beide Wege ergeben dieselbe `List<ConfigField>`, und die UI baut und validiert
  das Formular allein daraus, rendert ein `secret`-Feld als Passwortfeld und reicht dem Provider die
  befüllte Instanz. Ein nicht unterstützter Feldtyp meldet den Provider als fehlkonfiguriert.
* Die gewählte Provider-Id und die nicht geheimen Feldwerte überstehen einen Neustart über
  `preferences.yml`; ein geheimes Feld steht ausschließlich im `SecretStore` (OS-Schlüsselbund nach
  dem Muster von IntelliJ PasswordSafe, verschlüsselte Datei als Fallback), nie im Projektdokument
  und nie in `preferences.yml`.
* Die Feldbeschriftungen der mitgelieferten Provider erscheinen je nach Anwendungssprache in
  Englisch oder Deutsch, aufgelöst über ein plugin-eigenes `ResourceBundle`.
* Kollidieren zwei Provider auf derselben Id, gewinnt der zuerst geladene (eingebautes Verzeichnis
  vor Nutzer-Verzeichnis).
* Der **Stub-Provider** wird als Plugin-JAR des Moduls `lib/plugin/provider/stub` über das eingebaute
  Verzeichnis ausgeliefert, läuft durch denselben Plugin-Manager wie ein fremdes Plugin, gibt seinen
  über die UI änderbaren Standardtext gestreamt zurück und bleibt dauerhaft bestehen. Er ist der
  vollständige Nachweis der Plugin-Funktionalität.
* Unter `lib/plugin/provider` besteht je mitgeliefertem Provider ein Modul mit eigenem Plugin-JAR;
  für Stub ist alles umgesetzt, für LM Studio, OpenAI, Anthropic und llama.cpp Config-Modell und UI,
  die Client-Anbindung je in IP-06 bis IP-09.
* Eine Gradle-Aufgabe paketiert und prüft ein Provider-Plugin-JAR, die CI führt sie aus, und
  `.claude/rules` benennt `lib/plugin` mit `api`, `manager` und `provider/*`. Alle Artefakte tragen
  die Repo-Version.
* MkDocs trägt den Menüpunkt „Plugins“ mit einer Übersichtsseite zum Mechanismus und dem Bereich
  „AI Provider“ mit den drei Seiten API, Entwicklung und Beispiel Stub; der „Build and verify
  MkDocs“-Schritt der CI bleibt grün.
* Jede plugin-seitige API liegt im bestehenden `ai-ghost-plugin-api` (nur `kotlin.stdlib`); es
  wurde kein weiteres plugin-seitiges Modul angelegt. `ai-ghost-plugin-manager` hängt nur von
  `ai-ghost-plugin-api`, ClassGraph und `kotlin-reflect` ab und lädt Plugins über einen
  `URLClassLoader` je Plugin. Kein `lib`-Modul außerhalb der Architektur-Ausnahmen bekommt JavaFX.
  `lib/ai` ist unverändert.
* Die `TODO("AI action: …")`-Rümpfe in `app/ui` sind unverändert; dieses Feature erzeugt keinen
  KI-Text im Editor. Das Verdrahten und die Orchestrierung sind ein eigenes Feature.
* Build und Tests sind grün, Dokumentation und Changelog sind gemäß `project-docs` aktualisiert.

Die konkreten Anbindungen (IP-06 bis IP-09) gelten je für sich als abgeschlossen, wenn ihr Provider
im Plugin-Verzeichnis erscheint, konfigurierbar ist und „Verbindung testen“ echten Text aus der
jeweiligen Quelle liefert.
