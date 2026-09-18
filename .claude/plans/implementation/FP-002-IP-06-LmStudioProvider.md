# IP-06: LM-Studio-Provider

## Herkunft

* Feature Plan: `.claude/plans/features/FP-002-AiProviderPlugins.md`
* Plan-ID im Feature Plan: IP-06
* Status-Datei des Features: `.claude/plans/features/FP-002-AiProviderPlugins-status.md`

## Abhängigkeiten

* Voraussetzung: IP-03
* Start erst, wenn IP-03 im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Unabhängig von IP-04, IP-05, IP-07, IP-08, IP-09.

## Zu ladende Skills

* `ci-pipeline`
* `testing`
* `project-docs`

## Aufgaben

### 1. Modul

* `lib/plugin/provider/lm-studio` als `ai-ghost-provider-lm-studio` in `settings.gradle.kts`.
* Provider-Modul-Baustein aus IP-02 verwenden; Abhängigkeiten nur `ai-ghost-plugin-api` plus HTTP-Client.
* HTTP-Client vorher mit dem Nutzer abstimmen; Lizenz auf der Allowlist.

### 2. Config-Modell

* `LmStudioProviderConfig`: `baseUrl`, `port`, `modelName`, `timeoutSeconds`.
* `@AiProviderConfigField` je Feld; `en`- und `de`-`ResourceBundle`.

### 3. Client

* `generate()` sendet System- und User-Prompt an das OpenAI-kompatible HTTP-API von LM Studio.
* Antwort als Chunk-Strom weiterreichen; `cancel()` bricht die Verbindung ab.
* Fehler über `onError` melden, keine Ausnahme nach außen.

### 4. Tests

* Gegen einen lokalen Fake-Server: erfolgreicher Strom, Abbruch, Fehlerfall.
* `List<ConfigField>` vollständig; Bundle-Auflösung `en`/`de`.

### 5. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen; Pipeline nach `ci-pipeline` gegen das neue Modul prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-002-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Der LM-Studio-Provider steht in der Auswahl; „Verbindung testen“ liefert echten Text vom lokalen Server.
