# IP-04: Plugin-Paketierung und CI

## Herkunft

* Feature Plan: `.claude/plans/features/FP-002-AiProviderPlugins.md`
* Plan-ID im Feature Plan: IP-04
* Status-Datei des Features: `.claude/plans/features/FP-002-AiProviderPlugins-status.md`

## Abhängigkeiten

* Voraussetzung: IP-03
* Start erst, wenn IP-03 im Feature-Status `COMPLETED` ist.
* Blockiert: IP-05
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ci-pipeline`
* `testing`
* `project-docs`

## Aufgaben

### 1. Paketier-Aufgabe

* Gradle-Aufgabe `verifyPlugin` (Name endgültig festlegen): paketiert ein Provider-Plugin-JAR.
* Prüft Service- bzw. Manifest-Eintrag, `@AiProviderInfo` und die Vertragsversion gegen `ai-ghost-plugin-api`.
* Auf jedes Modul unter `lib/plugin/provider` angewandt; `check` und `build` hängen davon ab.
* Baut auf dem Provider-Modul-Baustein aus IP-02 auf.

### 2. CI

* Nach `ci-pipeline`: den Schritt „Verify plugin“ in `ci.yml` und `release.yml` einbinden.
* Die bestehende Parallelstruktur der Pipelines wahren.

### 3. Architekturregel

* `.claude/rules/architecture.md`: `lib/plugin` trägt `api`, `manager` und `provider/*`, alle ohne JavaFX.

### 4. KDoc und Changelog

* KDoc auf allen öffentlichen Typen von `ai-ghost-plugin-api`.
* CHANGELOG-Eintrag nach `project-docs`.

### 5. Tests

* Aufgabe scheitert bei falscher Vertragsversion, fehlendem Service-Eintrag, fehlendem `@AiProviderInfo`.
* Aufgabe ist grün für das Stub-Modul.

### 6. Abschluss

* Build über Agent (Task-Tool) ausführen; grün.
* Dokumentation nach `project-docs` prüfen.
* Diesen Plan im Feature-Status auf `COMPLETED` setzen, Fortschritt neu berechnen.
* Eintrag in `FP-002-Overview.md` und im Feature Plan als erledigt markieren.
* Plandatei mit `git rm` im selben Änderungssatz entfernen.

## Ergebnis

* `./gradlew build` paketiert und prüft jedes Provider-JAR; die CI führt den Schritt aus.
