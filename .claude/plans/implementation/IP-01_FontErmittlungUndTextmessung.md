# IP-01: Font-Ermittlung und Textmessung

## Herkunft

* Feature Plan: `.claude/plans/features/001_PaperWritingSurface.md`
* Plan-ID im Feature Plan: IP-01
* Status-Datei des Features: `.claude/plans/features/001_PaperWritingSurface_status.md`

## Abhängigkeiten

* Voraussetzung: keine
* Start jederzeit möglich.
* Blockiert: IP-03, IP-13, IP-22
* Reihenfolge und Graph stehen in Abschnitt 8 des Feature Plans.

## Zu ladende Skills

* `testing`
* `font`
* `ui-styling`
* `project-docs`

## Aufgaben

### 1. Font-Katalog

* Installierte Familien über `javafx.scene.text.Font.getFamilies()` auslesen.
* Katalog einmalig aufbauen und im Speicher halten.
* Methode zum erneuten Aufbau anbieten.
* Familien ohne nutzbare Textdarstellung aussortieren.

### 2. Auflösung einer Schrift

* `FontData` auf `javafx.scene.text.Font` abbilden, inklusive Fett und Kursiv.
* Feste Fallback-Kette für nicht installierte Familien festlegen.
* Ergebnis `Familie nicht installiert` als eigenen Zustand zurückgeben.
* `Font.loadFont` nicht verwenden, keine Datei öffnen.

### 3. Messung

* `TextMetrics`-Implementierung mit einem einmalig erzeugten, versteckten `Text`-Knoten.
* Knoten wiederverwenden, nicht pro Messung neu anlegen.
* `wrappingWidth` auf 0 setzen und `prefWidth(-1)` lesen.
* Bruchwert der Breite unverändert zurückgeben, nicht aufrunden.
* Wortbreite, Leerzeichenbreite, Ascent, Descent und Leading liefern.
* Bindung an den FX-Thread in der KDoc festhalten.

### 4. Messcache

* Messergebnisse nach Familie, Grad, Schnitt und Wort ablegen.
* Cache beim erneuten Aufbau des Katalogs leeren.

### 5. Tests

* Auflösung, Fallback und Zustand `nicht installiert` prüfen.
* Messung headless über TestFX prüfen.
* Wiederverwendung des Knotens und Cachetreffer prüfen.

### 6. Abschluss

* `module-info.java` von `app/ui` prüfen.
* Build über Agent ausführen.
* Dokumentation nach `project-docs` prüfen.

## Ergebnis

* Neues Paket in `app/ui` liefert Katalog, Auflösung und `TextMetrics`-Implementierung.
* Keine Schriftdatei wird gelesen, geöffnet oder ausgeliefert.
