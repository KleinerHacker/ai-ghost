"""Design metrics of the Ghost Writer font.

All values are given in font units and refer to an em square of 1000 units. The
design follows the ``AI`` lettering of the project logo: a geometric, monolinear
sans with a single stroke weight and fully rounded terminals.
"""

#: Units per em square.
UPM = 1000

#: Height of the capital letters above the baseline.
CAP_HEIGHT = 700

#: Height of the lowercase letters without ascender.
X_HEIGHT = 500

#: Top of the ascending lowercase letters, identical to the capital height.
ASCENDER = 700

#: Bottom of the descending letters below the baseline.
DESCENDER = -200

#: Height of the digits above the baseline.
DIGIT_HEIGHT = 640

#: Stroke weight of every stem, bowl and diagonal.
STROKE = 88

#: Half stroke weight, the radius of every rounded terminal.
HALF = STROKE // 2

#: Radius of a full stop, a dot above ``i`` and of a diaeresis dot.
DOT_RADIUS = 46

#: Space kept left and right of the ink of a glyph.
SIDE_BEARING = 60

#: Advance width shared by all digits so that numbers align in columns.
TABULAR_WIDTH = 700

#: Advance width of the space glyph.
SPACE_WIDTH = 260

#: Vertical centre of a diaeresis above a lowercase letter.
DIAERESIS_LOWER_Y = 600

#: Vertical centre of a diaeresis above a capital letter.
DIAERESIS_UPPER_Y = 790

#: Ascent reported to the layout engine, leaving room for the diaeresis.
LINE_ASCENT = 900

#: Descent reported to the layout engine.
LINE_DESCENT = -250

#: Recommended gap between two baselines in addition to ascent and descent.
LINE_GAP = 0
