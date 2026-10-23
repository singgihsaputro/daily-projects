#!/usr/bin/env python3
"""Builds book.pdf: "Things That Go", a 27-page vehicle activity book for ages 1-5.

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
    """Draw in vehicle units at (x, y), scaled by s, with the stroke kept at LW."""
    c.saveState()
    c.translate(x, y)
    c.scale(s, s)
    pen(c, LW / s)
    yield
    c.restoreState()


def wheel(c, x, y, r):
    c.setFillGray(1)
    c.circle(x, y, r, stroke=1, fill=1)
    c.setFillGray(0)
    dot(c, x, y, r * 0.25)


def star(c, x, y, r):
    pts = []
    for i in range(10):
        a = math.pi / 2 + i * math.pi / 5
        rr = r if i % 2 == 0 else r * 0.45
        pts.append((x + rr * math.cos(a), y + rr * math.sin(a)))
    poly(c, pts)


# ---------------------------------------------------------------- vehicles
# Each drawer is centred on the origin and about 100 units wide.
def draw_car(c):
    poly(c, [(-50, -10), (-50, 10), (-30, 14), (-18, 34), (22, 34), (36, 14), (50, 10), (50, -10)])
    poly(c, [(-14, 16), (-8, 28), (2, 28), (2, 16)])
    poly(c, [(8, 16), (8, 28), (17, 28), (27, 16)])
    wheel(c, -28, -12, 14)
    wheel(c, 28, -12, 14)


def draw_bus(c):
    c.roundRect(-52, -20, 104, 58, 10, stroke=1, fill=0)
    for x in (-40, -16, 8):
        c.rect(x, 10, 18, 18, stroke=1, fill=0)
    c.rect(32, 4, 14, 24, stroke=1, fill=0)
    wheel(c, -28, -20, 13)
    wheel(c, 28, -20, 13)


def draw_train(c):
    poly(c, [(-52, -15), (-52, 26), (14, 26), (14, 48), (52, 48), (52, -15)])
    c.rect(-42, 26, 16, 20, stroke=1, fill=0)
    c.rect(24, 22, 18, 16, stroke=1, fill=0)
    for x in (-34, -8, 30):
        wheel(c, x, -15, 11)


def draw_truck(c):
    c.rect(-56, -15, 66, 58, stroke=1, fill=0)
    poly(c, [(14, -15), (14, 30), (34, 30), (54, 6), (54, -15)])
    poly(c, [(21, 10), (21, 24), (32, 24), (42, 10)])
    wheel(c, -34, -15, 13)
    wheel(c, 32, -15, 13)


def draw_boat(c):
    poly(c, [(-54, -8), (54, -8), (36, -36), (-36, -36)])
    c.line(-2, -8, -2, 52)
    poly(c, [(4, 50), (4, 0), (38, 0)])
    poly(c, [(-8, 42), (-8, 0), (-34, 0)])


def draw_plane(c):
    c.ellipse(-54, -13, 52, 13, stroke=1, fill=0)
    poly(c, [(-8, 11), (-30, 46), (-14, 46), (14, 11)])
    poly(c, [(-8, -11), (-30, -42), (-14, -42), (14, -11)])
    poly(c, [(-46, 10), (-54, 34), (-38, 34), (-28, 11)])
    for x in (-10, 10, 30):
        dot(c, x, 2, 4.5)


def draw_bike(c):
    wheel(c, -32, -10, 24)
    wheel(c, 32, -10, 24)
    poly(c, [(-32, -10), (-8, -10), (-16, 24), (12, 24), (-8, -10)], close=False)
    poly(c, [(12, 24), (32, -10)], close=False)
    poly(c, [(12, 24), (18, 36), (28, 36)], close=False)
    poly(c, [(-16, 24), (-16, 30)], close=False)
    poly(c, [(-26, 30), (-6, 30)], close=False)


VEHICLES = {"car": draw_car, "bus": draw_bus, "train": draw_train, "truck": draw_truck,
            "boat": draw_boat, "plane": draw_plane, "bike": draw_bike}


def vehicle(c, name, x, y, s=1.0):
    with at(c, x, y, s):
        VEHICLES[name](c)


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
    c.drawCentredString(CX, TOP - 70, "Things That Go")
    c.setFont("Helvetica", 18)
    c.drawCentredString(CX, TOP - 105, "An activity book for ages 1 to 5")
    vehicle(c, "bus", CX, CY - 10, 3.6)
    vehicle(c, "car", CX - 130, BOTTOM + 70, 1.3)
    vehicle(c, "plane", CX + 130, BOTTOM + 80, 1.3)
    return "Print on A4 at 100%. Pages go from easiest to hardest, so start at the front.", "1-5"


# ---------------------------------------------------------------- tracing
@page
def trace_horizontal(c):
    names = ["car", "bus", "train", "truck", "boat"]
    for i, name in enumerate(names):
        y = TOP - 80 - i * 135
        vehicle(c, name, LEFT + 60, y, 0.9)
        dashed(c)
        c.line(LEFT + 130, y, RIGHT - 20, y)
        pen(c)
        dot(c, RIGHT - 8, y, 9)
    return "Trace each road from the vehicle to the dot with a fat crayon. Any wobbly line is a good line.", "1-2"


@page
def trace_vertical(c):
    names = ["bus", "car", "truck", "plane"]
    for i, name in enumerate(names):
        x = LEFT + 65 + i * 128
        vehicle(c, name, x, TOP - 60, 0.9)
        dashed(c)
        c.line(x, TOP - 125, x, BOTTOM + 40)
        pen(c)
        dot(c, x, BOTTOM + 30, 9)
    return "Trace each road from the top down to the dot. Let your child choose which road first.", "1-2"


@page
def colour_car(c):
    vehicle(c, "car", CX, CY, 4.6)
    return "Colour the car with any crayon. Scribbling counts. Name the colours as you go.", "1-3"


@page
def colour_bus(c):
    vehicle(c, "bus", CX, CY, 4.6)
    return "Colour the big bus. Ask how many windows the bus has, and count them together.", "1-3"


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
        vehicle(c, "car", RIGHT - 25, y + 40 * math.sin(440 / 38) + 40, 0.5)
    return "Trace the hilly roads left to right. Say 'up and over' as the crayon climbs.", "2-4"


@page
def size_biggest(c):
    for name, s, y in (("truck", 1.0, TOP - 90), ("truck", 1.8, CY - 20), ("truck", 1.4, BOTTOM + 90)):
        vehicle(c, name, CX, y, s)
    return "Ask your child to circle the BIGGEST truck. The small ones are shown smaller on purpose.", "2-4"


@page
def colour_boat(c):
    vehicle(c, "boat", CX, CY, 4.6)
    return "Colour the boat. Talk about what floats and what sinks.", "1-3"


def counting(c, name, n, cols):
    rows = math.ceil(n / cols)
    pitch_x, pitch_y = 150, 100
    top = TOP - 40
    for i in range(n):
        r, k = divmod(i, cols)
        in_row = min(cols, n - r * cols)
        x = CX + (k - (in_row - 1) / 2) * pitch_x
        vehicle(c, name, x, top - 40 - r * pitch_y, 1.15)
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
    counting(c, "car", 3, 3)
    return "Point to each car and count out loud, then trace the big outlined 3.", "2-4"


@page
def colour_train(c):
    vehicle(c, "train", CX, CY, 4.6)
    return "Colour the train. Ask what sound it makes.", "1-3"


@page
def trace_circles(c):
    for i in range(6):
        r, k = divmod(i, 2)
        x, y = CX + (k - 0.5) * 250, TOP - 110 - r * 235
        dashed(c)
        c.circle(x, y, 80, stroke=1, fill=0)
        pen(c)
        dot(c, x, y + 80, 9)
    return "These are wheels. Trace each circle starting at the dot, going round like a wheel.", "2-4"


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
    return "Trace the bumpy roads from the dot to the star. 'Bump, bump, bump!'", "2-4"


@page
def colour_plane(c):
    vehicle(c, "plane", CX, CY, 4.4)
    return "Colour the aeroplane. Look at the sky together and spot a real one if you can.", "1-3"


@page
def count_five(c):
    counting(c, "bus", 5, 3)
    return "Count the buses together, then trace the big outlined 5.", "2-4"


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
def colour_truck(c):
    vehicle(c, "truck", CX, CY, 4.3)
    return "Colour the truck. What could it carry? Let your child decide.", "2-5"


@page
def matching(c):
    names = ["car", "bus", "train", "plane"]
    right = names[2:] + names[:2]
    random.shuffle(right)
    for i, name in enumerate(names):
        y = TOP - 100 - i * 190
        vehicle(c, name, LEFT + 70, y, 1.0)
        dot(c, LEFT + 150, y, 10)
        vehicle(c, right[i], RIGHT - 70, y, 1.0)
        dot(c, RIGHT - 150, y, 10)
    return "Draw a line from each vehicle on the left to its matching twin on the right.", "3-4"


@page
def odd_one_out(c):
    rows = [["car", "car", "bus", "car"], ["boat", "plane", "boat", "boat"],
            ["train", "train", "train", "truck"], ["bike", "bike", "car", "bike"]]
    for r, row in enumerate(rows):
        y = TOP - 90 - r * 175
        for k, name in enumerate(row):
            vehicle(c, name, LEFT + 60 + k * 130, y, 1.0)
    return "Which one is different? Ask your child to circle it. Four rows, one odd vehicle each.", "3-5"


@page
def colour_bike(c):
    vehicle(c, "bike", CX, CY, 4.6)
    return "Colour the bicycle. Count the wheels: one, two.", "2-5"


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


def draw_maze(c, cols, rows, cell, start_vehicle):
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
    vehicle(c, start_vehicle, ox + cell / 2, oy - cell / 2, cell / 190)
    pen(c)
    star(c, ox + (cols - 0.5) * cell, oy - (rows - 0.5) * cell, cell * 0.28)


@page
def maze_small(c):
    draw_maze(c, 4, 5, 115, "car")
    return "Help the car drive to the star without touching the walls. Trace with a finger first.", "3-4"


@page
def count_eight(c):
    counting(c, "truck", 8, 3)
    return "Count the trucks in rows, pointing at each. Then trace the big outlined 8.", "3-5"


@page
def pattern(c):
    rows = [["car", "bus", "car", "bus", "car"], ["car", "car", "bus", "car", "car"],
            ["car", "bus", "boat", "car", "bus"]]
    for r, row in enumerate(rows):
        y = TOP - 100 - r * 220
        for k, name in enumerate(row):
            vehicle(c, name, LEFT + 40 + k * 82, y, 0.75)
        dashed(c)
        c.rect(LEFT + 40 + 5 * 82 - 36, y - 36, 72, 72, stroke=1, fill=0)
    return "What comes next in the pattern? Say it aloud together, then draw it in the dashed box.", "4-5"


@page
def dots_five(c):
    pts = [(CX - 150, CY - 120), (CX + 150, CY - 120), (CX + 150, CY + 60), (CX, CY + 190),
           (CX - 150, CY + 60)]
    return dot_to_dot(c, pts, "Join the dots 1 to 5, then back to 1, to build a house for the bus.", "3-5")


@page
def size_smallest(c):
    for name, s, y in (("bus", 1.5, TOP - 90), ("bus", 0.9, CY - 20), ("bus", 1.9, BOTTOM + 95)):
        vehicle(c, name, CX, y, s)
    return "Ask your child to circle the SMALLEST bus. Notice the middle-sized one too.", "2-4"


@page
def maze_large(c):
    draw_maze(c, 6, 7, 80, "bus")
    return "Help the bus find its way to the star. A pencil or crayon is good for this one.", "4-5"


@page
def dots_ten(c):
    base = [(-220, -60), (-150, -160), (0, -160), (150, -160), (220, -60),
            (60, -60), (60, 220), (-60, 100), (-100, -60), (-165, -60)]
    pts = [(CX + x, CY + y) for x, y in base]
    return dot_to_dot(c, pts, "Join the dots 1 to 10, then back to 1, to sail the boat.", "4-5", below={6, 9, 10})


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
    for name, x in (("car", CX - 150), ("train", CX), ("plane", CX + 150)):
        vehicle(c, name, x, BOTTOM + 120, 1.0)
    return "Write your child's name on the line and the date below, then stick it on the fridge.", "all"


def main():
    c = canvas.Canvas(OUT, pagesize=A4, invariant=1)
    c.setTitle("Things That Go: an activity book for ages 1-5")
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
