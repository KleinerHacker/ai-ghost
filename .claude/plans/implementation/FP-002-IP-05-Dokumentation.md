# IP-05: Dokumentation

## Herkunft

* Feature Plan: `.claude/plans/features/FP-002-AiProviderPlugins.md`
* Plan-ID im Feature Plan: IP-05
* Status-Datei des Features: `.claude/plans/features/FP-002-AiProviderPlugins-status.md`

## Abhängigkeiten

* Voraussetzung: IP-04
* Start erst, wenn IP-04 im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `project-docs`

## Aufgaben

### 1. Navigation

* Menüpunkt „Plugins“ in der MkDocs-Konfiguration anlegen.
* Darunter der Bereich „AI Provider“ mit drei Seiten.

### 2. Übersichtsseite

* Zwei Plugin-Orte: eingebautes `plugins/` (fix); Nutzer-Verzeichnis (Vorgabe `~/.ai-ghost/plugins`, in Preferences änderbar, wirkt beim nächsten Start).
* Einlesen: Scan beim Start, `URLClassLoader` je Plugin, Vertragsversions-Prüfung, Registrierung, Fehlerisolation.
* Id-Kollision: „zuerst geladen gewinnt“.
* Umgang mit einem defekten Plugin.

### 3. Seite „API“

* `AiProvider` (Prompt-Strom-Vertrag, Handle, Abbruch, Fehler).
* `@AiProviderInfo` samt Vertragsversion, `AiProviderConfig`, `@AiProviderConfigField`.
* `ConfigField`/`ConfigFieldType`, `configType`, `configSchema()`.
* Jeder Typ mit Zweck und Signatur.

### 4. Seite „Entwicklung“

* Abhängigkeiten: nur `ai-ghost-plugin-api` plus die eigenen des Providers.
* Klassen: Provider und Config-Modell; Registrierung über `configType` und Service-/Manifest-Eintrag.
* `opens` des Config-Pakets bei modularen Plugins.
* JAR bauen, in ein Plugin-Verzeichnis legen; i18n über ein eigenes `ResourceBundle` (`en`, `de`).

### 5. Seite „Beispiel: Stub“

* `StubProviderConfig` mit Annotations, Verweis auf das Modul `lib/plugin/provider/stub`.
* `generate()` mit gestreamtem `responseText`.
* Paketierung und Ergebnis in der Konfigurations-UI.

### 6. Abschluss

* MkDocs-Build lokal prüfen; „Build and verify MkDocs“ der CI grün.
* Build über Agent (Task-Tool) ausführen; grün.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-002-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Ein Autor findet unter „Plugins“ die vollständige Anleitung und kann den Stub nachbauen.
