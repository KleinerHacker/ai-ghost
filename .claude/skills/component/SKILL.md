---
Name: component
Description: Basic architecture for a Java FX component (NOT a window); required to create or edit Java FX components.
---

# Java FX Components

## Template

* The template is located here: `app/ui/src/main/kotlin/org/pcsoft/app/aighost/app/ui/component`
  * ONLY the files `ProjectList*.kt` are relevant
  * Their design and architecture MUST be adopted (Component, View, ViewModel)

## Structure

* The FXML always contains the component content
  * The content is fully defined there
  * The `fx:root` notation MUST be used
* The component is assembled through any FX Node class
  * Integration always happens via `root`

## Location

* Component classes MUST be placed under the root package in `ui.component`
  * Every required class MUST be placed in its own file
* Component FXML MUST be placed in the same directory within the resource directory