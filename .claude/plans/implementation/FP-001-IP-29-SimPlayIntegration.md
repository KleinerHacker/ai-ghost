# IP-29: simPlay-Integration

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-29
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: keine
* Blockiert: IP-30, IP-34 (und darüber den Rest des Features)
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ci-pipeline`
* `testing`
* `project-docs`

## Harte Einschränkung

* simPlay ist die einzige neue Drittanbieter-Abhängigkeit; keine weitere ohne Rückfrage.
* Fehlt eine Lizenz in der Allowlist, wird sie dem Nutzer vorgelegt, nicht selbst ergänzt.

## Aufgaben

### 1. Repository

* GitHub-Packages-Repository `https://maven.pkg.github.com/KleinerHacker/simPlay` im Wurzel-Build ergänzen.
* Zugangsdaten aus Umgebungsvariablen bzw. `gradle.properties` lesen, nie einchecken.
* `publishToMavenLocal`-Weg als Alternative für die lokale Entwicklung dokumentieren.

### 2. Abhängigkeit

* Exakte simPlay-Version an einer Stelle pinnen (Konstante bzw. Version-Catalog); mindestens 0.2.1 wegen `FxFontProbe`/`FontFingerprint` (IP-34).
* `org.pcsoft.framework:simplay-engine` als `api` in `lib/ai-ghost-layouting-model` vorsehen (Verdrahtung in IP-30).
* `org.pcsoft.framework:simplay-fx` als `implementation` in `app/ui` vorsehen (Verdrahtung in IP-31).
* JPMS-Modulnamen von `simplay-engine`/`simplay-fx` ermitteln und notieren.

### 3. Eigenbaumodule entfernen

* `lib/ai-ghost-layouting` per `git rm -r` entfernen.
* `lib/ai-ghost-layouting-fx` per `git rm -r` entfernen (Schrift-Stack bleibt größtenteils in `simplay-fx`; Rest zieht in IP-34 nach `app/ui`).
* `settings.gradle.kts` um beide `include`/`project(...)`-Blöcke bereinigen.
* Verweise in `app/ui` und `lib/ai-ghost-layouting-model` vorübergehend brechen lassen; IP-30/IP-31/IP-34 heilen sie.

### 4. Architekturregel

* `.claude/rules/architecture.md`: JavaFX-Erlaubnis für ein `lib`-Modul zurücknehmen.
* Festhalten: JavaFX ausschließlich in `app/ui`, transitiv über `simplay-fx`.

### 5. Lizenzen

* Lizenzbericht (`licensee`, `jk1`, `cyclonedx`) mit simPlay und transitiven Abhängigkeiten laufen lassen.
* Fehlende Einträge (u. a. kotlinx-serialization, JavaFX-Artefakte) auflisten und dem Nutzer vorlegen.

### 6. Pipeline

* `.github`-Workflows um das GitHub-Packages-Secret ergänzen.
* Build-, Test- und Lizenz-Jobs auf die entfernten Module prüfen.
* `jlink`-Einbindung von `app/ui` (`addExtraDependencies`) auf simPlay-Bedarf prüfen.

### 7. Dokumentation

* README-Modultabelle: `ai-ghost-layouting`/`ai-ghost-layouting-fx` streichen, simPlay-Nutzung nennen.
* MkDocs-Seiten der entfernten Module löschen bzw. umleiten.
* `CHANGELOG.md`-Eintrag zur Umstellung auf simPlay.

### 8. Abschluss

* Build über Agent ausführen (Kompilierfehler in `app/ui`/`layouting-model` sind bis IP-30/IP-31/IP-34 erwartbar; separat vermerken).
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* simPlay ist über GitHub Packages beziehbar, lokal und in der CI.
* `lib/ai-ghost-layouting` und `lib/ai-ghost-layouting-fx` sind aus Repository und Build entfernt.
* Die Architekturregel nennt `app/ui` als einzigen JavaFX-Ort.
* simPlay-Lizenzen sind in der Allowlist oder liegen dem Nutzer zur Freigabe vor.
