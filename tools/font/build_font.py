"""Build the Ghost Writer TrueType font from the glyph skeletons.

The generated font contains vector outlines only. Every glyph is stored in the
``glyf`` table as straight lines and quadratic Bezier curves, so it stays sharp
at every size. Bitmap tables are never written.

Usage::

    python build_font.py [output.ttf]
"""

import sys
from pathlib import Path

from fontTools.fontBuilder import FontBuilder
from fontTools.pens.ttGlyphPen import TTGlyphPen
from fontTools.ttLib import newTable

import strokes
from glyphs import CHARACTERS, SKELETONS, TABULAR_GLYPHS
from metrics import (
    LINE_ASCENT,
    LINE_DESCENT,
    LINE_GAP,
    SIDE_BEARING,
    SPACE_WIDTH,
    STROKE,
    TABULAR_WIDTH,
    UPM,
)

#: Family name of the font.
FAMILY_NAME = "Ghost Writer"

#: Style name of the only shipped weight.
STYLE_NAME = "Regular"

#: Version of the font, raised whenever a glyph changes.
VERSION = "1.000"

#: Default output location relative to the repository root.
DEFAULT_OUTPUT = Path("app/ui/src/main/resources/fonts/GhostWriter-Regular.ttf")

#: Kerning pairs in font units, applied to the shipped ``kern`` feature.
KERNING = {
    ("A", "V"): -40,
    ("A", "W"): -40,
    ("A", "T"): -50,
    ("A", "Y"): -50,
    ("L", "T"): -60,
    ("L", "V"): -60,
    ("L", "Y"): -60,
    ("P", "A"): -50,
    ("T", "A"): -50,
    ("T", "a"): -60,
    ("T", "o"): -60,
    ("T", "e"): -60,
    ("V", "a"): -40,
    ("W", "a"): -40,
    ("Y", "o"): -60,
    ("F", "a"): -30,
    ("r", "period"): -40,
    ("v", "period"): -40,
    ("y", "period"): -40,
}


def _contours(skeleton):
    """Return the outlines of a glyph skeleton.

    :param skeleton: list of strokes as described in :mod:`glyphs`
    """
    contours = []
    for stroke in skeleton:
        kind = stroke[0]
        if kind == "l":
            contours.extend(strokes.capsule(*stroke[1:5], STROKE))
        elif kind == "a":
            contours.extend(strokes.arc_stroke(*stroke[1:6], STROKE))
        elif kind == "r":
            contours.extend(strokes.ring(*stroke[1:4], STROKE))
        elif kind == "d":
            contours.extend(strokes.dot(*stroke[1:4]))
        else:
            raise ValueError(f"Unknown stroke primitive: {kind}")
    return contours


def _place(contours, tabular):
    """Return the contours moved to their side bearing and the advance width.

    :param contours: outlines of a glyph
    :param tabular: whether the glyph shares the fixed width of the digits
    """
    box = strokes.bounds(contours)
    if box is None:
        return contours, SPACE_WIDTH
    min_x, _, max_x, _ = box
    ink = max_x - min_x
    if tabular:
        return strokes.translate(contours, (TABULAR_WIDTH - ink) / 2.0 - min_x), TABULAR_WIDTH
    return strokes.translate(contours, SIDE_BEARING - min_x), int(round(ink + 2 * SIDE_BEARING))


def _draw(contours):
    """Return a TrueType glyph built from the given outlines.

    :param contours: outlines of a glyph
    """
    pen = TTGlyphPen(None)
    for contour in contours:
        for command in contour:
            points = [(int(round(x)), int(round(y))) for x, y in command[1:]]
            if command[0] == "move":
                pen.moveTo(points[0])
            elif command[0] == "line":
                pen.lineTo(points[0])
            else:
                pen.qCurveTo(points[0], points[1])
        pen.closePath()
    return pen.glyph()


def _notdef():
    """Return the outline of the glyph used for unsupported characters."""
    pen = TTGlyphPen(None)
    for box, clockwise in (((60, 0, 500, 700), True), ((148, 88, 412, 612), False)):
        left, bottom, right, top = box
        corners = [(left, bottom), (left, top), (right, top), (right, bottom)]
        if not clockwise:
            corners.reverse()
        pen.moveTo(corners[0])
        for corner in corners[1:]:
            pen.lineTo(corner)
        pen.closePath()
    return pen.glyph()


def _gasp():
    """Return the table asking every rasteriser for grid fitting and grey scale.

    The font carries no hinting instructions. Without this table a rasteriser is
    free to fall back to plain black and white rendering at small sizes, which
    makes the thin monolinear stems look ragged.
    """
    table = newTable("gasp")
    table.version = 1
    # 0x000F: grid fit and grey scale anti aliasing, for every size up to the maximum.
    table.gaspRange = {0xFFFF: 0x000F}
    return table


def _feature_text(available):
    """Return the feature file source holding the kerning pairs.

    :param available: glyph names present in the font
    """
    lines = ["feature kern {"]
    for (left, right), value in sorted(KERNING.items()):
        if left in available and right in available:
            lines.append(f"    pos {left} {right} {value};")
    lines.append("} kern;")
    return "\n".join(lines)


def build(output):
    """Build the font and write it to the given location.

    :param output: path of the TrueType file to write
    """
    glyph_order = [".notdef", "space"] + sorted(SKELETONS)
    outlines = {".notdef": _notdef(), "space": TTGlyphPen(None).glyph()}
    widths = {".notdef": 560, "space": SPACE_WIDTH}

    for name, skeleton in SKELETONS.items():
        placed, advance = _place(_contours(skeleton), name in TABULAR_GLYPHS)
        outlines[name] = _draw(placed)
        widths[name] = advance

    builder = FontBuilder(UPM, isTTF=True)
    builder.setupGlyphOrder(glyph_order)
    builder.setupCharacterMap(dict(CHARACTERS))
    builder.setupGlyf(outlines)
    builder.setupHorizontalMetrics({name: (widths[name], 0) for name in glyph_order})
    builder.setupHorizontalHeader(ascent=LINE_ASCENT, descent=LINE_DESCENT, lineGap=LINE_GAP)
    builder.setupNameTable(
        {
            "familyName": FAMILY_NAME,
            "styleName": STYLE_NAME,
            "uniqueFontIdentifier": f"{FAMILY_NAME} {STYLE_NAME} {VERSION}",
            "fullName": f"{FAMILY_NAME} {STYLE_NAME}",
            "version": f"Version {VERSION}",
            "psName": "GhostWriter-Regular",
            "manufacturer": "PCSoft",
            "designer": "PCSoft",
            "description": "Geometric monolinear sans with rounded terminals, drawn for AI Ghost.",
            "licenseDescription": "Licensed under the Apache License, Version 2.0.",
            "licenseInfoURL": "https://www.apache.org/licenses/LICENSE-2.0",
        }
    )
    builder.setupOS2(
        sTypoAscender=LINE_ASCENT,
        sTypoDescender=LINE_DESCENT,
        sTypoLineGap=LINE_GAP,
        usWinAscent=LINE_ASCENT,
        usWinDescent=-LINE_DESCENT,
        sxHeight=500,
        sCapHeight=700,
        achVendID="PCSF",
    )
    builder.setupPost()
    builder.addOpenTypeFeatures(_feature_text(set(glyph_order)))
    builder.font["gasp"] = _gasp()
    # Below this size the outlines are no longer legible, so no rasteriser should try.
    builder.font["head"].lowestRecPPEM = 8

    output.parent.mkdir(parents=True, exist_ok=True)
    builder.save(str(output))
    return output


def main():
    """Build the font, writing either to the default or to the given path."""
    target = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_OUTPUT
    written = build(target)
    print(f"Wrote {written} with {len(SKELETONS) + 2} glyphs")


if __name__ == "__main__":
    main()
