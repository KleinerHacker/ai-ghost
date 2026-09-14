# IP-18: KI-Schaltflächen an Absatz und Überschrift

## Herkunft

* Feature Plan: `.claude/plans/features/FP-001-PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-18
* Status-Datei des Features: `.claude/plans/features/FP-001-PaperWritingSurface-status.md`

## Abhängigkeiten

* Voraussetzung: IP-39 (vormals IP-31; IP-39 liefert die dauerhafte `PaperSheetView`)
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

* KEINE Verdrahtung an den `lib/ai`-Aktions-Port, KEIN Stub, KEIN Mock, KEINE reale oder simulierte KI-Anbindung.
* Der Aktions-Port aus IP-17 bleibt bestehen, wird von diesem Plan aber nicht benutzt.
* Jede KI-Schaltfläche ist per FXML `onAction` an eine parameterlose `*View`-Methode gebunden.
* Der einzige Rumpf dieser Methode ist `TODO("AI action: <name>")`.
* Keine Busy-Anzeige, kein Abbruch, kein Ersetzungspfad, kein Undo-Eintrag, kein Fehlerweg.

## Aufgaben

### 1. Schwebende Leiste

* `AiActionBar` als Inhalt eines simPlay-`FloatingOverlay`.
* Trigger `PARAGRAPH_HOVER`; im `EDITABLE`-Modus zusätzlich `CARET`.
* simPlay übernimmt Anzeigen, Positionieren und Verbergen sowie das Verankern beim Scrollen/Zoomen.
* `anchor`, `offsetX`, `offsetY` für die Platzierung an der Blockoberkante.

### 2. Erscheinung

* Schaltflächen Umschreiben, Ausbauen und Kürzen mit Icons nach `icons`.
* Halbtransparent, solange die Maus nicht direkt über der Leiste steht; volle Deckkraft beim Hover.
* Deckkraftwechsel als sanfte Animation (Fade).
* Beschriftungen und Tooltips aus dem Nachrichtenbündel.

### 3. Leere Methoden

* Je Schaltfläche eine parameterlose Methode im `*View`-Controller.
* Rumpf jeweils nur `TODO("AI action: rewrite")`, `TODO("AI action: expand")`, `TODO("AI action: shorten")`.
* Keine weitere Logik, kein Aufruf, keine Rückgabe.

### 4. Lebenszyklus

* `onShown`/`onHidden` des Overlays nur für das Anlegen/Freigeben der Leiste.
* Kein globaler Listener außerhalb des Overlays; sonst greift `showingBinding()`.

### 5. Tests

* Headless: Overlay erscheint bei `PARAGRAPH_HOVER`, verschwindet ohne Trigger.
* Fade-Zustand bei Hover über der Leiste.
* Jede `*View`-Methode wirft `NotImplementedError`.

### 6. Abschluss

* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Die schwebende KI-Leiste steht über simPlay-`FloatingOverlay` am fokussierten Block.
* Jede Schaltfläche endet an einer leeren `*View`-Methode mit `TODO("AI action: …")`.
* KEINE KI-Infrastruktur, kein Port, kein Provider ist Teil dieses Plans.
