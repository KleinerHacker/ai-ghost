# Status: IP-26 - Umzug von Schrift und Messung

Status: NOT_STARTED

## Abhängigkeiten

* Voraussetzung: IP-01, IP-25
* Blockiert: IP-07, IP-08, IP-13

## Aufgaben

| Nr | Aufgabe | Status |
|----|---------|--------|
| 1 | Schriftbeschreibung der Bibliothek | NOT_STARTED |
| 2 | Klassen verschieben | NOT_STARTED |
| 3 | Signaturen umstellen | NOT_STARTED |
| 4 | Anwendungsseite | NOT_STARTED |
| 5 | API-Zusage | NOT_STARTED |
| 6 | Tests | NOT_STARTED |
| 7 | Abschluss | NOT_STARTED |

## Fortschritt

0%

## Notizen

* Plan erstellt, keine Aufgabe begonnen.
* Die Trennlinie verläuft entlang `FontData`; `FontResolver` ist die Klasse mit der Naht.
* Der Fingerabdruck der Messung bleibt in `app/ui` und gehört zu IP-22.
