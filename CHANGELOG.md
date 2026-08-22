# ai-ghost Changelog

## [Unreleased]

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
