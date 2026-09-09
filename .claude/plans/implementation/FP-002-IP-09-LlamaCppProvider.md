# IP-09: llama.cpp-Provider

## Herkunft

* Feature Plan: `.claude/plans/features/FP-002-AiProviderPlugins.md`
* Plan-ID im Feature Plan: IP-09
* Status-Datei des Features: `.claude/plans/features/FP-002-AiProviderPlugins-status.md`

## Abhängigkeiten

* Voraussetzung: IP-03
* Start erst, wenn IP-03 im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Unabhängig von IP-04, IP-05, IP-06, IP-07, IP-08.

## Zu ladende Skills

* `ci-pipeline`
* `testing`
* `project-docs`

## Aufgaben

### 1. Modul und Bindung

* `lib/plugin/provider/llama-cpp` als `ai-ghost-provider-llama-cpp` in `settings.gradle.kts`.
* Provider-Modul-Baustein aus IP-02 verwenden.
* Native Bindung wählen (JNI/FFM gegenüber gebündelter Bibliothek) und mit dem Nutzer abstimmen.
* Plattformabhängige Artefakte und Auswirkung auf das jlink-Image klären.

### 2. Config-Modell

* `LlamaCppProviderConfig`: `modelPath`, `contextSize`, `threadCount`, `gpuLayers`.
* `@AiProviderConfigField` je Feld; `en`- und `de`-`ResourceBundle`.

### 3. Anbindung

* `generate()` lädt das Modell über die native Bindung und streamt Tokens.
* `cancel()` stoppt die Generierung; Fehler über `onError`.
* Modell zwischen Aufrufen halten oder je Aufruf laden; die Wahl im Modul dokumentieren.

### 4. Tests

* Ohne Modelldatei: Fehlerpfad, `List<ConfigField>`, Bundle-Auflösung.
* Optionaler, kennzeichnungspflichtiger Integrationstest mit kleinem Modell; Kategorie nach `testing`.

### 5. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen; Pipeline nach `ci-pipeline` gegen das neue Modul prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-002-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Der llama.cpp-Provider steht in der Auswahl; mit gültigem Modellpfad liefert „Verbindung testen“ echten Text.
