# Editor

The `Editor` tab is the place where the manuscript is written. It is split into two areas:

```
┌──────────────────┬───────────────────────────────────────┐
│                  │                                       │
│   Project tree   │            Editing area               │
│                  │                                       │
└──────────────────┴───────────────────────────────────────┘
                   ↑
                 splitter
```

* **Left:** the [project tree](project-tree.md), which lists every part of the book.
* **Right:** the editing area, which shows the part picked in the tree.

## Splitter

The line between the two areas is a splitter. Drag it to give the tree more or less room; the pointer
changes as soon as it is over the line.

The tree never becomes narrower than 250 pixels, so the chapter names stay readable no matter how far
the splitter is pushed to the left. Making the window wider gives the extra room to the editing area,
so the tree keeps the width it was given.

## Editing area

The editing area is the **writing surface**: the part picked in the project tree is shown on a sheet
that already carries the typography, the margins and the page structure of the finished book.

* **Prolog, chapters and epilog** are written directly on the sheet - the heading, its further lines
  and every paragraph. Each paragraph is its own text block; a change is taken over into the project
  with every keystroke.
* **The blurb** is written the same way, without a heading.
* **The title page and the copyright page** are shown as they will be printed, but are not edited
  here: their text comes from the *Book* section of the inspector and from the project settings.

A design change made in the inspector - a different font, size or line spacing - is applied to the
sheet at once, without the caret leaving the place it was in.

Every text change is undone and redone through the Undo and Redo buttons of the tool bar. Consecutive
typing in the same block falls together into a single step; moving to another block or leaving the
sheet closes that step.

## Paragraphs

Each paragraph is its own block on the sheet, and can be reshaped without leaving the keyboard:

* **Enter** splits the paragraph at the caret into two.
* **Backspace** at the very start of a paragraph merges it into the one before it; **Delete** at the
  very end merges it with the one after it.
* **Ctrl+Shift+Up** and **Ctrl+Shift+Down** move the whole paragraph one place up or down; the same two
  actions are on the paragraph's right-click menu, next to *Remove block*.
* **Up** and **Down** move the caret into the paragraph above or below once it sits on the first or the
  last line of the current one - the same way they would in a single, book-length page.

Every one of these is undone and redone as its own step, exactly like a text change.

