# IP-03: Provider-Auswahl, Konfiguration und Persistenz

## Herkunft

* Feature Plan: `.claude/plans/features/FP-002-AiProviderPlugins.md`
* Plan-ID im Feature Plan: IP-03
* Status-Datei des Features: `.claude/plans/features/FP-002-AiProviderPlugins-status.md`

## Abhängigkeiten

* Voraussetzung: IP-02
* Start erst, wenn IP-02 im Feature-Status `COMPLETED` ist.
* Blockiert: IP-04, IP-06, IP-07, IP-08, IP-09
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `fx-model`
* `ui-styling`
* `fx-component-lifecycle`
* `icons`
* `testing`
* `project-docs`

## Aufgaben

### 1. Preferences-Modell

* In `ai-ghost-model` Gruppe `AiProviders`: `selectedProviderId: String?`, `values: Map<String, Map<String, String>>`, `userPluginDirectory: String`.
* Vorgabe `userPluginDirectory` = `~/.ai-ghost/plugins`; `@JsonIgnoreProperties(ignoreUnknown = true)`.
* `Preferences` um `aiProviders` erweitern, `@JsonPropertyOrder` anpassen.

### 2. FX-Modell

* Nach `fx-model`: `AiProvidersProperty` in `ai-ghost-fx-model`, Paket gespiegelt.
* Verschachtelte Änderungspropagation für Id, Werte und Verzeichnis.
* Property-Tests nach `fx-model`.

### 3. SecretStore

* Schnittstelle `SecretStore` mit `get/set/remove(providerId, fieldName)`.
* Implementierung: OS-Schlüsselbund primär, verschlüsselte Datei als Fallback.
* Eine Bibliothek für den Schlüsselbund-Zugriff vorher mit dem Nutzer abstimmen.

### 4. Startschritt anpassen

* Der Startschritt aus IP-01 liest `userPluginDirectory` aus `Preferences` statt der festen Vorgabe.

### 5. Persistenz-Verdrahtung

* Je Provider aus `List<ConfigField>` Werte laden: nicht geheim aus `Preferences`, geheim aus `SecretStore`.
* Fehlendes Feld auf die Vorgabe der `List<ConfigField>` zurückfallen lassen.
* Beim Speichern zurückschreiben; die Instanz über den Manager befüllen.

### 6. Einstellungsabschnitt

* Nach `ui-styling`: MVVM-FX-Trio, FXML, CSS, Nachrichtenbündel.
* Provider-Liste; je `ConfigFieldType` ein Steuerelement (Text, Passwort, Kontrollkästchen, Zahl, Auswahl).
* `help` als Hinweis, `required`-Markierung, Typ- und Pflichtprüfung.
* Feld „Plugin-Verzeichnis“ mit `DirectoryChooser`, gebunden an `userPluginDirectory`, Hinweis „wirkt beim nächsten Start“.
* Nach `fx-component-lifecycle` alle Bindungen beim Schließen lösen.

### 7. Verbindungstest

* Schaltfläche ruft `AiProvider.generate()` mit kurzem festem Prompt, nicht auf dem FX-Thread.
* Ergebnis bzw. Fehler anzeigen; Icon nach `icons`.

### 8. Tests

* Nach `testing`: Property-Tests des FX-Modells, Persistenz-Roundtrip nicht geheim und geheim.
* Formularbau aus einer `List<ConfigField>`-Testeingabe, ohne echtes Plugin.
* Verbindungstest gegen den Stub zeigt `responseText`.
* Geändertes Verzeichnis wird beim nächsten Start gescannt (headless).

### 9. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-002-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* Ein Nutzer wählt, konfiguriert, testet und speichert den Stub; die Wahl übersteht einen Neustart.
* Ein geheimes Feld liegt nur im `SecretStore`, nie in `preferences.yml`.
