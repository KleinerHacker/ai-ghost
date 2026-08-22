# Project tree

The `Editor` tab shows the open project as a tree. It is the place to navigate the manuscript: every
part of the book has its own node, and picking one selects the part to work on.

## Structure

```
Project
├── Prolog
├── Chapter
│   ├── <chapter name>
│   └── <chapter name>
├── Epilog
└── Blurb
```

| Node      | What it stands for                                              |
|-----------|-----------------------------------------------------------------|
| `Project` | The open project, the root of the tree                          |
| `Prolog`  | The text printed before the first chapter                       |
| `Chapter` | The branch collecting every chapter of the book                 |
| `Epilog`  | The text printed after the last chapter                         |
| `Blurb`   | The advertising text printed on the cover                       |

The four branches are always shown, also while the part behind them has not been written yet, so
there is always a place to start from.

## Chapters

Below `Chapter` sits one node per chapter, in the order the chapters are arranged in the book. A
chapter is listed by its **name**, not by the heading it is printed with: the heading may still be
empty while the chapter is only outlined, whereas the name is what tells the chapters apart while
writing.

New chapters are added through `File > New > Chapter...` (`Ctrl+Alt+C`) and appear in the tree right
away.

## Language

The node labels follow the language of the user interface: in German the tree reads `Projekt`,
`Prolog`, `Kapitel`, `Epilog` and `Klappentext`.
