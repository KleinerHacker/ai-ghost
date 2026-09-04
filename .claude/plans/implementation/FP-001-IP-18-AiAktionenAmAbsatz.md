# IP-18: KI-Schaltflächen an Absatz und Überschrift

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-18
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-10
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `component`
* `ui-styling`
* `fx-component-lifecycle`
* `testing`
* `icons`
* `project-docs`

## Harte Einschränkung

* KEINE Verdrahtung an den `lib/ai`-Aktions-Port, KEIN Stub, KEIN Mock, KEINE reale oder simulierte KI-Anbindung
* Der Aktions-Port aus IP-17 bleibt bestehen, wird von diesem Plan aber nicht benutzt
* Jede KI-Schaltfläche ist per FXML `onAction` an eine parameterlose Methode des `*View`-Controllers gebunden
* Der einzige Rumpf dieser Methode ist `TODO("AI action: <name>")`
* Keine Busy-Anzeige, kein Abbruch, kein Ersetzungspfad, kein Undo-Eintrag, kein Fehlerweg
* Die gesamte KI-Aktionsinfrastruktur kommt erst mit dem künftigen Plugin-System-Feature

## Aufgaben

### 1. Aktionsleiste

* `AiActionBar` schwebend am fokussierten Absatz oder an der Überschrift zeigen.
* Sichtbar nur, wenn die Maus über dem Absatz/der Überschrift steht.
* Halbtransparent, solange die Maus nicht direkt über der Leiste steht.
* Volle Deckkraft erst beim Hover direkt über der Leiste.
* Deckkraftwechsel als sanfte Animation (Fade), kein hartes Umschalten.
* Schaltflächen Umschreiben, Ausbauen und Kürzen anbieten.
* Icons nach `icons` anlegen.
* Beschriftungen und Tooltips aus dem Nachrichtenbündel.

### 2. Leere Methoden

* Je Schaltfläche eine parameterlose Methode im `*View`-Controller anlegen.
* Methoden per FXML `onAction` verdrahten.
* Rumpf jeweils nur `TODO("AI action: rewrite")`, `TODO("AI action: expand")`, `TODO("AI action: shorten")`.
* Keine weitere Logik, kein Aufruf, keine Rückgabe.

### 3. Tests

* Aktionsleiste, Sichtbarkeit bei Hover und Fade-Zustand headless prüfen.
* Prüfen, dass jede Methode `NotImplementedError` wirft.

### 4. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Die schwebende KI-Leiste steht sichtbar am fokussierten Block mit Umschreiben, Ausbauen und Kürzen.
* Jede Schaltfläche endet an einer leeren `*View`-Methode mit `TODO("AI action: …")`.
* KEINE KI-Infrastruktur, kein Port, kein Provider ist Teil dieses Plans.
