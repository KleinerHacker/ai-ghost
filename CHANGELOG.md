# ai-ghost Changelog

## [Unreleased]

## [0.0.1]

* Editor centre is a writing surface showing the whole book as one continuous, typeset sheet;
  picking a part in the project tree - title page, copyright page, prolog, a chapter, the epilog or
  the blurb - jumps to its place on that sheet instead of opening a document of its own, and every
  part is written on directly. A tool bar above it offers a read-only "Preview" toggle, and a small
  label above every page names the book part that begins there

* Project tree manages the book's structure: "Add/Rename/Delete Chapter", a "Title Page" and
  "Copyright Page" node ahead of the prolog, and a checkbox on the prolog, the epilog and the blurb
  to leave that part out of the finished book without losing its text

* Writing surface supports paragraph-level editing - split, merge and reorder a paragraph with
  keyboard shortcuts or its right-click menu - and full Undo/Redo from the Edit menu and tool bar,
  with a dropdown history and consecutive typing collapsed into a single step

* Editor gains an Inspector next to the project tree with "Book", "Chapter" and "Design" sections
  (author and prompts, chapter name and prompts, and typography of title/heading/body text); AI
  action buttons - "Generate chapter" and a floating Rewrite/Expand/Shorten bar on the writing
  surface - are present but not yet wired to generation

* Project Settings dialog (File menu and tool bar) configures page size, margins and blank leading/
  trailing pages under a *Design* section; further *General* and per-part *Design* sections are
  placeholders for now

* Book project file format: one document holding project data, manuscript design and manuscript
  side by side, tolerant of parts written by a newer version and parts it doesn't yet know, with
  corrupt or incomplete files reported instead of silently opened or discarded

* Project records the fonts it was written with and warns if a font is missing or renders
  differently when opened elsewhere, naming the affected elements and the substitute font used

* Preferences moved from JSON (`preferences.json`) to grouped YAML (`preferences.yml`, with
  `appearance.themeMode` and `ai` sections); old preference files are not migrated

* Application logs its session to the console and to rolling, compressed log files under
  `.ai-ghost/logs`; unreadable preferences or project files are reported in a dialog naming the
  reason instead of failing silently, and the "open recent" list survives an empty session

* Startup shows a floating, frameless splash animation of the logo while settings are read, fading
  out as the main window appears

* User interface redesign: the "Ghost Writer" type face shipped with the app, an indigo-and-navy
  light/dark theme following the `themeMode` preference, restyled buttons, list entries and
  dialogs (including height-adapting messages and Yes/No question dialogs), and a bolder AI wand
  icon

* Main window: menu bar (`File`, `Publish`, `Help`) with icons and shortcuts, `Editor`/`Preview`
  tabs, a project tree beside the editing area, translated UI (English/German), and the AI Ghost
  application icon in every size

* Application is shipped as a ZIP archive containing `ghost-ui.sh`, `ghost-ui.bat` and a `libs`
  folder with all required JARs
