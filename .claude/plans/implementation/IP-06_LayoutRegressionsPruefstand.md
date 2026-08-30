# IP-06: Layout-Regressionsprüfstand

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-06
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: IP-04, IP-07, IP-08
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `ci-pipeline`
* `project-docs`

## Aufgaben

### 1. Beispielprojekte

* Kurzes Teil, langes Teil, Blocksatz, mehrere Designs, gerade und ungerade Seiten.
* Projekt mit ausgeschaltetem Prolog und mit eingeschaltetem Prolog.
* Alle gegen die deterministische Messung aus IP-03 erzeugen.

### 2. Golden Files

* Seitenstruktur als Zahlen einchecken, nicht als Bild.
* Abweichung mit Angabe der betroffenen Zeile melden.
* Weg zum Neuerzeugen einer Golden File dokumentieren.

### 3. Flächenvergleich

* Umbruchpositionen von `PaperFlowView` gegen die Seiten von `PaperPageView` prüfen.
* Test headless über TestFX ausführen.

### 4. Gradle

* Task anlegen, der Golden Files und Flächenvergleich ausführt.
* Task in den `build`-Lauf einhängen.

### 5. Abschluss

* Build über Agent ausführen.
* Pipeline nach `ci-pipeline` prüfen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Eine geänderte Seitenstruktur bricht den Build, solange die Golden Files nicht bewusst erneuert wurden.
* Schreibfläche und Vorschau können nicht unbemerkt auseinanderlaufen.
