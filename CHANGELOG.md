# ai-ghost Changelog

## [Unreleased]

* Project Settings dialog opens from the File menu and from the tool bar: a tree on the left switches
  between sections, and the *Design* section sets the page size - a preset such as A5 or A4, or a
  custom width and height entered in millimetres - the four page margins in millimetres, and whether
  the book begins and ends with a blank page; OK stores and closes, Apply stores without closing,
  Cancel discards, and impossible values keep both from being stored. The *General* section and the
  child sections of *Design* - title page, copyright page, prolog, chapter, epilog, blurb - are
  placeholders for now

* Project records what its fonts measured like on the computer it was written on, and says so when
  a font is missing or sets differently when the project is opened elsewhere: one warning names the
  affected elements, the font the project asks for and the font that is used instead - a project
  written before this existed carries no such record and is opened silently as before

* Preferences are stored as YAML in `preferences.yml` instead of as JSON in `preferences.json`, so
  the settings read like plain lines of text when you open the file - a `preferences.json` left over
  from an earlier version is not read any more and the settings have to be chosen again

* Preferences file groups its settings in blocks: the appearance is chosen under `appearance` with
  `themeMode` inside it, and the limits of the AI stand under `ai` - a file still naming `themeMode`
  at the top level is read with the default appearance, so the choice has to be made again once

* Warning and error dialogs take the height their text needs: a message running over several lines
  is wrapped and shown completely instead of being cut off at the bottom

* Button starting an AI action shows a bolder magic wand: a thick shaft with rounded ends and two
  clear sparkles instead of the thin line with tiny dots, so it stays readable at small sizes

* Title lines of a book are shown as a list and no longer as loose fields: the lines, the hint that
  there is none yet and the plus adding one stand together in an area of their own, and a line
  separates one entry from the next

* Buttons stand off the surface they sit on: each one carries a brighter fill of its own, a line
  around it and a soft shadow, hovering and pressing shade that fill, and the button answering a
  dialog carries the indigo of the application; only the buttons of the tool bar and of the menu bar
  stay flat, because those strips lift them already

* Button removing an entry of a list - a title line, for instance - shows a trash bin in the indigo
  of the application instead of a red cross

* Messages of the application are shown in dialogs of their own look, with an icon drawn for the
  light and for the dark appearance, and a report you unfold with "Show details" whenever there is
  more to say than one sentence - the parts an incomplete project lost, for instance

* Question dialogs are answered with "Yes" and "No" instead of a button naming the action, and
  closing such a dialog with ESCAPE or the window close button counts as "No"

* Book project is stored as one document holding its parts side by side - the project data, the
  design of the manuscript and the manuscript itself - so a project written by a newer version still
  opens with the parts this version knows, and a damaged file is reported instead of opening an empty
  project

* Project document that lost one of its three basic parts - the project data, the design or the
  manuscript - is reported as a corrupt project instead of being opened with empty defaults in place
  of the lost part; a saved document names the parts stored beside those three, so a part that went
  missing is noticed as well

* Project document that only lost a part beyond the three basic ones is no longer thrown away: a
  warning names the affected parts, says that saving removes them from the file for good, and opens
  the project only after you confirm

* Project document keeps a part this version cannot read - written by a newer version for instance -
  and writes it back unchanged when you save, instead of dropping it

* Entries inside a project document carry the `.json` extension, so the content is recognizable in
  any archive tool

* Menu "open recent project" shows each entry on two lines - the file name, and below it in
  smaller type the folder the project sits in, so two projects of the same name are told apart

* List of recently opened projects is no longer emptied when the window leaves the screen without a
  project having been opened meanwhile

* Saving or opening a project that fails is reported in a dialog naming the reason - a missing file,
  a folder in place of the file, a file that cannot be read or written, or a damaged project file -
  and the application keeps running with the project it holds

* Course of a session is written to the console and to a log file in `.ai-ghost/logs` of the home
  directory; the file carries more detail than the console, is rolled over daily and at 10 MB, and
  the last ten of the older files are kept compressed

* Preferences that cannot be read are reported while starting: a missing file is replaced by the
  defaults silently, a damaged or unreadable file is only reset after you confirm, and a folder in
  place of the file is reported before the application closes; all of these dialogs follow the
  language of the application and its appearance

* User interface uses its own type face `Ghost Writer`, a rounded geometric sans matching the logo;
  it ships with the application, so the text looks the same on every platform
* User interface text is slightly larger and rendered with grey scale smoothing, which suits the
  even stroke weight of the new type face

* Main window menu bar with the menus `File`, `Publish` and `Help`
* Menu entries carry icons, and the frequently used ones carry their usual keyboard shortcut:
  `Ctrl+O` to open a project, `Ctrl+S` to save, `Ctrl+Shift+S` to save under a new name,
  `Ctrl+Alt+C` for a new chapter, and `F1` for the online help
* User interface is translated; it follows the system language and ships English and German
* Application window carries the AI Ghost icon in every size the window manager asks for
* User interface uses the AI Ghost design: a light theme with indigo accents, deep navy text and
  softly rounded controls, matching the logo and the documentation site
* User interface comes in a light and a dark appearance; the dark one carries the same indigo
  accents on deep navy surfaces
* Appearance follows the `themeMode` setting of the preferences - `LIGHT`, `DARK` or `SYSTEM`, which
  follows the operating system; the setting is read while starting, so a change takes effect after a
  restart
* Main window shows the `Editor` and `Preview` tabs in the AI Ghost design: rounded tab headers on
  the window surface, separated from the content by an indigo accent line
* `Editor` tab is split into the project tree on the left and the editing area on the right,
  separated by a splitter the user can drag; the tree never becomes narrower than 250 pixels and
  a wider window gives the extra room to the editing area
* Editing area shows a placeholder until the editors for the parts of the book exist
* `Editor` tab shows the open project as a tree: `Prolog`, `Chapter`, `Epilog` and `Blurb` sit below
  the project, each with its icon, and every chapter is listed by its name below `Chapter`
* Book project carries a prolog, an epilog and a blurb beside its chapters; each of them is optional
  and stays absent until it is created

* Application is shipped as a ZIP archive containing `ghost-ui.sh`, `ghost-ui.bat` and a `libs` folder
  with all required JARs
