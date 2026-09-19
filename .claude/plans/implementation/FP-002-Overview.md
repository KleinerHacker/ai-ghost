# Implementierungspläne: AI Provider Plugins

* Feature Plan: `.claude/plans/features/FP-002-AiProviderPlugins.md`
* Reihenfolge und Abhängigkeitsgraph: Abschnitt 8 des Feature Plans.
* Ein Plan startet erst, wenn jede Voraussetzung im Feature-Status `COMPLETED` ist.
* Ein erledigter Plan wird samt seiner Zeile hier entfernt; den Stand führt
  `FP-002-AiProviderPlugins-status.md`.
* Kern: IP-01 bis IP-05. IP-06 bis IP-09 sind die konkreten Anbindungen, je atomar, einzeln und
  später umsetzbar.
* Neue Bausteine: Modul `lib/plugin/manager` (`ai-ghost-plugin-manager`), Container
  `lib/plugin/provider` mit je einem Modul pro mitgeliefertem Provider.
* `lib/ai` und die `TODO("AI action: …")`-Rümpfe bleiben unberührt.

## Pläne

| ID | Plan | Datei | Voraussetzung |
|----|------|-------|---------------|
| IP-02 | Provider-Module und Stub-Provider | `FP-002-IP-02-ProviderModuleUndStub.md` | IP-01 (COMPLETED) |
| IP-03 | Provider-Auswahl, Konfiguration und Persistenz | `FP-002-IP-03-ProviderAuswahlKonfigurationPersistenz.md` | IP-02 |
| IP-04 | Plugin-Paketierung und CI | `FP-002-IP-04-PluginPaketierungUndCi.md` | IP-03 |
| IP-05 | Dokumentation | `FP-002-IP-05-Dokumentation.md` | IP-04 |
| IP-06 | LM-Studio-Provider | `FP-002-IP-06-LmStudioProvider.md` | IP-03 |
| IP-07 | OpenAI-API-Provider | `FP-002-IP-07-OpenAiProvider.md` | IP-03 |
| IP-08 | Anthropic-API-Provider | `FP-002-IP-08-AnthropicProvider.md` | IP-03 |
| IP-09 | llama.cpp-Provider | `FP-002-IP-09-LlamaCppProvider.md` | IP-03 |

## Ohne Voraussetzung startbar

* IP-02 - Provider-Module und Stub-Provider (Voraussetzung IP-01 ist `COMPLETED`)
