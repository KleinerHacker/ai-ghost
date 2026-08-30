# IP-03: Layout-Kern

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-03
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: IP-01, IP-02, IP-24
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: IP-04
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `ci-pipeline`
* `project-docs`

## Aufgaben

### 1. Modul

* Modul `lib/layout` als `ai-ghost-layout` in `settings.gradle.kts` aufnehmen.
* Abhängigkeit auf `ai-ghost-model`, kein Toolkit.
* `module-info.java` anlegen.

### 2. Messschnittstelle

* `TextMetrics` als Interface im Modul definieren.
* Deterministische Implementierung für Tests mitliefern.

### 3. Aufgelöster Stil

* Familie, Grad, Schnitt, Ausrichtung, Zeilenabstand und Abstände davor und danach bündeln.
* Auflösung aus `Design` je Elementklasse.

### 4. Blockmodell

* Blocktypen für Titelseite, Copyright-Seite, Überschrift, Überschriftzeile, Absatz und Klappentext.
* Autor und Copyright aus `Meta` in die beiden Seitenblöcke übernehmen.

### 5. Zeilenumbruch

* Umbruch gegen eine vorgegebene Spaltenbreite über `TextMetrics`.
* Ausrichtung links, rechts, zentriert und Blocksatz.
* Umbruchschritt hinter einem Interface kapseln.
* Jede gesetzte Zeile auf Absatzindex und Zeichenbereich abbilden.

### 6. Tests

* Umbruch und Ausrichtung gegen die deterministische Messung prüfen.
* Rückabbildung auf den Zeichenbereich prüfen.

### 7. Abschluss

* Build über Agent ausführen.
* Pipeline nach `ci-pipeline` prüfen, neues Modul.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Ein Teil und ein Design ergeben eine reproduzierbare Folge gesetzter Zeilen.
* Das Ergebnis enthält keinen Toolkit-Typ.
