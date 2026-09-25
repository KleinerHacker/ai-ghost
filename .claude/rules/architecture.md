---
name: Architecture
---

# Architecture

## Module Structure

* all Applications (end user apps) are stored in 'app'
  * Java FX UI application is stored in 'app/ui'
    * no other module SHOULD CONTAIN Java FX parts or frameworks
* A module under 'lib' MAY carry Java FX only in this one case; every other one stays free of it
  * a property model library on 'javafx.base' alone - currently 'lib/fx-model'
    (`ai-ghost-fx-model`); no toolkit module beyond `javafx.base`
* 'lib/plugin' carries the plugin mechanism, free of Java FX: 'api' (`ai-ghost-plugin-api`, the
  plugin-authoring API) and 'system' (`ai-ghost-plugin-system`, only the `AiProviderExtensionConfig`
  declaration of the `"ai"` extension point external `pluggiat` resolves a provider plugin against);
  building and running pluggiat's own `PluginManager` and projecting its scan result into a registry
  is `app/ui`'s own concern (`org.pcsoft.app.aighost.app.plugin`), not this module's; a later
  'provider' branch holds one module per shipped provider
* The full Java FX toolkit lives ONLY in 'app/ui', and there only transitively through `simplay-fx`
  (the layout renderer, previously the in-house module `lib/layouting-fx`, removed in IP-29)
