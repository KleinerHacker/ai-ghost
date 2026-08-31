---
name: Architecture
---

# Architecture

## Module Structure

* all Applications (end user apps) are stored in 'app'
  * Java FX UI application is stored in 'app/ui'
    * no other module SHOULD CONTAIN Java FX parts or frameworks
* A module under 'lib' MAY carry Java FX only in these two cases; every other one stays free of it
  * a property model library on 'javafx.base' alone - currently 'lib/fx-model'
    (`ai-ghost-fx-model`); no toolkit module beyond `javafx.base`
  * a Java FX component library on the full toolkit - currently 'lib/layouting-fx'
    (`ai-ghost-layouting-fx`), the renderer of the layout core
    * it MUST NOT contain application logic, view models, FXML or message bundles
    * it MUST NOT depend on a module of this application other than the layout core
