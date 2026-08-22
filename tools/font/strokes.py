"""Vector primitives the Ghost Writer glyphs are built from.

A glyph is described as a skeleton of strokes. Every stroke is turned into a
closed outline of straight lines and quadratic Bezier segments, which is exactly
what the TrueType ``glyf`` table stores. Nothing here produces pixels.

All outlines of a glyph run in the same direction, so overlapping strokes merge
into a single shape under the non-zero winding rule TrueType uses. Only the
counter of a closed ring runs the opposite way, which turns it into a hole.

A contour is a list of commands:

* ``("move", point)`` - start of the contour
* ``("line", point)`` - straight segment
* ``("qcurve", control, point)`` - quadratic Bezier segment
"""

import math

#: Largest angle a single quadratic segment covers, in degrees.
SEGMENT_MAX_DEGREES = 15.0


def _polar(cx, cy, radius, degrees):
    """Return the point at ``radius`` and ``degrees`` around the given centre.

    :param cx: x coordinate of the centre
    :param cy: y coordinate of the centre
    :param radius: distance from the centre
    :param degrees: angle in degrees, zero pointing right, growing anticlockwise
    """
    angle = math.radians(degrees)
    return cx + radius * math.cos(angle), cy + radius * math.sin(angle)


def _append_arc(contour, cx, cy, radius, start, end):
    """Append a circular arc to a contour as quadratic Bezier segments.

    The arc is split into segments of at most :data:`SEGMENT_MAX_DEGREES`. The
    control point of a segment sits on the intersection of the tangents, scaled
    so that the curve passes through the circle at its midpoint.

    :param contour: contour the segments are appended to
    :param cx: x coordinate of the centre
    :param cy: y coordinate of the centre
    :param radius: radius of the arc
    :param start: start angle in degrees
    :param end: end angle in degrees, smaller than ``start`` for a clockwise arc
    """
    span = end - start
    count = max(1, int(math.ceil(abs(span) / SEGMENT_MAX_DEGREES)))
    step = span / count
    control_radius = radius * (2.0 - math.cos(math.radians(abs(step)) / 2.0))
    for index in range(count):
        segment_start = start + step * index
        segment_end = segment_start + step
        control = _polar(cx, cy, control_radius, (segment_start + segment_end) / 2.0)
        contour.append(("qcurve", control, _polar(cx, cy, radius, segment_end)))


def capsule(x0, y0, x1, y1, weight):
    """Return the outline of a straight stroke with two rounded ends.

    :param x0: x coordinate of the start of the skeleton
    :param y0: y coordinate of the start of the skeleton
    :param x1: x coordinate of the end of the skeleton
    :param y1: y coordinate of the end of the skeleton
    :param weight: stroke weight, the rounded ends have half of it as radius
    """
    half = weight / 2.0
    if math.hypot(x1 - x0, y1 - y0) < 1e-9:
        return dot(x0, y0, half)

    direction = math.degrees(math.atan2(y1 - y0, x1 - x0))
    left = direction + 90.0

    contour = [("move", _polar(x0, y0, half, left))]
    contour.append(("line", _polar(x1, y1, half, left)))
    _append_arc(contour, x1, y1, half, left, left - 180.0)
    contour.append(("line", _polar(x0, y0, half, left - 180.0)))
    _append_arc(contour, x0, y0, half, left - 180.0, left - 360.0)
    return [contour]


def arc_stroke(cx, cy, radius, start, end, weight):
    """Return the outline of a circular stroke with two rounded ends.

    :param cx: x coordinate of the centre of the skeleton arc
    :param cy: y coordinate of the centre of the skeleton arc
    :param radius: radius of the skeleton arc
    :param start: start angle in degrees
    :param end: end angle in degrees, smaller than ``start`` for a clockwise arc
    :param weight: stroke weight, the rounded ends have half of it as radius
    """
    half = weight / 2.0
    sign = 1.0 if end >= start else -1.0
    first_radius = radius - sign * half
    second_radius = radius + sign * half

    contour = [("move", _polar(cx, cy, first_radius, start))]
    _append_arc(contour, cx, cy, first_radius, start, end)

    end_x, end_y = _polar(cx, cy, radius, end)
    end_cap = end + 180.0 if sign > 0 else end
    _append_arc(contour, end_x, end_y, half, end_cap, end_cap - 180.0)

    _append_arc(contour, cx, cy, second_radius, end, start)

    start_x, start_y = _polar(cx, cy, radius, start)
    start_cap = start if sign > 0 else start + 180.0
    _append_arc(contour, start_x, start_y, half, start_cap, start_cap - 180.0)
    return [contour]


def ring(cx, cy, radius, weight):
    """Return the outline of a closed circular stroke with a counter.

    :param cx: x coordinate of the centre
    :param cy: y coordinate of the centre
    :param radius: radius of the skeleton circle
    :param weight: stroke weight
    """
    half = weight / 2.0
    outer = [("move", _polar(cx, cy, radius + half, 0.0))]
    _append_arc(outer, cx, cy, radius + half, 0.0, -360.0)
    inner = [("move", _polar(cx, cy, radius - half, 0.0))]
    _append_arc(inner, cx, cy, radius - half, 0.0, 360.0)
    return [outer, inner]


def dot(cx, cy, radius):
    """Return the outline of a filled circle.

    :param cx: x coordinate of the centre
    :param cy: y coordinate of the centre
    :param radius: radius of the circle
    """
    contour = [("move", _polar(cx, cy, radius, 0.0))]
    _append_arc(contour, cx, cy, radius, 0.0, -360.0)
    return [contour]


def sample(contour, steps=12):
    """Return points along a contour, used to measure the ink of a glyph.

    :param contour: contour to walk along
    :param steps: number of samples taken on every curved segment
    """
    points = []
    current = None
    for command in contour:
        if command[0] == "move":
            current = command[1]
            points.append(current)
        elif command[0] == "line":
            current = command[1]
            points.append(current)
        else:
            control, end = command[1], command[2]
            for index in range(1, steps + 1):
                t = index / steps
                inverse = 1.0 - t
                x = inverse * inverse * current[0] + 2 * inverse * t * control[0] + t * t * end[0]
                y = inverse * inverse * current[1] + 2 * inverse * t * control[1] + t * t * end[1]
                points.append((x, y))
            current = end
    return points


def bounds(contours):
    """Return the ink bounding box of a list of contours as a tuple.

    :param contours: contours of a glyph
    """
    points = [point for contour in contours for point in sample(contour)]
    if not points:
        return None
    xs = [point[0] for point in points]
    ys = [point[1] for point in points]
    return min(xs), min(ys), max(xs), max(ys)


def translate(contours, dx, dy=0.0):
    """Return the contours moved by the given offset.

    :param contours: contours of a glyph
    :param dx: offset along the x axis
    :param dy: offset along the y axis
    """
    moved = []
    for contour in contours:
        shifted = []
        for command in contour:
            points = [(point[0] + dx, point[1] + dy) for point in command[1:]]
            shifted.append((command[0], *points))
        moved.append(shifted)
    return moved
