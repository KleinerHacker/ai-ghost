---
name: development
---

# Development

## Planning

* A plan MUST be created for EVERY change, ALWAYS ask the user to create a plan or not
    * A switch to plan mode MUST happen
* The PLAN MUST ALWAYS be written in GERMAN - both the plan file and the console output
    * This applies to headings, bullet points and every other text of the plan
* The PLAN MUST NOT contain a summary or explanation of the changes
    * FORBIDDEN sections: "Context", "Background", "Summary", "Overview", "Rationale", "Trade-offs"
    * FORBIDDEN: prose paragraphs of any kind - the plan consists of bullet points ONLY
* The implementation tasks MUST be explained in short bullet points with no more than 20 words per bullet and a maximum of 10 bullets per task
    * A bullet describes WHAT is done, NOT WHY
* Before leaving plan mode the plan MUST be checked against ALL rules above
* The plan MUST be written into the local `.claude/plans/implementation` directory, together with a status file
    * Naming scheme:
        * Plan: `<Name>.md`
        * Status: `<Name>-status.md`
    * The status MUST ALWAYS be kept up to date
* When restarting an existing plan after an interruption, plan mode MUST be entered
    * The remaining items are laid out again according to the prescribed scheme
* As soon as a plan is finished, its two files MUST be removed from
  `.claude/plans/implementation` immediately, with `git rm`
    * Removed are EXACTLY `<Name>.md` and `<Name>-status.md` of the finished plan
    * FORBIDDEN: emptying the directory - every other plan and status file stays untouched
    * FORBIDDEN: removing a plan that is not finished yet
    * The removal happens in the same change set as the last task of the plan
    * The feature status file records the plan as `COMPLETED` before its files are removed

## Implementation

* Kotlin MUST ALWAYS be used
* Gradle MUST ALWAYS be used

* All changes to a single file MUST be applied in ONE single tool call
    * Before editing, ALL required changes to that file MUST be collected and planned completely
    * Then the file is written EXACTLY ONCE - with the `Write` tool (full content) or with a
      SINGLE `Edit` call
    * FORBIDDEN: several `Edit` calls on the same file, one after another, for the same change
    * FORBIDDEN: incremental "edit -> read -> edit again" cycles on the same file
    * If a change to file A reveals a follow-up change in file A, the file MUST NOT be patched
      again - the complete new content MUST be written in one operation instead
    * This rule applies per file, NOT per task: several DIFFERENT files MAY be edited in
      parallel, each with exactly one call

## Building

* A build MUST always be performed with the Gradle target `build` after every change

## Testing

* Details are in the `testing` skill, which MUST be loaded before a test class is created or changed
