"""Render a specimen of the Ghost Writer font for visual inspection.

The image is a review aid only. It is never shipped and never part of the font.

Usage::

    python preview.py [font.ttf] [preview.png]
"""

import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

from build_font import DEFAULT_OUTPUT

#: Default location of the rendered specimen.
DEFAULT_PREVIEW = Path("build/font-preview.png")

#: Lines shown in the specimen, covering every supported character.
LINES = [
    ("ABCDEFGHIJKLM", 64),
    ("NOPQRSTUVWXYZ", 64),
    ("abcdefghijklm", 64),
    ("nopqrstuvwxyz", 64),
    ("0123456789", 64),
    ("ÄÖÜ äöü ß", 64),
    (".,;:!? '\"„“‚‘ ()[]{} -–— /\\|", 44),
    ("&@#%$€§°*+=<>^~_", 44),
    ("The quick brown fox jumps over the lazy dog.", 40),
    ("Franz jagt im komplett verwahrlosten Taxi quer durch Bayern.", 32),
    ("Ghost Writer 1234567890", 48),
]


def render(font_path, preview_path):
    """Render the specimen lines and write them to a PNG file.

    :param font_path: path of the TrueType file to render with
    :param preview_path: path of the image to write
    """
    margin = 40
    spacing = 26
    heights = [int(size * 1.5) for _, size in LINES]
    width = 1400
    height = margin * 2 + sum(heights) + spacing * (len(LINES) - 1)

    image = Image.new("RGB", (width, height), "#f7f9fd")
    draw = ImageDraw.Draw(image)

    y = margin
    for (text, size), line_height in zip(LINES, heights):
        font = ImageFont.truetype(str(font_path), size)
        draw.text((margin, y), text, font=font, fill="#1e2a4a")
        y += line_height + spacing

    preview_path.parent.mkdir(parents=True, exist_ok=True)
    image.save(preview_path)
    return preview_path


def main():
    """Render the specimen of the built font."""
    font_path = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_OUTPUT
    preview_path = Path(sys.argv[2]) if len(sys.argv) > 2 else DEFAULT_PREVIEW
    print(f"Wrote {render(font_path, preview_path)}")


if __name__ == "__main__":
    main()
