# IP-08: Anthropic-API-Provider

## Herkunft

* Feature Plan: `.claude/plans/features/FP-002-AiProviderPlugins.md`
* Plan-ID im Feature Plan: IP-08
* Status-Datei des Features: `.claude/plans/features/FP-002-AiProviderPlugins-status.md`

## Abhängigkeiten

* Voraussetzung: IP-03
* Start erst, wenn IP-03 im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Unabhängig von IP-04, IP-05, IP-06, IP-07, IP-09.

## Zu ladende Skills

* `ci-pipeline`
* `testing`
* `project-docs`

## Aufgaben

### 1. Modul

* `lib/plugin/provider/anthropic` als `ai-ghost-provider-anthropic` in `settings.gradle.kts`.
* Provider-Modul-Baustein aus IP-02 verwenden.
* HTTP-Client bzw. Anthropic-SDK vorher mit dem Nutzer abstimmen; Lizenz auf der Allowlist.

### 2. Config-Modell

* `AnthropicProviderConfig`: `apiKey` (`secret`), `modelName`, `anthropicVersion`, `baseUrl`.
* `@AiProviderConfigField` je Feld; `en`- und `de`-`ResourceBundle`.

### 3. Client

* `generate()` gegen die Messages-API mit Streaming und Abbruch.
* Schlüssel aus dem gereichten `AiProviderConfig`; nie protokollieren.
* Fehler über `onError`, keine Ausnahme nach außen.

### 4. Tests

* Gegen einen Fake-Endpunkt: Strom, Abbruch, Fehler, fehlender Schlüssel.
* `List<ConfigField>` vollständig; Bundle-Auflösung `en`/`de`.

### 5. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen; Pipeline nach `ci-pipeline` gegen das neue Modul prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-002-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Der Anthropic-Provider steht in der Auswahl; mit hinterlegtem Schlüssel liefert „Verbindung testen“ echten Text.
