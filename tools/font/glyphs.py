"""Skeletons of every Ghost Writer glyph.

A skeleton is a list of strokes. A stroke is a tuple whose first entry names the
primitive:

* ``("l", x0, y0, x1, y1)`` - straight stroke with rounded ends
* ``("a", cx, cy, radius, start, end)`` - circular stroke with rounded ends,
  drawn anticlockwise when ``end`` is larger than ``start``
* ``("r", cx, cy, radius)`` - closed circular stroke with a counter
* ``("d", cx, cy, radius)`` - filled circle

Coordinates are written in font units on the baseline. The horizontal position
is irrelevant, because the build step moves every glyph to its side bearing.
"""

from metrics import (
    DIAERESIS_LOWER_Y,
    DIAERESIS_UPPER_Y,
    DOT_RADIUS,
)


def _diaeresis(left_x, right_x, y):
    """Return the two dots of a diaeresis.

    :param left_x: x coordinate of the left dot
    :param right_x: x coordinate of the right dot
    :param y: vertical centre of both dots
    """
    return [("d", left_x, y, DOT_RADIUS), ("d", right_x, y, DOT_RADIUS)]


def _comma(x, y):
    """Return a comma shaped mark with its tail pointing down and left.

    :param x: x coordinate of the dot
    :param y: vertical centre of the dot
    """
    return [("d", x, y, DOT_RADIUS), ("l", x, y - 24, x - 40, y - 134)]


def _reverse_comma(x, y):
    """Return a comma shaped mark with its tail pointing up and left.

    :param x: x coordinate of the dot
    :param y: vertical centre of the dot
    """
    return [("d", x, y, DOT_RADIUS), ("l", x, y + 24, x - 40, y + 134)]


def _spine(x, upper_y, lower_y, radius):
    """Return the two bowls of an ``S`` shaped spine.

    The upper bowl carries the top and the left side and runs anticlockwise, the
    lower bowl carries the right side and the bottom and runs clockwise. Both
    meet at the point where the two circles touch.

    :param x: horizontal centre of both bowls
    :param upper_y: vertical centre of the upper bowl
    :param lower_y: vertical centre of the lower bowl
    :param radius: radius of both bowls
    """
    return [("a", x, upper_y, radius, 30, 270), ("a", x, lower_y, radius, 90, -160)]


_CAPITALS = {
    "A": [("l", 360, 656, 104, 44), ("l", 360, 656, 616, 44), ("l", 190, 250, 530, 250)],
    "B": [
        ("l", 104, 44, 104, 656),
        ("l", 104, 656, 400, 656),
        ("l", 104, 404, 400, 404),
        ("l", 104, 44, 400, 44),
        ("a", 400, 530, 126, 90, -90),
        ("a", 400, 224, 180, 90, -90),
    ],
    "C": [("a", 410, 350, 306, 55, 305)],
    "D": [
        ("l", 104, 44, 104, 656),
        ("l", 104, 656, 354, 656),
        ("l", 104, 44, 354, 44),
        ("a", 354, 350, 306, 90, -90),
    ],
    "E": [
        ("l", 104, 44, 104, 656),
        ("l", 104, 656, 560, 656),
        ("l", 104, 350, 500, 350),
        ("l", 104, 44, 560, 44),
    ],
    "F": [("l", 104, 44, 104, 656), ("l", 104, 656, 560, 656), ("l", 104, 350, 500, 350)],
    "G": [("a", 410, 350, 306, 55, 360), ("l", 716, 350, 500, 350)],
    "H": [("l", 104, 44, 104, 656), ("l", 616, 44, 616, 656), ("l", 104, 350, 616, 350)],
    "I": [("l", 104, 44, 104, 656)],
    "J": [("l", 392, 656, 392, 150), ("a", 248, 150, 144, 0, -180)],
    "K": [("l", 104, 44, 104, 656), ("l", 140, 350, 600, 656), ("l", 140, 350, 600, 44)],
    "L": [("l", 104, 44, 104, 656), ("l", 104, 44, 540, 44)],
    "M": [
        ("l", 104, 44, 104, 656),
        ("l", 764, 44, 764, 656),
        ("l", 104, 656, 434, 240),
        ("l", 764, 656, 434, 240),
    ],
    "N": [("l", 104, 44, 104, 656), ("l", 616, 44, 616, 656), ("l", 104, 656, 616, 44)],
    "O": [("r", 410, 350, 306)],
    "P": [
        ("l", 104, 44, 104, 656),
        ("l", 104, 656, 400, 656),
        ("l", 104, 380, 400, 380),
        ("a", 400, 518, 138, 90, -90),
    ],
    "Q": [("r", 410, 350, 306), ("l", 500, 180, 660, 20)],
    "R": [
        ("l", 104, 44, 104, 656),
        ("l", 104, 656, 400, 656),
        ("l", 104, 380, 400, 380),
        ("a", 400, 518, 138, 90, -90),
        ("l", 400, 380, 620, 44),
    ],
    "S": _spine(254, 500, 200, 150),
    "T": [("l", 360, 44, 360, 656), ("l", 104, 656, 616, 656)],
    "U": [("l", 104, 656, 104, 244), ("l", 504, 656, 504, 244), ("a", 304, 244, 200, 180, 360)],
    "V": [("l", 104, 656, 360, 44), ("l", 616, 656, 360, 44)],
    "W": [
        ("l", 104, 656, 264, 44),
        ("l", 264, 44, 434, 560),
        ("l", 434, 560, 604, 44),
        ("l", 604, 44, 764, 656),
    ],
    "X": [("l", 104, 656, 616, 44), ("l", 104, 44, 616, 656)],
    "Y": [("l", 104, 656, 360, 370), ("l", 616, 656, 360, 370), ("l", 360, 370, 360, 44)],
    "Z": [("l", 104, 656, 616, 656), ("l", 616, 656, 104, 44), ("l", 104, 44, 616, 44)],
}

_SMALL = {
    "a": [("r", 310, 250, 206), ("l", 516, 456, 516, 44)],
    "b": [("l", 104, 656, 104, 44), ("r", 310, 250, 206)],
    "c": [("a", 310, 250, 206, 55, 305)],
    "d": [("r", 310, 250, 206), ("l", 516, 656, 516, 44)],
    "e": [("a", 310, 250, 206, 0, 325), ("l", 104, 250, 516, 250)],
    "f": [("l", 300, 44, 300, 530), ("a", 430, 530, 130, 180, 85), ("l", 160, 456, 480, 456)],
    "g": [("r", 310, 250, 206), ("l", 516, 456, 516, -6), ("a", 366, -6, 150, 0, -160)],
    "h": [("l", 104, 656, 104, 44), ("a", 310, 250, 206, 180, 0), ("l", 516, 250, 516, 44)],
    "i": [("l", 104, 456, 104, 44), ("d", 104, 600, DOT_RADIUS)],
    "j": [("l", 260, 456, 260, -56), ("a", 160, -56, 100, 0, -180), ("d", 260, 600, DOT_RADIUS)],
    "k": [("l", 104, 656, 104, 44), ("l", 140, 250, 460, 456), ("l", 140, 250, 480, 44)],
    "l": [("l", 104, 656, 104, 44)],
    "m": [
        ("l", 104, 456, 104, 44),
        ("a", 280, 280, 176, 180, 0),
        ("l", 456, 280, 456, 44),
        ("a", 632, 280, 176, 180, 0),
        ("l", 808, 280, 808, 44),
    ],
    "n": [("l", 104, 456, 104, 44), ("a", 310, 250, 206, 180, 0), ("l", 516, 250, 516, 44)],
    "o": [("r", 310, 250, 206)],
    "p": [("r", 310, 250, 206), ("l", 104, 456, 104, -156)],
    "q": [("r", 310, 250, 206), ("l", 516, 456, 516, -156)],
    "r": [("l", 104, 456, 104, 44), ("a", 310, 250, 206, 180, 60)],
    "s": _spine(204, 350, 150, 100),
    "t": [("l", 240, 656, 240, 150), ("a", 340, 150, 100, 180, 270), ("l", 104, 456, 400, 456)],
    "u": [("l", 104, 456, 104, 244), ("a", 310, 244, 206, 180, 360), ("l", 516, 456, 516, 244)],
    "v": [("l", 104, 456, 300, 44), ("l", 496, 456, 300, 44)],
    "w": [
        ("l", 104, 456, 240, 44),
        ("l", 240, 44, 380, 380),
        ("l", 380, 380, 520, 44),
        ("l", 520, 44, 656, 456),
    ],
    "x": [("l", 104, 456, 456, 44), ("l", 104, 44, 456, 456)],
    "y": [("l", 104, 456, 300, 60), ("l", 496, 456, 196, -156)],
    "z": [("l", 104, 456, 456, 456), ("l", 456, 456, 104, 44), ("l", 104, 44, 456, 44)],
}

_DIGITS = {
    "zero": [("r", 380, 320, 276)],
    "one": [("l", 380, 44, 380, 596), ("l", 380, 596, 230, 500)],
    "two": [
        ("a", 380, 440, 196, 190, -55),
        ("l", 492, 279, 160, 44),
        ("l", 140, 44, 640, 44),
    ],
    "three": [("a", 380, 450, 150, 190, -60), ("a", 380, 190, 150, 60, -190)],
    "four": [("l", 400, 596, 104, 200), ("l", 104, 200, 600, 200), ("l", 460, 596, 460, 44)],
    "five": [
        ("l", 150, 596, 560, 596),
        ("l", 150, 596, 150, 340),
        ("a", 330, 270, 190, 160, -160),
    ],
    "six": [("r", 330, 220, 176), ("a", 330, 420, 176, 180, 75)],
    "seven": [("l", 104, 596, 620, 596), ("l", 620, 596, 280, 44)],
    "eight": [("r", 234, 466, 130), ("r", 234, 206, 130)],
    "nine": [("r", 330, 420, 176), ("a", 330, 220, 176, 0, -105)],
}

_PUNCTUATION = {
    "period": [("d", 0, 44, DOT_RADIUS)],
    "comma": _comma(0, 44),
    "colon": [("d", 0, 44, DOT_RADIUS), ("d", 0, 350, DOT_RADIUS)],
    "semicolon": [("d", 0, 350, DOT_RADIUS)] + _comma(0, 44),
    "exclam": [("l", 0, 656, 0, 180), ("d", 0, 44, DOT_RADIUS)],
    "question": [
        ("a", 0, 540, 116, 190, -30),
        ("l", 100, 482, 0, 340),
        ("d", 0, 180, DOT_RADIUS),
    ],
    "quotesingle": [("l", 0, 656, 0, 530)],
    "quotedbl": [("l", -70, 656, -70, 530), ("l", 70, 656, 70, 530)],
    "quoteleft": _reverse_comma(0, 540),
    "quoteright": _comma(0, 610),
    "quotedblleft": _reverse_comma(-70, 540) + _reverse_comma(70, 540),
    "quotedblright": _comma(-70, 610) + _comma(70, 610),
    "quotesinglbase": _comma(0, 44),
    "quotedblbase": _comma(-70, 44) + _comma(70, 44),
    "parenleft": [("a", 300, 300, 300, 130, 230)],
    "parenright": [("a", 0, 300, 300, -50, 50)],
    "bracketleft": [("l", 0, -60, 0, 660), ("l", 0, 660, 140, 660), ("l", 0, -60, 140, -60)],
    "bracketright": [("l", 140, -60, 140, 660), ("l", 0, 660, 140, 660), ("l", 0, -60, 140, -60)],
    "braceleft": [
        ("a", 190, 536, 130, 180, 90),
        ("l", 60, 536, 60, 420),
        ("l", 60, 420, -40, 350),
        ("l", -40, 350, 60, 280),
        ("l", 60, 280, 60, 64),
        ("a", 190, 64, 130, 180, 270),
    ],
    "braceright": [
        ("a", -60, 536, 130, 0, 90),
        ("l", 70, 536, 70, 420),
        ("l", 70, 420, 170, 350),
        ("l", 170, 350, 70, 280),
        ("l", 70, 280, 70, 64),
        ("a", -60, 64, 130, 0, -90),
    ],
    "hyphen": [("l", 0, 350, 160, 350)],
    "endash": [("l", 0, 350, 260, 350)],
    "emdash": [("l", 0, 350, 420, 350)],
    "underscore": [("l", 0, -100, 420, -100)],
    "slash": [("l", 0, -60, 300, 660)],
    "backslash": [("l", 0, 660, 300, -60)],
    "bar": [("l", 0, -60, 0, 660)],
    "plus": [("l", 0, 350, 300, 350), ("l", 150, 200, 150, 500)],
    "equal": [("l", 0, 270, 300, 270), ("l", 0, 430, 300, 430)],
    "less": [("l", 300, 560, 60, 350), ("l", 60, 350, 300, 140)],
    "greater": [("l", 0, 560, 240, 350), ("l", 240, 350, 0, 140)],
    "asciitilde": [("a", 80, 350, 80, 180, 0), ("a", 240, 350, 80, 180, 360)],
    "asciicircum": [("l", 0, 500, 150, 660), ("l", 150, 660, 300, 500)],
    "asterisk": [
        ("l", 0, 540, 0, 680),
        ("l", 0, 540, -121, 470),
        ("l", 0, 540, 121, 470),
    ],
    "numbersign": [
        ("l", 140, 44, 100, 656),
        ("l", 320, 44, 280, 656),
        ("l", 40, 240, 380, 240),
        ("l", 60, 460, 400, 460),
    ],
    "percent": [("r", 150, 540, 104), ("r", 490, 160, 104), ("l", 520, 656, 120, 44)],
    "ampersand": [
        ("a", 250, 540, 120, -60, 240),
        ("l", 190, 436, 86, 155),
        ("a", 250, 215, 175, 200, 340),
        ("l", 414, 155, 560, 290),
        ("l", 310, 436, 600, 44),
    ],
    "at": [
        ("a", 360, 300, 300, -30, 290),
        ("r", 360, 300, 110),
        ("l", 470, 300, 470, 190),
        ("a", 400, 190, 70, 0, -180),
    ],
    "Euro": [
        ("a", 360, 350, 290, 45, 315),
        ("l", 60, 420, 520, 420),
        ("l", 60, 280, 520, 280),
    ],
    "dollar": [
        ("a", 254, 500, 150, 30, 270),
        ("a", 254, 200, 150, 90, -160),
        ("l", 254, 720, 254, -20),
    ],
    "section": [
        ("a", 200, 520, 105, 30, 270),
        ("a", 200, 310, 105, 90, -160),
        ("a", 200, 300, 105, 30, 270),
        ("a", 200, 90, 105, 90, -160),
    ],
    "degree": [("r", 0, 570, 80)],
}

_UMLAUTS = {
    "Adieresis": _CAPITALS["A"] + _diaeresis(240, 480, DIAERESIS_UPPER_Y),
    "Odieresis": _CAPITALS["O"] + _diaeresis(290, 530, DIAERESIS_UPPER_Y),
    "Udieresis": _CAPITALS["U"] + _diaeresis(184, 424, DIAERESIS_UPPER_Y),
    "adieresis": _SMALL["a"] + _diaeresis(200, 420, DIAERESIS_LOWER_Y),
    "odieresis": _SMALL["o"] + _diaeresis(200, 420, DIAERESIS_LOWER_Y),
    "udieresis": _SMALL["u"] + _diaeresis(200, 420, DIAERESIS_LOWER_Y),
    "germandbls": [
        ("l", 104, 44, 104, 540),
        ("a", 240, 540, 136, 180, 0),
        ("l", 376, 540, 376, 470),
        ("l", 376, 470, 300, 400),
        ("a", 300, 270, 130, 100, -130),
    ],
}

#: Skeleton of every glyph, keyed by its PostScript glyph name.
SKELETONS = {}
SKELETONS.update(_CAPITALS)
SKELETONS.update(_SMALL)
SKELETONS.update(_DIGITS)
SKELETONS.update(_PUNCTUATION)
SKELETONS.update(_UMLAUTS)

#: Glyphs whose advance width is the shared tabular width.
TABULAR_GLYPHS = set(_DIGITS)

#: Glyph name of every supported character, keyed by its code point.
CHARACTERS = {}
for _name in _CAPITALS:
    CHARACTERS[ord(_name)] = _name
for _name in _SMALL:
    CHARACTERS[ord(_name)] = _name
for _index, _name in enumerate(
    ["zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"]
):
    CHARACTERS[ord("0") + _index] = _name
CHARACTERS.update(
    {
        ord(" "): "space",
        ord("."): "period",
        ord(","): "comma",
        ord(":"): "colon",
        ord(";"): "semicolon",
        ord("!"): "exclam",
        ord("?"): "question",
        ord("'"): "quotesingle",
        ord('"'): "quotedbl",
        ord("("): "parenleft",
        ord(")"): "parenright",
        ord("["): "bracketleft",
        ord("]"): "bracketright",
        ord("{"): "braceleft",
        ord("}"): "braceright",
        ord("-"): "hyphen",
        ord("/"): "slash",
        ord("\\"): "backslash",
        ord("|"): "bar",
        ord("+"): "plus",
        ord("="): "equal",
        ord("<"): "less",
        ord(">"): "greater",
        ord("~"): "asciitilde",
        ord("^"): "asciicircum",
        ord("*"): "asterisk",
        ord("#"): "numbersign",
        ord("%"): "percent",
        ord("&"): "ampersand",
        ord("@"): "at",
        ord("_"): "underscore",
        ord("$"): "dollar",
        ord("§"): "section",
        ord("°"): "degree",
        ord("€"): "Euro",
        ord("–"): "endash",
        ord("—"): "emdash",
        ord("‘"): "quoteleft",
        ord("’"): "quoteright",
        ord("“"): "quotedblleft",
        ord("”"): "quotedblright",
        ord("‚"): "quotesinglbase",
        ord("„"): "quotedblbase",
        ord("Ä"): "Adieresis",
        ord("Ö"): "Odieresis",
        ord("Ü"): "Udieresis",
        ord("ä"): "adieresis",
        ord("ö"): "odieresis",
        ord("ü"): "udieresis",
        ord("ß"): "germandbls",
    }
)
