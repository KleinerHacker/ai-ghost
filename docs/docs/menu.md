# Menu and keyboard shortcuts

The main window carries a single menu bar. Every entry listed here is reachable with the mouse and,
where a shortcut is given, straight from the keyboard.

`Ctrl` is the shortcut key on Windows and Linux; on macOS the same entries use `Cmd`.

## File

| Entry                   | Shortcut           | What it does                                  |
|-------------------------|--------------------|-----------------------------------------------|
| New > Project...        | -                  | Creates a new book project                    |
| New > Chapter...        | `Ctrl+Alt+C`       | Adds a chapter to the current project         |
| Open...                 | `Ctrl+O`           | Opens an existing book project                |
| Open recent project     | -                  | Lists the projects opened before, each with its file name and the folder it sits in |
| Save                    | `Ctrl+S`           | Saves the current project                     |
| Save As...              | `Ctrl+Shift+S`     | Saves the current project under a new name    |
| Preferences...          | -                  | Opens the application preferences             |
| Project Settings...     | -                  | Opens the settings of the current project     |
| Exit                    | `Alt+F4`           | Closes the application                        |

### Opening a project that is not complete

A book project is one document made of several parts. What happens when a part is missing depends on
which part it is:

* **A basic part is missing or damaged** - the project data, the design or the manuscript itself:
  the project cannot be opened at all and is reported as corrupt. The project you are working on
  stays open.
* **Any other part is missing or damaged** - a part of a plugin for instance: AI Ghost warns you,
  asks whether the project may be opened anyway and names the affected parts behind *Show details*.
  Opening it does not bring those parts back - as soon as you save the project, they are removed from
  the file for good. Answer with *No* and back up the file first if you want to keep them.

### Opening a project on another computer

A book project stores the fonts it is set in by name, and it stores how those fonts measured on the
computer it was written on. When you open it somewhere else, AI Ghost compares the two:

* **A font is not installed here** - AI Ghost sets the manuscript in another one and warns you,
  naming the affected elements, the font the project asks for and the font used instead behind
  *Show details*.
* **A font is installed but sets differently** - another version of the same font, for instance.
  You are warned in the same way.

Lines and pages break at other places in both cases, so the manuscript does not look the way its
author saw it. Nothing in the project itself is changed; install the fonts named to see it as it was
written. A project written before AI Ghost recorded this carries no measurements and is opened
without a warning.

## Publish

| Entry             | Shortcut       | What it does                                |
|-------------------|----------------|---------------------------------------------|
| Export > to PDF...| -              | Exports the manuscript as a PDF document    |

## Help

| Entry          | Shortcut | What it does                          |
|----------------|----------|---------------------------------------|
| Online Help... | `F1`     | Opens this documentation in a browser |
| About          | -        | Shows version and licence information |

## Language

The user interface follows the language of the operating system. English and German are shipped; any
other system language falls back to English.
