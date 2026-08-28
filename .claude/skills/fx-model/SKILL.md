---
name: fx-model
description: Mirroring rule between the plain model POJOs in lib/model (ai-ghost-model) and their Java FX property models in lib/fx-model (ai-ghost-fx-model) - package mirroring, nested change propagation, mandatory tests and the usage in app/ui. Load before a POJO under lib/model is added, renamed, removed or changed in its fields, before an FX property model is created or changed, and whenever a POJO in lib/model has no counterpart in lib/fx-model yet.
---

# FX Model

## Mirroring Rule

* EVERY POJO in module `ai-ghost-model` (`lib/model`) MUST have a counterpart in module
  `ai-ghost-fx-model` (`lib/fx-model`)
  * The counterpart is built on Java FX properties and wraps the POJO - it MUST NOT copy its data
  * The POJO stays the single source of truth, the FX model is only the property view on it
* Enums, value objects without fields and pure marker types need NO counterpart
* Adding, renaming, removing or re-typing a field of a POJO REQUIRES the same change in its FX model
  in the SAME change set

## Package And Naming

* The package sub structure of `lib/model` MUST be mirrored under
  `org.pcsoft.app.aighost.fx.model`
  * `org.pcsoft.app.aighost.model.pref.RecentOpened`
    -> `org.pcsoft.app.aighost.fx.model.pref.RecentOpenedProperty`
* Naming scheme: `<PojoName>Property`
* Storage counterparts follow the scheme `FX<StorageName>`
  * `PreferencesStorage` -> `FXPreferencesStorage`

## Structure Of An FX Model

* A root FX model extends `SimpleObjectProperty<Pojo>`
* A nested FX model extends `OverrideObjectProperty<Pojo>` and takes `setter`, `getter`, `fireEvent`
* EVERY field of the POJO gets an own property, backed by the matching `Override*Property` from
  `org.pcsoft.app.aighost.fx.model.internal`
  * Object fields whose type is a POJO get the FX model of that POJO, not a plain
    `OverrideObjectProperty`
* For every field expose
  * `val <field>Property` - the property itself
  * `var <field>` - delegating getter and setter onto the property
* `invalidated()` MUST refresh every field property, `refresh()` of a nested model MUST do the same
* A change of a field MUST fire the change event of its parent property, up to the root

## Visibility

* An FX model used by `app/ui` MUST be public AND its package MUST be exported in
  `lib/fx-model/src/main/java/module-info.java`
  * Without the export the module system rejects the type, although Kotlin resolves it
* An FX model nobody outside the module reaches stays `internal`
* A public FX model MUST NOT expose a member of an `internal` type
  * Such a member stays `internal`, and what the user interface needs is offered as a short
    accessor of a public type next to it

## Testing

* Load the `testing` skill before writing the tests
* For EVERY FX model a test MUST prove for ALL fields
  * The value is read from the wrapped POJO
  * A write through the property reaches the POJO
  * A write through a binding reaches the POJO
  * The change fires on ALL object properties of the tree up to the root
  * A change on the POJO itself is visible through the properties
  * Exchanging the wrapped object updates every field property and fires up to the root
  * Exchanging the wrapped object against one with equal values keeps the field properties quiet
* A nested tree MUST be tested through the root as well, not only per level
