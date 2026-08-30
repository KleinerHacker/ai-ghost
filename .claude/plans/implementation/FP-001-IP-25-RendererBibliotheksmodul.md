# IP-25: Renderer-Bibliotheksmodul

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-25
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: keine
* Blockiert durch offene Entscheidung: JavaFX als Abhängigkeit eines Bibliotheksmoduls.
* Blockiert: IP-26, IP-07, IP-08, IP-27, IP-28
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `ci-pipeline`
* `project-docs`

## Aufgaben

### 1. Entscheidung einholen

* Nutzer nach JavaFX als Abhängigkeit eines Bibliotheksmoduls fragen.
* Ohne Zustimmung den Plan nicht beginnen.

### 2. Modul anlegen

* Verzeichnis `lib/layouting-fx` anlegen und mit `git add` aufnehmen.
* In `settings.gradle.kts` als `ai-ghost-layouting-fx` eintragen.
* `build.gradle.kts` mit `java-library` und Lizenzkopf anlegen.

### 3. Abhängigkeiten

* `api(project(":lib:ai-ghost-layouting"))` eintragen.
* JavaFX-Module `javafx.controls` und `javafx.graphics` eintragen.
* Keine weitere Abhängigkeit aufnehmen.

### 4. JPMS

* `module-info.java` mit `requires` auf Layouting-Kern und JavaFX anlegen.
* Ziel von `compileJava` auf das Kotlin-Ausgabeverzeichnis setzen, wie in den Nachbarmodulen.

### 5. Architekturregel

* `.claude/rules/architecture.md` um den erlaubten Fall ergänzen.
* JavaFX erlaubt in `app/ui` und in einer JavaFX-Komponentenbibliothek.
* Verbot für alle übrigen Module unverändert lassen.

### 6. Testaufbau

* TestFX headless im Testsatz des Moduls einrichten.
* Aufbau von `app/ui` als Vorlage nehmen, Abweichungen dokumentieren.
* Einen Rauchtest schreiben, der eine leere Szene startet.

### 7. Pipeline

* Modul in Build, Test und Abdeckung der Workflows aufnehmen.
* Bildschirmlosen Betrieb der TestFX-Tests im Runner sicherstellen.

### 8. Abschluss

* Build über Agent ausführen.
* jlink-Image von `app/ui` auf Lauffähigkeit prüfen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Das Modul baut und testet leer.
* JavaFX löst innerhalb eines Bibliotheksmoduls auf.
* Die Architekturregel benennt den einen erlaubten Fall.
