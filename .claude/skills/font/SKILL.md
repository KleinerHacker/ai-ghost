---
name: font
description: Draw, extend and ship the Ghost Writer type face - the vector generator under tools/font, the glyph skeleton language, the design metrics and the access through AiGhostFonts. Load before a glyph is drawn or changed, before a character is added to the font, and before the font file is referenced from Kotlin, FXML or CSS.
---

# Ghost Writer Font

* The application ships its own type face `Ghost Writer`, a geometric monolinear sans with rounded
  terminals that follows the `AI` lettering of `docs/docs/assets/images/logo.png`
* The font MUST stay purely vector based
  * Outlines live in the TrueType `glyf` table as straight lines and quadratic Bezier curves
  * FORBIDDEN: bitmap tables of any kind, for instance `EBDT`, `EBLC`, `CBDT` or `sbix`

## Layout of the Sources

* The generator lives under `tools/font`, the generated font under
  `app/ui/src/main/resources/fonts/GhostWriter-Regular.ttf`
* Both the generator and the generated font are committed
  * `tools/font/metrics.py` - design metrics, the single source of every measurement
  * `tools/font/strokes.py` - vector primitives, nothing here knows about letters
  * `tools/font/glyphs.py` - the skeleton of every glyph and the code point of every character
  * `tools/font/build_font.py` - assembles the TrueType tables and writes the font
  * `tools/font/preview.py` - renders a specimen for review, never shipped
* Python dependencies are pinned in `tools/font/requirements.txt`

## Design Metrics

* Every measurement MUST be taken from `tools/font/metrics.py`, never written into a glyph directly
* Em square 1000 units, capital height 700, x-height 500, ascender 700, descender -200
* Digits are 640 units high and share the tabular advance width, so figures align in columns
* Stroke weight is 88 units for EVERY stroke - the font is monolinear and carries a single weight
* Side bearing is 60 units; the build step moves each glyph to it, so a skeleton MAY be written at
  any horizontal position

## Drawing a Glyph

* A glyph is a skeleton: a list of strokes in `tools/font/glyphs.py`, written as tuples
  * `("l", x0, y0, x1, y1)` - straight stroke with rounded ends
  * `("a", cx, cy, radius, start, end)` - circular stroke, anticlockwise when `end` exceeds `start`
  * `("r", cx, cy, radius)` - closed circular stroke with a counter
  * `("d", cx, cy, radius)` - filled circle, used for dots and diaereses
* Curves MUST be true circles; the geometric look comes from circles and straight lines only
* Strokes MAY overlap - all outlines of a glyph run in the same direction and merge under the
  non-zero winding rule, only the counter of a closed ring runs the opposite way
* FORBIDDEN: adding a primitive to `glyphs.py`; new primitives belong in `strokes.py`

## Reviewing a Glyph

* After EVERY change to a glyph the specimen MUST be rendered and looked at
  * `python tools/font/build_font.py` followed by `python tools/font/preview.py`
  * The specimen is written to `build/font-preview.png`
* A glyph is only accepted once its direction, its stroke weight and its side bearings match the
  rest of the alphabet in the rendered image

## Adding a Character

* Add the skeleton to `SKELETONS` and the code point to `CHARACTERS`, both in `tools/font/glyphs.py`
* Use the PostScript glyph name, for instance `period`, `germandbls`, `Adieresis`
* Extend `AiGhostFontsIT` so the new character is covered by the outline check

## Building and Using the Font

* The Gradle task `:app:ai-ghost-ui:generateFont` regenerates the font and runs before
  `processResources`, so `build` always carries a font matching the generator
  * On a machine without Python and fontTools the task logs a notice and keeps the committed font,
    so the build never depends on a Python installation
* The font MUST be reached exclusively through the object class `AiGhostFonts` in the root package
  `org.pcsoft.app.aighost.app` of `app/ui`
  * The file name MUST NOT appear anywhere else; every consumer goes through `AiGhostFonts`
  * `AiGhostTheme.install()` registers the fonts, so no caller has to remember it
* The user interface uses the family as its default, set once in `-fx-font-family` of `.root` in
  `app/ui/src/main/resources/styles/ai-ghost.css`
  * The platform faces stay behind it in the list as a fallback
  * FORBIDDEN: naming the family in Kotlin code, in FXML or in an inline style
