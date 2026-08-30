# Status: IP-01 - Font-Ermittlung und Textmessung

Status: COMPLETED

## Abhängigkeiten

* Voraussetzung: keine
* Blockiert: IP-03, IP-13, IP-22

## Aufgaben

| Nr | Aufgabe | Status |
|----|---------|--------|
| 1 | Font-Katalog | COMPLETED |
| 2 | Auflösung einer Schrift | COMPLETED |
| 3 | Messung | COMPLETED |
| 4 | Messcache | COMPLETED |
| 5 | Tests | COMPLETED |
| 6 | Abschluss | COMPLETED |

## Fortschritt

100%

## Notizen

* Neues Paket `org.pcsoft.app.aighost.app.font` in `app/ui`.
* `FontCatalog`, `FontResolution`, `FontResolver`, `LineMetrics`, `JavaFxTextMetrics` angelegt.
* Tests: `FontCatalogTest`, `FontResolverTest`, `JavaFxTextMetricsTest` (headless TestFX).
* `module-info.java` um Export des Font-Pakets ergänzt.
* CHANGELOG, README und MkDocs unverändert: keine für Endnutzer sichtbare Änderung.
* Gradle `build` erfolgreich.
* Feature-Status auf COMPLETED gesetzt.
