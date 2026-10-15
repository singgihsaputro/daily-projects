#!/usr/bin/env python3
"""Builds book.pdf: "Fruit and Veg", a 26-page activity book for ages 1-5.

Run `python3 generate.py` (needs `pip install reportlab`). The output is
deterministic: fixed random seed, fixed PDF metadata and invariant mode.
"""
import math
import random
from contextlib import contextmanager

from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas

W, H = A4
MARGIN = 42.5                       # 15 mm safe margin
LEFT, RIGHT, TOP, BOTTOM = MARGIN, W - MARGIN, H - MARGIN, 62
CX, CY = W / 2, (TOP + BOTTOM) / 2
LW = 4                              # minimum stroke, in points
OUT = "book.pdf"
random.seed(2026)


# ---------------------------------------------------------------- primitives
def pen(c, width=LW):
    c.setLineWidth(width)
    c.setLineCap(1)
    c.setLineJoin(1)
    c.setStrokeGray(0)
    c.setFillGray(0)
    c.setDash()


def dot(c, x, y, r):
    c.circle(x, y, r, stroke=0, fill=1)


def line_path(c, pts, close=False):
    p = c.beginPath()
    p.moveTo(*pts[0])
    for q in pts[1:]:
        p.lineTo(*q)
    if close:
        p.close()
    return p


def curve(c, start, *segs):
    p = c.beginPath()
    p.moveTo(*start)
    for s in segs:
        p.curveTo(*s)
    return p


def stroke(c, p):
    c.drawPath(p, stroke=1, fill=0)


# ---------------------------------------------------------------- produce
# Each drawer is centred on the origin and about 100 units wide.
def draw_apple(c):
    stroke(c, curve(c, (0, 36), (-20, 50, -52, 36, -48, -4), (-44, -44, -14, -52, 0, -40),
                    (14, -52, 44, -44, 48, -4), (52, 36, 20, 50, 0, 36)))
    stroke(c, line_path(c, [(0, 36), (6, 56)]))
    stroke(c, curve(c, (8, 50), (20, 68, 40, 64, 44, 52), (30, 40, 16, 40, 8, 50)))


def draw_banana(c):
    stroke(c, curve(c, (-52, 44), (-60, -30, 10, -58, 58, -8), (62, -4, 64, 4, 62, 8),
                    (16, -26, -24, -14, -32, 40), (-36, 48, -48, 50, -52, 44)))
    stroke(c, line_path(c, [(-52, 44), (-54, 58)]))
    stroke(c, line_path(c, [(-32, 40), (-30, 56), (-54, 58)]))


def draw_strawberry(c):
    stroke(c, curve(c, (-44, 30), (-52, 0, -22, -36, 0, -54), (22, -36, 52, 0, 44, 30),
                    (30, 48, -30, 48, -44, 30)))
    stroke(c, line_path(c, [(-30, 40), (-14, 62), (0, 42), (14, 62), (30, 40)]))
    for x, y in [(-20, 14), (8, 20), (28, 6), (-4, -8), (-24, -12), (14, -22), (-4, -34)]:
        dot(c, x, y, 4)


def draw_carrot(c):
    stroke(c, curve(c, (-34, 26), (-34, 36, 34, 36, 34, 26), (30, -6, 8, -46, 0, -60)))
    stroke(c, curve(c, (0, -60), (-8, -46, -30, -6, -34, 26)))
    for x1, y1, x2, y2 in [(-14, 12, -4, 10), (8, -6, 18, -8), (-8, -22, 0, -24)]:
        stroke(c, line_path(c, [(x1, y1), (x2, y2)]))
    for dx in (-26, 0, 26):
        stroke(c, curve(c, (dx * 0.4, 34), (dx * 1.6 - 14, 58, dx * 1.6 - 10, 74, dx * 1.2, 84),
                        (dx * 1.6 + 10, 74, dx * 1.6 + 14, 58, dx * 0.4, 34)))


def draw_pear(c):
    stroke(c, curve(c, (0, 54), (-18, 54, -16, 20, -26, 8), (-60, -14, -48, -52, 0, -52),
                    (48, -52, 60, -14, 26, 8), (16, 20, 18, 54, 0, 54)))
    stroke(c, line_path(c, [(0, 54), (4, 70)]))
    stroke(c, curve(c, (6, 62), (18, 76, 36, 72, 40, 62), (28, 52, 14, 54, 6, 62)))


def draw_pumpkin(c):
    c.ellipse(-52, -38, 52, 38)
    c.ellipse(-30, -40, 30, 40)
    stroke(c, line_path(c, [(0, 38), (4, 56)]))
    stroke(c, line_path(c, [(4, 56), (14, 60)]))


def draw_cherries(c):
    c.circle(-26, -30, 22)
    c.circle(26, -34, 22)
    stroke(c, curve(c, (-26, -8), (-26, 20, -8, 40, 8, 54)))
    stroke(c, curve(c, (26, -12), (26, 20, 20, 40, 8, 54)))
    stroke(c, curve(c, (8, 54), (20, 64, 36, 64, 44, 56), (34, 46, 18, 46, 8, 54)))


def draw_grapes(c):
    rows = [3, 3, 2, 1]
    for r, n in enumerate(rows):
        for i in range(n):
            c.circle((i - (n - 1) / 2) * 34, 30 - r * 32, 16)
    stroke(c, line_path(c, [(0, 46), (0, 62)]))
    stroke(c, curve(c, (0, 58), (10, 72, 30, 70, 36, 60), (24, 50, 8, 52, 0, 58)))


def draw_orange(c):
    c.circle(0, -4, 46)
    stroke(c, curve(c, (0, 42), (10, 58, 30, 58, 36, 50), (28, 40, 12, 38, 0, 42)))
    dot(c, 0, 24, 4)


ITEMS = {"apple": draw_apple, "banana": draw_banana, "strawberry": draw_strawberry,
         "carrot": draw_carrot, "pear": draw_pear, "pumpkin": draw_pumpkin,
         "cherries": draw_cherries, "grapes": draw_grapes, "orange": draw_orange}
NATURAL = {k: 110 for k in ITEMS}


@contextmanager
def placed(c, x, y, width):
    k = width / 110
    c.saveState()
    c.translate(x, y)
    c.scale(k, k)
    pen(c, LW / k)
    yield
    c.restoreState()


def item(c, kind, x, y, width=110):
    with placed(c, x, y, width):
        ITEMS[kind](c)


# ---------------------------------------------------------------- page chrome
def footer(c, ages, note):
    c.setFillGray(0.25)
    c.setFont("Helvetica", 9)
    c.drawCentredString(CX, 48, f"For the grown-up: {note}  |  Suits ages {ages}")


def outlined_number(c, n, x, y, size=110):
    c.saveState()
    c.setFont("Helvetica-Bold", size)
    c.setLineWidth(LW)
    c.setStrokeGray(0)
    c.setFillGray(1)
    t = c.beginText(x - c.stringWidth(str(n), "Helvetica-Bold", size) / 2, y)
    t.setTextRenderMode(1)
    t.textOut(str(n))
    c.drawText(t)
    c.restoreState()


def dashed(c, p):
    c.saveState()
    pen(c, LW)
    c.setDash(14, 12)
    c.drawPath(p, stroke=1, fill=0)
    c.restoreState()


def start_marker(c, x, y):
    c.saveState()
    pen(c)
    c.circle(x, y, 9, stroke=0, fill=1)
    c.restoreState()


def ring_zone(c, x, y, w, h):
    c.saveState()
    pen(c, 3)
    c.setDash(6, 6)
    c.roundRect(x - w / 2, y - h / 2, w, h, 14)
    c.restoreState()


# ---------------------------------------------------------------- page types
def cover(c):
    c.setFont("Helvetica-Bold", 54)
    c.drawCentredString(CX, TOP - 70, "FRUIT")
    c.drawCentredString(CX, TOP - 130, "and VEG")
    for kind, x, y in [("apple", CX - 120, CY + 150), ("banana", CX + 120, CY + 150),
                       ("carrot", CX - 120, CY - 70), ("strawberry", CX + 120, CY - 50),
                       ("pumpkin", CX, CY - 250)]:
        item(c, kind, x, y, 150)
    footer(c, "1-5", "an activity book to print on A4 at 100% scale")


def trace_lines(c, vertical):
    pen(c)
    for i in range(5):
        if vertical:
            x = LEFT + 50 + i * (RIGHT - LEFT - 100) / 4
            p = line_path(c, [(x, TOP - 30), (x, BOTTOM + 40)])
            start_marker(c, x, TOP - 30)
        else:
            y = TOP - 60 - i * 150
            p = line_path(c, [(LEFT + 20, y), (RIGHT - 20, y)])
            start_marker(c, LEFT + 20, y)
        dashed(c, p)
    footer(c, "1-2", "follow each dashed line from the dot with a fat crayon")


def colour_page(c, kind, ages, width=380):
    item(c, kind, CX, CY, width)
    footer(c, ages, "colour it in any way you like; scribbling is great practice")


def trace_curves(c, kind):
    pen(c)
    for i in range(4):
        y = TOP - 90 - i * 170
        pts = [(LEFT + 10, y)]
        n = 4
        seg = (RIGHT - LEFT - 20) / n
        p = c.beginPath()
        p.moveTo(*pts[0])
        for k in range(n):
            x0 = LEFT + 10 + k * seg
            s = 1 if (k + i) % 2 == 0 else -1
            if kind == "waves":
                p.curveTo(x0 + seg / 3, y + 70 * s, x0 + 2 * seg / 3, y + 70 * s, x0 + seg, y)
            else:
                p.lineTo(x0 + seg / 2, y + 60 * s)
                p.lineTo(x0 + seg, y)
        start_marker(c, LEFT + 10, y)
        dashed(c, p)
    footer(c, "2-5", "trace the bumps" if kind == "waves" else "trace the zigzags")


def trace_circle_page(c):
    for i, (x, y) in enumerate([(CX - 120, CY + 200), (CX + 120, CY + 200),
                                (CX - 120, CY - 40), (CX + 120, CY - 40)]):
        r = 80
        p = c.beginPath()
        p.arc(x - r, y - r, x + r, y + r, 90, 359)
        dashed(c, p)
        start_marker(c, x, y + r)
    item(c, "orange", CX, CY - 260, 130)
    footer(c, "2-4", "start at the dot and go round once; the orange is a circle too")


def trace_shapes_page(c):
    pen(c)
    s = 150
    shapes = [
        ("square", [(-s / 2, -s / 2), (-s / 2, s / 2), (s / 2, s / 2), (s / 2, -s / 2), (-s / 2, -s / 2)]),
        ("triangle", [(0, s / 2 + 10), (-s / 2 - 10, -s / 2), (s / 2 + 10, -s / 2), (0, s / 2 + 10)]),
        ("diamond", [(0, s / 2 + 20), (-s / 2, 0), (0, -s / 2 - 20), (s / 2, 0), (0, s / 2 + 20)]),
        ("rectangle", [(-100, -50), (-100, 50), (100, 50), (100, -50), (-100, -50)]),
    ]
    spots = [(CX - 120, CY + 200), (CX + 120, CY + 200), (CX - 120, CY - 80), (CX + 100, CY - 80)]
    for (name, pts), (x, y) in zip(shapes, spots):
        scale = 0.78 if name == "rectangle" else 1
        moved = [(x + px * scale, y + py * scale) for px, py in pts]
        dashed(c, line_path(c, moved))
        start_marker(c, *moved[0])
    footer(c, "3-4", "square, triangle, diamond and rectangle; start at each dot")


def scatter(n, cx, cy, w, h):
    """Deterministic non-overlapping positions on a loose grid."""
    cols = 3 if n > 4 else 2
    rows = math.ceil(n / cols)
    cells = [(i % cols, i // cols) for i in range(n)]
    out = []
    for col, row in cells:
        x = cx + (col - (cols - 1) / 2) * (w / cols)
        y = cy + ((rows - 1) / 2 - row) * (h / rows)
        out.append((x + random.uniform(-8, 8), y + random.uniform(-8, 8)))
    return out


def counting_page(c, n, kind, ages):
    for x, y in scatter(n, CX, CY + 90, 400, 470):
        item(c, kind, x, y, 120 if n > 6 else (125 if n > 4 else 160))
    ring_zone(c, CX, BOTTOM + 82, 170, 150)
    outlined_number(c, n, CX, BOTTOM + 42)
    footer(c, ages, f"count the pieces together, then trace the number {n}")


def matching_page(c):
    kinds = ["apple", "banana", "carrot", "cherries"]
    right = ["cherries", "apple", "carrot", "banana"]
    for i, (a, b) in enumerate(zip(kinds, right)):
        y = TOP - 100 - i * 175
        item(c, a, LEFT + 80, y, 120)
        item(c, b, RIGHT - 80, y, 120)
    pen(c)
    for i in range(4):
        y = TOP - 100 - i * 175
        c.circle(LEFT + 175, y, 9, stroke=0, fill=1)
        c.circle(RIGHT - 175, y, 9, stroke=0, fill=1)
    footer(c, "3-4", "draw a line from each fruit or veg to its twin")


def odd_one_out_page(c):
    rows = [("apple", "banana"), ("carrot", "pear"), ("strawberry", "grapes"), ("pumpkin", "orange")]
    odd_at = [2, 0, 1, 2]
    for i, ((a, b), odd) in enumerate(zip(rows, odd_at)):
        y = TOP - 90 - i * 180
        for j in range(3):
            item(c, b if j == odd else a, CX + (j - 1) * 165, y, 130)
    footer(c, "3-5", "circle the one that is different in each row")


def size_page(c, kind, smallest):
    if smallest:
        rows = [[135, 70, 100], [75, 115, 145], [145, 95, 72]]
    else:
        rows = [[80, 145, 110], [145, 75, 115], [85, 100, 150]]
    for y, row in zip([TOP - 130, CY, BOTTOM + 150], rows):
        for x, width in zip([CX - 175, CX, CX + 175], row):
            item(c, kind, x, y, width)
    footer(c, "2-5", "circle the smallest one in each row" if smallest else "circle the biggest one in each row")


def pattern_page(c):
    patterns = [["apple", "banana", "apple", "banana", "apple"],
                ["carrot", "carrot", "pear", "carrot", "carrot"],
                ["orange", "grapes", "cherries", "orange", "grapes"]]
    for i, pat in enumerate(patterns):
        y = TOP - 100 - i * 220
        step = (RIGHT - LEFT) / 6
        for j, k in enumerate(pat):
            item(c, k, LEFT + step * (j + 0.5), y, 72)
        ring_zone(c, LEFT + step * 5.5, y, 82, 100)
    footer(c, "4-5", "what comes next? draw it in the dashed box")


# ---------------------------------------------------------------- mazes
def build_maze(cols, rows):
    seen = {(0, 0)}
    open_walls = set()
    stack = [(0, 0)]
    while stack:
        x, y = stack[-1]
        nbrs = [(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))
                if 0 <= x + dx < cols and 0 <= y + dy < rows and (x + dx, y + dy) not in seen]
        if not nbrs:
            stack.pop()
            continue
        n = random.choice(nbrs)
        open_walls.add(frozenset(((x, y), n)))
        seen.add(n)
        stack.append(n)
    return open_walls


def maze_page(c, cols, rows, ages, start_kind, end_kind):
    """Mazes open at the left of the top row and the right of the bottom row."""
    cell = min((RIGHT - LEFT - 190) / cols, (TOP - BOTTOM - 120) / rows)
    ox = CX - cols * cell / 2
    oy = CY - rows * cell / 2
    opened = build_maze(cols, rows)
    pen(c)
    for x in range(cols + 1):
        for y in range(rows):
            top_row, bottom_row = rows - 1, 0
            if x in (0, cols):
                if (x == 0 and y == top_row) or (x == cols and y == bottom_row):
                    continue
                c.line(ox + x * cell, oy + y * cell, ox + x * cell, oy + (y + 1) * cell)
            elif frozenset(((x - 1, rows - 1 - y), (x, rows - 1 - y))) not in opened:
                c.line(ox + x * cell, oy + y * cell, ox + x * cell, oy + (y + 1) * cell)
    for y in range(rows + 1):
        for x in range(cols):
            if y in (0, rows):
                c.line(ox + x * cell, oy + y * cell, ox + (x + 1) * cell, oy + y * cell)
            elif frozenset(((x, rows - y), (x, rows - 1 - y))) not in opened:
                c.line(ox + x * cell, oy + y * cell, ox + (x + 1) * cell, oy + y * cell)
    item(c, start_kind, LEFT + 40, oy + (rows - 0.5) * cell, 80)
    item(c, end_kind, RIGHT - 40, oy + 0.5 * cell, 80)
    footer(c, ages, "find the way in from the left to the finish on the right")


# ---------------------------------------------------------------- dot to dot
def dot_to_dot(c, pts, ages, note):
    cx = sum(p[0] for p in pts) / len(pts)
    cy = sum(p[1] for p in pts) / len(pts)
    pen(c)
    for i, (x, y) in enumerate(pts):
        c.circle(x, y, 9, stroke=0, fill=1)
        d = math.hypot(x - cx, y - cy) or 1
        lx, ly = x + (x - cx) / d * 34, y + (y - cy) / d * 34
        c.setFont("Helvetica-Bold", 30)
        c.drawCentredString(lx, ly - 10, str(i + 1))
    footer(c, ages, note)


def house_points():
    return [(CX, CY + 200), (CX + 190, CY + 20), (CX + 190, CY - 200),
            (CX - 190, CY - 200), (CX - 190, CY + 20)]


def star_points():
    pts = []
    for i in range(10):
        r = 200 if i % 2 == 0 else 95
        a = math.radians(90 - i * 36)
        pts.append((CX + r * math.cos(a), CY + r * math.sin(a)))
    return pts


# ---------------------------------------------------------------- certificate
def certificate(c):
    pen(c, 6)
    c.roundRect(LEFT + 10, BOTTOM + 40, RIGHT - LEFT - 20, TOP - BOTTOM - 60, 24)
    c.setFont("Helvetica-Bold", 48)
    c.drawCentredString(CX, TOP - 130, "WELL DONE!")
    for kind, x, y in [("apple", CX - 110, CY + 40), ("banana", CX + 110, CY + 50),
                       ("strawberry", CX, CY - 110)]:
        item(c, kind, x, y, 170)
    c.setLineWidth(3)
    c.line(CX - 150, BOTTOM + 110, CX + 150, BOTTOM + 110)
    footer(c, "all", "write the child's name on the line and add a sticker")


# ---------------------------------------------------------------- the book
PAGES = [
    cover,
    lambda c: trace_lines(c, False),
    lambda c: trace_lines(c, True),
    lambda c: colour_page(c, "apple", "1-3"),
    lambda c: colour_page(c, "banana", "1-3"),
    lambda c: trace_curves(c, "waves"),
    lambda c: size_page(c, "apple", False),
    lambda c: colour_page(c, "strawberry", "2-4"),
    lambda c: counting_page(c, 3, "apple", "2-3"),
    lambda c: colour_page(c, "carrot", "2-4", 360),
    trace_circle_page,
    lambda c: trace_curves(c, "zigzag"),
    lambda c: colour_page(c, "pear", "2-5"),
    lambda c: counting_page(c, 5, "cherries", "3-4"),
    matching_page,
    lambda c: colour_page(c, "pumpkin", "2-5"),
    trace_shapes_page,
    odd_one_out_page,
    lambda c: maze_page(c, 4, 5, "3-4", "grapes", "pumpkin"),
    lambda c: counting_page(c, 8, "strawberry", "4-5"),
    pattern_page,
    lambda c: size_page(c, "pear", True),
    lambda c: dot_to_dot(c, house_points(), "3-4", "join the dots 1 to 5, then back to 1"),
    lambda c: dot_to_dot(c, star_points(), "4-5", "join the dots 1 to 10, then back to 1"),
    lambda c: maze_page(c, 6, 8, "4-5", "banana", "orange"),
    certificate,
]


def main():
    c = canvas.Canvas(OUT, pagesize=A4, invariant=1, pageCompression=1)
    c.setTitle("Fruit and Veg: an activity book for ages 1-5")
    c.setAuthor("daily-projects")
    for draw in PAGES:
        pen(c)
        draw(c)
        c.showPage()
    c.save()
    print(f"wrote {OUT} with {len(PAGES)} pages")


if __name__ == "__main__":
    main()
