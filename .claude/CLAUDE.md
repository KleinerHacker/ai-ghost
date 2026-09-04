# Global Rules

* NEVER EVER save memories!!!
* Create subagents IS LIMITED to a DEPTH of one agent 

## Skills

* The following skills carry binding rules and MUST be loaded in the named situation:
  * `ui-styling` - before ANY change under `app/ui` (Kotlin, FXML, message bundles, CSS)
  * `fx-component-lifecycle` - before a view registers or deregisters anything global (listener,
    subscription, timer) or `showingBinding` is touched
  * `icons` - before an icon is drawn, moved, renamed, registered or referenced
  * `font` - before a glyph of the shipped type face is drawn or changed, before a character is
    added to it, and before the font file is referenced
  * `fx-model` - before a POJO in `lib/model` is added, removed or changed in its fields, and
    before an FX property model in `lib/fx-model` is created or changed
  * `startup` - before a startup background process is added or changed
  * `testing` - before a test class is created or changed
  * `project-docs` - after EVERY change, to check README, MkDocs, KDoc and CHANGELOG.md
  * `ci-pipeline` - before a workflow file under `.github` is created or changed

## Concurrency

* Concurrent or long-running processes (e.g. `build`, `test`, `verifyPlugin`, `koverXmlReport`)
  MUST ALWAYS be executed through an agent (Task tool)
  * NOT through a background command of the shell
  * The agent returns the result; only the result is reported

## Limiting search

* NEVER decompile or reflect depending on third party class
  * If this is required, ask the user first

## Console / CLI Output

* On Console or in CLI: MUST ALWAYS in GERMANY
* Plans printed on Console MUST ALWAYS in GERMANY

## File Output

* Into files: MUST ALWAYS in ENGLISH
