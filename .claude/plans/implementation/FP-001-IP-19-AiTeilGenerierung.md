# IP-19: KI-Schaltfläche zur Teil-Generierung

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-19
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-12
* Start erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Blockiert: keinen weiteren Plan
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `ui-styling`
* `fx-component-lifecycle`
* `testing`
* `icons`
* `project-docs`

## Harte Einschränkung

* KEINE Verdrahtung an den `lib/ai`-Aktions-Port, KEIN Stub, KEIN Mock, KEINE reale oder simulierte KI-Anbindung
* Der Aktions-Port aus IP-17 bleibt bestehen, wird von diesem Plan aber nicht benutzt
* Die Schaltfläche ist per FXML `onAction` an eine parameterlose Methode des `InspectorView`-Controllers gebunden
* Der einzige Rumpf dieser Methode ist `TODO("AI action: generate-part")`
* Keine Streaming-Anzeige, kein vorläufiger Zustand, kein Annehmen/Verwerfen, kein Auflösen
* Die gesamte Generierungsinfrastruktur kommt erst mit dem künftigen Plugin-System-Feature

## Aufgaben

### 1. Schaltfläche

* Im Inspector-Abschnitt des Teils eine einzige KI-Schaltfläche „Teil generieren“ anbieten.
* Icon nach `icons` anlegen.
* Beschriftung und Tooltip aus dem Nachrichtenbündel.

### 2. Leere Methode

* Parameterlose Methode im `InspectorView`-Controller anlegen.
* Methode per FXML `onAction` verdrahten.
* Rumpf nur `TODO("AI action: generate-part")`.
* Keine weitere Logik, kein Aufruf, keine Rückgabe.

### 3. Tests

* Prüfen, dass die Schaltfläche vorhanden und aktivierbar ist.
* Prüfen, dass die Methode `NotImplementedError` wirft.

### 4. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Eine KI-Schaltfläche „Teil generieren“ steht im Inspector-Teilabschnitt.
* Sie endet an einer leeren `InspectorView`-Methode mit `TODO("AI action: generate-part")`.
* KEINE Generierung, kein vorläufiger Zustand, kein Provider ist Teil dieses Plans.
