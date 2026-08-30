# Status: IP-02 - Design mit Seitenformat

Status: COMPLETED

## Abhängigkeiten

* Voraussetzung: keine
* Blockiert: IP-03, IP-13, IP-14

## Aufgaben

| Nr | Aufgabe | Status |
|----|---------|--------|
| 1 | Modell | DONE |
| 3 | FX-Modell | DONE |
| 4 | Tests | DONE |
| 5 | Abschluss | DONE |

## Fortschritt

100%

## Notizen

* `PageFormat` in `lib/model/project/design` mit Breite, Höhe und den vier Rändern als `Double`.
* Standard A5 (419.53 x 595.28 pt), Ränder 20 innen, 15 außen, 15 oben, 20 unten.
* `Design` mit `pageFormat` und je einem Zeilenabstandsfaktor pro Elementklasse (Standard 1.2).
* Version des Teils bleibt bei 1, keine Kompatibilitätsbehandlung nötig.
* `BeanFields.double` ergänzt, `PageFormatProperty` neu, `DesignProperty` erweitert.
* Build und alle Tests grün.
* Dokumentation geprüft: kein Eintrag in CHANGELOG, README oder MkDocs nötig, da keine
  Endnutzer-sichtbare Änderung.
