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

The editing area currently shows a placeholder. The editors for the individual parts of the book -
prolog, chapters, epilog and blurb - are added in a later version.
