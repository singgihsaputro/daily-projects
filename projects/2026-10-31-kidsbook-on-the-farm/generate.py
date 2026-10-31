#!/usr/bin/env python3
"""Builds book.pdf: "On the Farm", a 27-page farm animal activity book for ages 1-5.

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


def dashed(c, width=LW):
    pen(c, width)
    c.setLineCap(0)
    c.setDash(10, 9)


def dot(c, x, y, r):
    c.circle(x, y, r, stroke=0, fill=1)


def poly(c, pts, close=True):
    p = c.beginPath()
    p.moveTo(*pts[0])
    for q in pts[1:]:
        p.lineTo(*q)
    if close:
        p.close()
    c.drawPath(p, stroke=1, fill=0)


@contextmanager
def at(c, x, y, s=1.0):
    """Draw in animal units at (x, y), scaled by s, with the stroke kept at LW."""
    c.saveState()
    c.translate(x, y)
    c.scale(s, s)
    pen(c, LW / s)
    yield
    c.restoreState()


def star(c, x, y, r):
    pts = []
    for i in range(10):
        a = math.pi / 2 + i * math.pi / 5
        rr = r if i % 2 == 0 else r * 0.45
        pts.append((x + rr * math.cos(a), y + rr * math.sin(a)))
    poly(c, pts)


def white(c):
    c.setFillGray(1)


def blob(c, x0, y0, x1, y1):
    white(c)
    c.ellipse(x0, y0, x1, y1, stroke=1, fill=1)
    c.setFillGray(0)


def disc(c, x, y, r):
    white(c)
    c.circle(x, y, r, stroke=1, fill=1)
    c.setFillGray(0)


def box(c, x, y, w, h, r=4):
    white(c)
    c.roundRect(x, y, w, h, r, stroke=1, fill=1)
    c.setFillGray(0)


def tri(c, pts):
    white(c)
    p = c.beginPath()
    p.moveTo(*pts[0])
    for q in pts[1:]:
        p.lineTo(*q)
    p.close()
    c.drawPath(p, stroke=1, fill=1)
    c.setFillGray(0)


def curl(c, pts):
    p = c.beginPath()
    p.moveTo(*pts[0])
    p.curveTo(*pts[1], *pts[2], *pts[3])
    c.drawPath(p, stroke=1, fill=0)


# ---------------------------------------------------------------- animals
# Each drawer is centred on the origin and about 100 units wide, facing right.
def draw_cow(c):
    for x in (-36, -14, 14, 36):
        box(c, x - 6, -40, 12, 30, 3)
    curl(c, [(-50, 20), (-64, 20), (-64, 0), (-58, -10)])
    box(c, -50, -18, 100, 52, 22)
    for x, y in ((-24, 12), (6, 22)):
        disc(c, x, y, 8)
    box(c, 40, 8, 40, 40, 14)
    tri(c, [(44, 46), (38, 62), (54, 48)])
    tri(c, [(70, 46), (80, 62), (62, 48)])
    box(c, 56, 8, 24, 18, 8)
    dot(c, 62, 16, 2.5)
    dot(c, 74, 16, 2.5)
    dot(c, 56, 34, 3.5)
    dot(c, 70, 34, 3.5)


def draw_pig(c):
    for x in (-34, 22):
        box(c, x - 6, -38, 12, 26, 3)
    blob(c, -56, -24, 38, 34)
    curl(c, [(-54, 8), (-72, 14), (-66, -6), (-58, 0)])
    disc(c, 36, 12, 26)
    tri(c, [(24, 32), (24, 54), (40, 36)])
    blob(c, 42, -2, 66, 20)
    dot(c, 50, 9, 2.5)
    dot(c, 58, 9, 2.5)
    dot(c, 30, 22, 3.5)


def draw_sheep(c):
    for x in (-30, 14):
        box(c, x - 5, -40, 10, 26, 3)
    for x, y, r in ((-34, 6, 22), (-10, 22, 24), (16, 14, 24), (-6, -6, 26), (24, -4, 20), (-44, -8, 16)):
        disc(c, x, y, r)
    box(c, 34, -6, 34, 40, 14)
    tri(c, [(38, 28), (26, 36), (38, 40)])
    dot(c, 48, 16, 3.5)
    dot(c, 60, 6, 2.5)


def draw_hen(c):
    for x in (-8, 10):
        c.line(x, -20, x, -42)
        c.line(x, -42, x + 10, -42)
    tri(c, [(-34, 10), (-62, 40), (-56, 4)])
    blob(c, -44, -26, 36, 28)
    curl(c, [(-22, 10), (-10, -8), (10, -8), (20, 8)])
    disc(c, 36, 30, 16)
    tri(c, [(48, 34), (68, 28), (48, 22)])
    disc(c, 30, 48, 6)
    disc(c, 40, 50, 6)
    dot(c, 40, 34, 3)


def draw_duck(c):
    curl(c, [(-60, -24), (-30, -34), (30, -34), (60, -24)])
    tri(c, [(-44, 0), (-64, 24), (-34, 12)])
    blob(c, -48, -22, 36, 24)
    curl(c, [(-24, 4), (-10, -12), (12, -12), (26, 6)])
    disc(c, 34, 36, 18)
    tri(c, [(48, 38), (72, 30), (48, 26)])
    dot(c, 38, 42, 3.5)


def draw_cat(c):
    for x in (-30, 16):
        box(c, x - 6, -40, 12, 24, 3)
    curl(c, [(-48, -4), (-80, -10), (-72, 40), (-60, 34)])
    blob(c, -50, -24, 34, 28)
    disc(c, 38, 24, 22)
    tri(c, [(24, 40), (22, 66), (38, 44)])
    tri(c, [(52, 44), (60, 66), (66, 38)])
    dot(c, 31, 28, 3.5)
    dot(c, 46, 28, 3.5)
    tri(c, [(34, 20), (42, 20), (38, 14)])
    c.line(46, 16, 68, 20)
    c.line(46, 12, 68, 8)


def draw_dog(c):
    for x in (-34, -12, 14, 32):
        box(c, x - 6, -40, 12, 28, 3)
    curl(c, [(-54, 10), (-70, 24), (-68, 36), (-60, 44)])
    box(c, -54, -18, 100, 44, 20)
    box(c, 36, 8, 36, 36, 14)
    blob(c, 56, 8, 82, 28)
    dot(c, 78, 22, 4)
    dot(c, 50, 30, 3.5)
    tri(c, [(34, 36), (20, 12), (42, 22)])


ANIMALS = {"cow": draw_cow, "pig": draw_pig, "sheep": draw_sheep, "hen": draw_hen,
           "duck": draw_duck, "cat": draw_cat, "dog": draw_dog}


def animal(c, name, x, y, s=1.0):
    with at(c, x, y, s):
        ANIMALS[name](c)


# ---------------------------------------------------------------- page chrome
PAGES = []


def page(fn):
    PAGES.append(fn)
    return fn


def footer(c, n, note, ages):
    c.saveState()
    c.setFillGray(0.35)
    c.setFont("Helvetica", 8)
    c.drawString(LEFT, 36, f"For the grown-up: {note}")
    c.drawString(LEFT, 25, f"Suits ages {ages}.")
    c.drawRightString(RIGHT, 25, str(n))
    c.restoreState()


# ---------------------------------------------------------------- cover
@page
def cover(c):
    pen(c)
    c.setFont("Helvetica-Bold", 54)
    c.drawCentredString(CX, TOP - 70, "On the Farm")
    c.setFont("Helvetica", 18)
    c.drawCentredString(CX, TOP - 105, "An activity book for ages 1 to 5")
    animal(c, "cow", CX, CY - 10, 3.1)
    animal(c, "pig", CX - 130, BOTTOM + 70, 1.2)
    animal(c, "cat", CX + 130, BOTTOM + 80, 1.3)
    return "Print on A4 at 100%. Pages go from easiest to hardest, so start at the front.", "1-5"


# ---------------------------------------------------------------- tracing
@page
def trace_horizontal(c):
    names = ["cow", "pig", "sheep", "hen", "duck"]
    for i, name in enumerate(names):
        y = TOP - 80 - i * 135
        animal(c, name, LEFT + 60, y, 0.9)
        dashed(c)
        c.line(LEFT + 130, y, RIGHT - 20, y)
        pen(c)
        dot(c, RIGHT - 8, y, 9)
    return "Trace each path from the animal to the dot (the barn) with a fat crayon. Any wobbly line is a good line.", "1-2"


@page
def trace_vertical(c):
    names = ["pig", "cow", "hen", "cat"]
    for i, name in enumerate(names):
        x = LEFT + 65 + i * 128
        animal(c, name, x, TOP - 60, 0.7)
        dashed(c)
        c.line(x, TOP - 125, x, BOTTOM + 40)
        pen(c)
        dot(c, x, BOTTOM + 30, 9)
    return "Trace each path from the animal down to the dot. Let your child choose which animal goes first.", "1-2"


@page
def colour_cow(c):
    animal(c, "cow", CX, CY, 3.3)
    return "Colour the cow with any crayon. Scribbling counts. Say moo together.", "1-3"


@page
def colour_pig(c):
    animal(c, "pig", CX, CY, 3.3)
    return "Colour the pig. Ask what sound a pig makes and what colour it should be.", "1-3"


@page
def trace_waves(c):
    for i in range(4):
        y = TOP - 100 - i * 170
        pts = [(LEFT + 20 + t, y + 40 * math.sin(t / 38)) for t in range(0, 440, 4)]
        dashed(c)
        p = c.beginPath()
        p.moveTo(*pts[0])
        for q in pts[1:]:
            p.lineTo(*q)
        c.drawPath(p, stroke=1, fill=0)
        pen(c)
        dot(c, pts[0][0], pts[0][1], 9)
        animal(c, "cow", RIGHT - 25, y + 40 * math.sin(440 / 38) + 40, 0.5)
    return "Trace the hilly paths left to right. Say 'up and over' as the crayon climbs.", "2-4"


@page
def size_biggest(c):
    for name, s, y in (("hen", 1.0, TOP - 90), ("hen", 1.8, CY - 20), ("hen", 1.4, BOTTOM + 90)):
        animal(c, name, CX, y, s)
    return "Ask your child to circle the BIGGEST hen. The small ones are shown smaller on purpose.", "2-4"


@page
def colour_duck(c):
    animal(c, "duck", CX, CY, 3.3)
    return "Colour the duck. Talk about how ducks love to swim.", "1-3"


def counting(c, name, n, cols):
    rows = math.ceil(n / cols)
    pitch_x, pitch_y = 150, 100
    top = TOP - 40
    for i in range(n):
        r, k = divmod(i, cols)
        in_row = min(cols, n - r * cols)
        x = CX + (k - (in_row - 1) / 2) * pitch_x
        animal(c, name, x, top - 40 - r * pitch_y, 0.85)
    base = top - rows * pitch_y - 40
    c.saveState()
    pen(c, 3)
    t = c.beginText()
    t.setTextRenderMode(1)
    t.setFont("Helvetica-Bold", 220)
    w = c.stringWidth(str(n), "Helvetica-Bold", 220)
    t.setTextOrigin(CX - w / 2, BOTTOM + 30 if base > BOTTOM + 330 else BOTTOM + 10)
    t.textOut(str(n))
    c.drawText(t)
    c.restoreState()


@page
def count_three(c):
    counting(c, "cow", 3, 3)
    return "Point to each cow and count out loud, then trace the big outlined 3.", "2-4"


@page
def colour_sheep(c):
    animal(c, "sheep", CX, CY, 3.3)
    return "Colour the sheep. Ask what sound it makes and what wool feels like.", "1-3"


@page
def trace_circles(c):
    for i in range(6):
        r, k = divmod(i, 2)
        x, y = CX + (k - 0.5) * 250, TOP - 110 - r * 235
        dashed(c)
        c.circle(x, y, 80, stroke=1, fill=0)
        pen(c)
        dot(c, x, y + 80, 9)
    return "Trace each circle starting at the dot, going round like a bale of hay.", "2-4"


@page
def trace_zigzag(c):
    for i in range(4):
        y = TOP - 90 - i * 170
        pts = [(LEFT + 25 + k * 55, y + (45 if k % 2 else -45)) for k in range(9)]
        dashed(c)
        p = c.beginPath()
        p.moveTo(*pts[0])
        for q in pts[1:]:
            p.lineTo(*q)
        c.drawPath(p, stroke=1, fill=0)
        pen(c)
        dot(c, pts[0][0], pts[0][1], 9)
        star(c, pts[-1][0] + 8, pts[-1][1], 20)
    return "Trace the bumpy paths from the dot to the star. 'Bump, bump, bump!'", "2-4"


@page
def colour_cat(c):
    animal(c, "cat", CX, CY, 3.3)
    return "Colour the cat. Practise a soft, gentle stroke like patting a real cat.", "1-3"


@page
def count_five(c):
    counting(c, "pig", 5, 3)
    return "Count the pigs together, then trace the big outlined 5.", "2-4"


@page
def trace_shapes(c):
    s = 150
    shapes = [
        [(-s / 2, -s / 2), (s / 2, -s / 2), (s / 2, s / 2), (-s / 2, s / 2)],
        [(-s / 2, -s / 2), (s / 2, -s / 2), (0, s / 2)],
        [(0, -s / 2), (s / 2, 0), (0, s / 2), (-s / 2, 0)],
        [(-s * 0.7, -s / 3), (s * 0.7, -s / 3), (s * 0.7, s / 3), (-s * 0.7, s / 3)],
    ]
    for i, pts in enumerate(shapes):
        r, k = divmod(i, 2)
        x, y = CX + (k - 0.5) * 260, CY + 150 - r * 300
        dashed(c)
        poly(c, [(x + a, y + b) for a, b in pts])
        pen(c)
        dot(c, x + pts[0][0], y + pts[0][1], 10)
    return "Square, triangle, diamond, rectangle. Trace each from the dot and say its name.", "3-5"


@page
def colour_hen(c):
    animal(c, "hen", CX, CY, 3.3)
    return "Colour the hen. Ask what a hen gives us (eggs!).", "2-5"


@page
def matching(c):
    names = ["cow", "pig", "sheep", "cat"]
    right = names[2:] + names[:2]
    random.shuffle(right)
    for i, name in enumerate(names):
        y = TOP - 100 - i * 190
        animal(c, name, LEFT + 70, y, 1.0)
        dot(c, LEFT + 150, y, 10)
        animal(c, right[i], RIGHT - 70, y, 1.0)
        dot(c, RIGHT - 150, y, 10)
    return "Draw a line from each animal on the left to its matching twin on the right.", "3-4"


@page
def odd_one_out(c):
    rows = [["cow", "cow", "pig", "cow"], ["duck", "cat", "duck", "duck"],
            ["sheep", "sheep", "sheep", "hen"], ["dog", "dog", "cow", "dog"]]
    for r, row in enumerate(rows):
        y = TOP - 90 - r * 175
        for k, name in enumerate(row):
            animal(c, name, LEFT + 60 + k * 130, y, 0.78)
    return "Which one is different? Ask your child to circle it. Four rows, one odd animal each.", "3-5"


@page
def colour_dog(c):
    animal(c, "dog", CX, CY, 3.3)
    return "Colour the dog. Count the legs together: one, two, three, four.", "2-5"


# ---------------------------------------------------------------- mazes
def build_maze(cols, rows):
    """Depth-first maze. Returns the set of removed walls as frozenset cell pairs."""
    seen, open_ = {(0, 0)}, set()
    stack = [(0, 0)]
    while stack:
        x, y = stack[-1]
        nbrs = [(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))
                if 0 <= x + dx < cols and 0 <= y + dy < rows and (x + dx, y + dy) not in seen]
        if not nbrs:
            stack.pop()
            continue
        n = random.choice(nbrs)
        seen.add(n)
        open_.add(frozenset(((x, y), n)))
        stack.append(n)
    return open_


def draw_maze(c, cols, rows, cell, start_animal):
    open_ = build_maze(cols, rows)
    ox, oy = CX - cols * cell / 2, CY + rows * cell / 2     # top-left corner
    pen(c, 5)
    for x in range(cols + 1):
        for y in range(rows):
            if 0 < x < cols and frozenset(((x - 1, y), (x, y))) in open_:
                continue
            c.line(ox + x * cell, oy - y * cell, ox + x * cell, oy - (y + 1) * cell)
    for y in range(rows + 1):
        for x in range(cols):
            if y == 0 and x == 0 or y == rows and x == cols - 1:
                continue                                    # entrance and exit
            if 0 < y < rows and frozenset(((x, y - 1), (x, y))) in open_:
                continue
            c.line(ox + x * cell, oy - y * cell, ox + (x + 1) * cell, oy - y * cell)
    animal(c, start_animal, ox + cell / 2, oy - cell / 2, cell / 190)
    pen(c)
    star(c, ox + (cols - 0.5) * cell, oy - (rows - 0.5) * cell, cell * 0.28)


@page
def maze_small(c):
    draw_maze(c, 4, 5, 115, "cow")
    return "Help the cow walk to the star without touching the walls. Trace with a finger first.", "3-4"


@page
def count_eight(c):
    counting(c, "hen", 8, 3)
    return "Count the hens in rows, pointing at each. Then trace the big outlined 8.", "3-5"


@page
def pattern(c):
    rows = [["cow", "pig", "cow", "pig", "cow"], ["cow", "cow", "pig", "cow", "cow"],
            ["cow", "pig", "duck", "cow", "pig"]]
    for r, row in enumerate(rows):
        y = TOP - 100 - r * 220
        for k, name in enumerate(row):
            animal(c, name, LEFT + 40 + k * 82, y, 0.52)
        dashed(c)
        c.rect(LEFT + 40 + 5 * 82 - 36, y - 36, 72, 72, stroke=1, fill=0)
    return "What comes next in the pattern? Say it aloud together, then draw it in the dashed box.", "4-5"


@page
def dots_five(c):
    pts = [(CX - 150, CY - 120), (CX + 150, CY - 120), (CX + 150, CY + 60), (CX, CY + 190),
           (CX - 150, CY + 60)]
    return dot_to_dot(c, pts, "Join the dots 1 to 5, then back to 1, to build a house for the pig.", "3-5")


@page
def size_smallest(c):
    for name, s, y in (("pig", 1.5, TOP - 90), ("pig", 0.9, CY - 20), ("pig", 1.9, BOTTOM + 95)):
        animal(c, name, CX, y, s)
    return "Ask your child to circle the SMALLEST pig. Notice the middle-sized one too.", "2-4"


@page
def maze_large(c):
    draw_maze(c, 6, 7, 80, "pig")
    return "Help the pig find its way to the star. A pencil or crayon is good for this one.", "4-5"


@page
def dots_ten(c):
    base = [(-200, -170), (-200, 40), (-100, 115), (0, 190), (100, 115), (200, 40),
            (200, -170), (100, -170), (0, -170), (-100, -170)]
    pts = [(CX + x, CY + y) for x, y in base]
    return dot_to_dot(c, pts, "Join the dots 1 to 10, then back to 1, to build the barn.", "4-5", below={8, 9, 10})


def dot_to_dot(c, pts, note, ages, below=()):
    mx = sum(p[0] for p in pts) / len(pts)
    my = sum(p[1] for p in pts) / len(pts)
    pen(c)
    for i, (x, y) in enumerate(pts, 1):
        dot(c, x, y, 9)
        d = math.hypot(x - mx, y - my) or 1
        lx, ly = x + (x - mx) / d * 32, y + (y - my) / d * 32 - 10
        if i in below:
            lx, ly = x, y - 42
        c.setFont("Helvetica-Bold", 30)
        c.drawCentredString(lx, ly, str(i))
    return note, ages


@page
def certificate(c):
    pen(c, 6)
    c.roundRect(LEFT + 10, BOTTOM + 20, RIGHT - LEFT - 20, TOP - BOTTOM - 20, 30, stroke=1, fill=0)
    star(c, CX, TOP - 190, 110)
    c.setFont("Helvetica-Bold", 52)
    c.drawCentredString(CX, CY - 40, "Well done!")
    c.setLineWidth(3)
    c.line(CX - 170, CY - 120, CX + 170, CY - 120)
    for name, x in (("cow", CX - 150), ("sheep", CX), ("cat", CX + 150)):
        animal(c, name, x, BOTTOM + 120, 1.0)
    return "Write your child's name on the line and the date below, then stick it on the fridge.", "all"


def main():
    c = canvas.Canvas(OUT, pagesize=A4, invariant=1)
    c.setTitle("On the Farm: an activity book for ages 1-5")
    c.setAuthor("daily-projects")
    for n, fn in enumerate(PAGES, 1):
        pen(c)
        note, ages = fn(c)
        footer(c, n, note, ages)
        c.showPage()
    c.save()
    print(f"wrote {OUT} with {len(PAGES)} pages")


if __name__ == "__main__":
    main()
