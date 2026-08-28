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
  names the affected parts and asks whether the project may be opened anyway. Opening it does not
  bring those parts back - as soon as you save the project, they are removed from the file for good.
  Cancel and back up the file first if you want to keep them.

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
