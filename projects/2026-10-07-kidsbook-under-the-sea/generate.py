#!/usr/bin/env python3
"""Builds book.pdf: "Under the Sea", a 27-page activity book for ages 1-5.

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
CX = W / 2
LW = 4                              # minimum stroke, in points
OUT = "book.pdf"

# natural width of each creature in its own drawing units
NATURAL = {"fish": 104, "whale": 112, "star": 100, "crab": 140,
           "shell": 80, "octopus": 72, "turtle": 116, "boat": 100}


# ---------------------------------------------------------------- primitives
def pen(c, width=LW):
    c.setLineWidth(width)
    c.setLineCap(1)
    c.setLineJoin(1)
    c.setStrokeGray(0)
    c.setFillGray(0)


@contextmanager
def placed(c, x, y, width, kind, flip=False):
    """Move the origin to (x, y) and scale so `kind` is `width` pt wide."""
    k = width / NATURAL[kind]
    c.saveState()
    c.translate(x, y)
    c.scale(-k if flip else k, k)
    pen(c, LW / k)
    yield
    c.restoreState()


def dot(c, x, y, r):
    c.circle(x, y, r, stroke=0, fill=1)


def outline(c, p):
    c.drawPath(p, stroke=1, fill=0)


def path(c, pts, close=False):
    p = c.beginPath()
    p.moveTo(*pts[0])
    for q in pts[1:]:
        p.lineTo(*q)
    if close:
        p.close()
    return p


def bezier(c, start, *segs):
    p = c.beginPath()
    p.moveTo(*start)
    for s in segs:
        p.curveTo(*s)
    return p


# ---------------------------------------------------------------- creatures
def draw_fish(c):
    c.ellipse(-50, -28, 34, 28)
    outline(c, path(c, [(32, 9), (54, 28), (54, -28), (32, -9)]))
    outline(c, bezier(c, (-14, 27), (-6, 50, 14, 46, 20, 22)))
    outline(c, bezier(c, (-8, -27), (0, -42, 10, -40, 14, -24)))
    c.arc(-48, -6, -30, 8, 200, 140)
    for sx in (-4, 10):
        outline(c, bezier(c, (sx, 18), (sx - 6, 8, sx - 6, -8, sx, -18)))
    dot(c, -28, 9, 5)


def draw_whale(c):
    outline(c, bezier(c, (-56, -4), (-58, 44, -8, 52, 20, 22),
                      (26, 16, 34, 18, 36, 30), (44, 26, 50, 32, 56, 44),
                      (46, 48, 38, 42, 34, 36), (30, 18, 26, 4, 22, -6),
                      (4, -34, -50, -34, -56, -4)))
    outline(c, bezier(c, (-52, -10), (-34, -20, -12, -20, 4, -12)))
    outline(c, bezier(c, (-14, 8), (-6, 0, 6, 2, 10, 8)))
    dot(c, -34, 12, 5)
    for dx in (-14, 0, 14):
        outline(c, bezier(c, (-20, 46), (-20 + dx, 62, -20 + dx * 2, 60,
                                         -20 + dx * 2.4, 54)))


def draw_star(c):
    pts = []
    for i in range(10):
        r = 50 if i % 2 == 0 else 22
        a = math.pi / 2 + i * math.pi / 5
        pts.append((r * math.cos(a), r * math.sin(a) - 3))
    outline(c, path(c, pts, close=True))
    dot(c, -9, 5, 3.5)
    dot(c, 9, 5, 3.5)
    c.arc(-9, -8, 9, 2, 200, 140)


def draw_crab(c):
    c.ellipse(-42, -32, 42, 22)
    for s in (-1, 1):
        outline(c, path(c, [(s * 36, 6), (s * 52, 26)]))
        c.circle(s * 58, 36, 13)
        outline(c, path(c, [(s * 58, 36), (s * 58 + s * 3, 49)]))
        outline(c, path(c, [(s * 14, 22), (s * 14, 34)]))
        c.circle(s * 14, 40, 7)
        for i in range(3):
            y = -6 - i * 9
            outline(c, path(c, [(s * 40, y), (s * 58, y - 8), (s * 64, y - 22)]))
        dot(c, s * 14, 40, 3)
    c.arc(-14, -14, 14, 4, 205, 130)


def draw_shell(c):
    c.arc(-40, -40, 40, 40, 0, 180)
    outline(c, path(c, [(-40, 0), (40, 0)]))
    outline(c, path(c, [(-16, -12), (16, -12), (12, 0), (-12, 0)], close=True))
    for deg in (150, 120, 90, 60, 30):
        a = math.radians(deg)
        outline(c, path(c, [(0, 0), (38 * math.cos(a), 38 * math.sin(a))]))


def draw_octopus(c):
    c.ellipse(-34, -26, 34, 52)
    for x0, sway in ((-26, -1), (-9, 1), (9, -1), (26, 1)):
        outline(c, bezier(c, (x0, -20), (x0 + 12 * sway, -36, x0 - 12 * sway, -48,
                                          x0 + 4 * sway, -62)))
    for x in (-13, 13):
        c.circle(x, 24, 8)
        dot(c, x, 24, 3.5)
    c.arc(-10, 4, 10, 16, 205, 130)


def draw_turtle(c):
    c.translate(-8, 0)
    c.arc(-42, -36, 42, 36, 0, 180)
    outline(c, path(c, [(-42, 0), (42, 0)]))
    outline(c, bezier(c, (-14, 0), (-22, 14, -16, 28, -6, 32)))
    outline(c, bezier(c, (14, 0), (22, 14, 16, 28, 6, 32)))
    outline(c, path(c, [(0, 0), (0, 34)]))
    c.circle(58, 10, 13)
    outline(c, path(c, [(40, 4), (47, 6)]))
    dot(c, 62, 14, 3.5)
    for x in (-30, 22):
        c.roundRect(x, -20, 20, 20, 7)
    outline(c, path(c, [(-42, 0), (-54, -6), (-42, -10)]))


def draw_boat(c):
    outline(c, path(c, [(-50, -14), (50, -14), (36, -38), (-36, -38)], close=True))
    outline(c, path(c, [(0, -14), (0, 60)]))
    outline(c, path(c, [(5, 58), (5, -8), (42, -8)], close=True))
    outline(c, path(c, [(-5, 44), (-5, -8), (-34, -8)], close=True))
    outline(c, path(c, [(0, 60), (16, 54), (0, 48)]))


DRAWERS = {"fish": draw_fish, "whale": draw_whale, "star": draw_star,
           "crab": draw_crab, "shell": draw_shell, "octopus": draw_octopus,
           "turtle": draw_turtle, "boat": draw_boat}


def creature(c, kind, x, y, width, flip=False):
    with placed(c, x, y, width, kind, flip):
        DRAWERS[kind](c)


def bubbles(c, spots):
    pen(c)
    for x, y, r in spots:
        c.circle(x, y, r)


def wave_line(c, y, x0=LEFT, x1=RIGHT, amp=14, period=70):
    pen(c)
    p = c.beginPath()
    p.moveTo(x0, y)
    x = x0
    while x + period <= x1 + 0.1:
        p.curveTo(x + period * .25, y + amp * 2, x + period * .75, y - amp * 2,
                  x + period, y)
        x += period
    outline(c, p)


# ---------------------------------------------------------------- page chrome
class Book:
    def __init__(self):
        self.c = canvas.Canvas(OUT, pagesize=A4, invariant=1)
        self.c.setTitle("Under the Sea - activity book for ages 1-5")
        self.c.setAuthor("daily-projects")
        self.n = 0

    def page(self, ages, note):
        if self.n:
            self.c.showPage()
        self.n += 1
        c = self.c
        pen(c)
        c.setFont("Helvetica", 8.5)
        c.drawString(LEFT, 45, f"Suits ages {ages}.  {note}")
        c.drawRightString(RIGHT, 45, str(self.n))
        return c

    def save(self):
        self.c.save()


def numeral(c, text, x, y, size):
    """Outlined digits: cheap on ink, and a child can colour or trace them."""
    t = c.beginText(x, y)
    t.setFont("Helvetica-Bold", size)
    t.setTextRenderMode(1)
    c.setLineWidth(3)
    t.textOut(text)
    c.drawText(t)
    pen(c)


def dashed(c, p):
    c.setDash(12, 11)
    c.setLineWidth(5)
    outline(c, p)
    c.setDash()
    pen(c)


# ---------------------------------------------------------------- page types
def cover(b):
    c = b.page("1-5", "A printable activity book. Print on A4, portrait, at 100% scale.")
    creature(c, "fish", CX, 500, 340)
    creature(c, "whale", 170, 250, 200, flip=True)
    creature(c, "turtle", 430, 250, 190)
    bubbles(c, [(110, 640, 30), (150, 700, 20), (480, 660, 34), (440, 730, 18)])
    c.setFont("Helvetica-Bold", 44)
    c.drawCentredString(CX, 150, "Under the Sea")
    c.setFont("Helvetica", 16)
    c.drawCentredString(CX, 118, "A crayon activity book for ages 1 to 5")
    wave_line(c, 85, amp=8)


def trace_lines(b):
    c = b.page("1-2", "Trace each line with a finger first, then a fat crayon. No wrong answers.")
    for y in (700, 560, 420, 280, 140 + 20):
        creature(c, "fish", 110, y, 100)
        dashed(c, path(c, [(190, y), (450, y)]))
        pen(c)
        c.circle(500, y, 28)


def trace_down(b):
    c = b.page("1-2", "Bubbles float up, fish swim down: trace from the bubble to the fish.")
    for x in (115, 240, 365, 490):
        pen(c)
        c.circle(x, 730, 36)
        dashed(c, path(c, [(x, 685), (x, 235)]))
        creature(c, "fish", x, 160, 90)


def colour_page(b, ages, note, kind, width, extra=()):
    c = b.page(ages, note)
    creature(c, kind, CX, 440, width)
    bubbles(c, extra)
    wave_line(c, 120)
    return c


def size_big(b):
    c = b.page("2-3", "Ask: which one is the big one? Circle it. Swap the words for little and small.")
    rows = [("fish", 680, False), ("star", 440, True), ("shell", 200, False)]
    for kind, y, big_right in rows:
        big, small = (210, 70)
        xb, xs = (400, 120) if big_right else (190, 470)
        creature(c, kind, xb, y, big)
        creature(c, kind, xs, y, small)


def trace_waves(b):
    c = b.page("2-3", "Trace the waves from left to right with a crayon.")
    for y, amp, period in ((690, 22, 130), (540, 34, 130), (390, 24, 100), (240, 40, 130)):
        p = c.beginPath()
        p.moveTo(LEFT + 20, y)
        x = LEFT + 20
        while x + period <= RIGHT - 10:
            p.curveTo(x + period * .25, y + amp * 2, x + period * .75, y - amp * 2,
                      x + period, y)
            x += period
        dashed(c, p)
        dot(c, LEFT + 20, y, 11)


def count_rows(b):
    c = b.page("2-3", "Count the objects together and say the number out loud.")
    for n, kind, y in ((1, "fish", 650), (2, "star", 440), (3, "shell", 230)):
        numeral(c, str(n), 70, y - 35, 100)
        for i in range(n):
            creature(c, kind, 235 + i * 125, y, 100)


def trace_shapes(b):
    c = b.page("3-4", "Trace each shape, starting at the dot. Name it: circle, square, triangle.")
    p = c.beginPath()
    p.circle(CX, 650, 85)
    dashed(c, p)
    dot(c, CX, 735, 11)
    dashed(c, path(c, [(CX - 80, 490), (CX + 80, 490), (CX + 80, 330),
                       (CX - 80, 330)], close=True))
    dot(c, CX - 80, 490, 11)
    dashed(c, path(c, [(CX, 270), (CX + 90, 110), (CX - 90, 110)], close=True))
    dot(c, CX, 270, 11)


def find_circles(b):
    c = b.page("3-4", "Colour in all the circles. Count how many you found.")
    kinds = "csticcstcsts"
    for i, k in enumerate(kinds):
        x = 130 + (i % 3) * 170
        y = 690 - (i // 3) * 170
        p = c.beginPath()
        if k == "c":
            p.circle(x, y, 60)
        elif k == "s":
            p.rect(x - 56, y - 56, 112, 112)
        else:
            p.moveTo(x, y + 62)
            p.lineTo(x + 66, y - 52)
            p.lineTo(x - 66, y - 52)
            p.close()
        outline(c, p)


def draw_maze(c, cols, rows, cell, x0, y_top, seed, start, goal):
    rnd = random.Random(seed)
    walls = {(i, j): {"N", "E", "S", "W"} for i in range(cols) for j in range(rows)}
    opposite = {"N": "S", "S": "N", "E": "W", "W": "E"}
    step = {"N": (0, -1), "S": (0, 1), "E": (1, 0), "W": (-1, 0)}
    seen, stack = {(0, 0)}, [(0, 0)]
    while stack:
        i, j = stack[-1]
        options = []
        for d, (di, dj) in step.items():
            n = (i + di, j + dj)
            if n in walls and n not in seen:
                options.append((d, n))
        if not options:
            stack.pop()
            continue
        d, n = rnd.choice(options)
        walls[(i, j)].discard(d)
        walls[n].discard(opposite[d])
        seen.add(n)
        stack.append(n)
    walls[(0, 0)].discard("W")
    walls[(cols - 1, rows - 1)].discard("E")
    pen(c)
    for (i, j), ws in walls.items():
        x, y = x0 + i * cell, y_top - j * cell
        if "N" in ws:
            c.line(x, y, x + cell, y)
        if "S" in ws:
            c.line(x, y - cell, x + cell, y - cell)
        if "W" in ws:
            c.line(x, y, x, y - cell)
        if "E" in ws:
            c.line(x + cell, y, x + cell, y - cell)
    mid = cell / 2
    creature(c, start, x0 + mid, y_top - mid, cell * .6)
    creature(c, goal, x0 + (cols - 1) * cell + mid, y_top - (rows - 1) * cell - mid,
             cell * .55)


def easy_maze(b):
    c = b.page("3-4", "Help the fish find the shell. Trace the way with a finger, then a crayon.")
    draw_maze(c, 4, 5, 125, CX - 250, 780, 7, "fish", "shell")


def matching(b):
    c = b.page("3-4", "Draw a line from each creature to its twin on the other side.")
    kinds = ["fish", "crab", "star", "shell"]
    order = [2, 0, 3, 1]
    for row, k in enumerate(kinds):
        y = 690 - row * 165
        creature(c, k, 130, y, 105)
        creature(c, kinds[order[row]], 465, y, 105)
        dot(c, 225, y, 9)
        dot(c, 370, y, 9)


def count_five(b):
    c = b.page("3-4", "Count the starfish. Trace the big number with a finger.")
    numeral(c, "5", CX - 52, 640, 190)
    for i in range(5):
        x = 140 + (i % 3) * 160 if i < 3 else 220 + (i - 3) * 160
        y = 400 if i < 3 else 230
        creature(c, "star", x, y, 125)


def odd_one_out(b):
    c = b.page("3-5", "Which one is different? Circle it.")
    rows = [("fish", "crab", 2), ("star", "shell", 0), ("crab", "star", 3),
            ("shell", "fish", 1)]
    for r, (same, odd, pos) in enumerate(rows):
        y = 690 - r * 170
        for i in range(4):
            creature(c, odd if i == pos else same, 105 + i * 128, y, 100)


def trace_zigzag(b):
    c = b.page("3-5", "Trace the zigzags, loops and big waves. Take it slowly.")
    y = 690
    p = c.beginPath()
    p.moveTo(LEFT + 20, y)
    x = LEFT + 20
    up = True
    while x < RIGHT - 40:
        x += 45
        p.lineTo(x, y + (45 if up else 0))
        up = not up
    dashed(c, p)
    dot(c, LEFT + 20, y, 11)
    y = 520
    p = c.beginPath()
    steps, loops, r = 200, 4, 36
    for i in range(steps + 1):
        t = i / steps * loops * 2 * math.pi
        px = LEFT + 40 + t * 17 - r * math.sin(t)
        py = y + r - r * math.cos(t)
        if i == 0:
            p.moveTo(px, py)
        else:
            p.lineTo(px, py)
    dashed(c, p)
    dot(c, LEFT + 40, y, 11)
    y = 330
    p = c.beginPath()
    p.moveTo(LEFT + 20, y)
    x = LEFT + 20
    while x + 150 <= RIGHT - 10:
        p.curveTo(x + 38, y + 120, x + 112, y + 120, x + 150, y)
        x += 150
    dashed(c, p)
    dot(c, LEFT + 20, y, 11)
    y = 150
    p = c.beginPath()
    p.moveTo(LEFT + 20, y)
    p.curveTo(200, y + 140, 400, y - 120, RIGHT - 20, y + 20)
    dashed(c, p)
    dot(c, LEFT + 20, y, 11)


def dots(c, pts, label_offset=34):
    pen(c)
    mx = sum(p[0] for p in pts) / len(pts)
    my = sum(p[1] for p in pts) / len(pts)
    c.setFont("Helvetica-Bold", 30)
    for i, (x, y) in enumerate(pts, 1):
        dot(c, x, y, 10)
        d = math.hypot(x - mx, y - my) or 1
        lx = x + (x - mx) / d * label_offset
        ly = y + (y - my) / d * label_offset - 10
        c.drawCentredString(lx, ly, str(i))


def dot_five(b):
    c = b.page("3-5", "Join the dots in order, 1 to 5, then back to 1. What shape appeared?")
    dots(c, [(CX, 700), (480, 470), (410, 250), (185, 250), (115, 470)])
    bubbles(c, [(100, 150, 26), (160, 120, 16)])


def pattern(b):
    c = b.page("4-5", "What comes next? Draw it in the empty box.")
    xs = [92 + i * 100 for i in range(5)]
    rows = [(650, [("fish", 90), ("shell", 90), ("fish", 90), ("shell", 90)], "fish"),
            (430, [("fish", 90), ("star", 90), ("shell", 90), ("fish", 90)], "star"),
            (210, [("fish", 120), ("fish", 60), ("fish", 120), ("fish", 60)], "fish")]
    for y, items, _ in rows:
        for x, (k, s) in zip(xs, items):
            creature(c, k, x, y, s)
        c.setDash(8, 8)
        c.roundRect(xs[4] - 48, y - 48, 96, 96, 14)
        c.setDash()


def size_order(b):
    c = b.page("4-5", "Circle the smallest one in every row. Then find the biggest.")
    rows = [("crab", 640, (110, 190, 70)), ("fish", 430, (60, 120, 190)),
            ("shell", 220, (180, 100, 60))]
    for kind, y, sizes in rows:
        x = LEFT + 8
        for s in sizes:
            creature(c, kind, x + s / 2, y, s)
            x += s + 30


def count_eight(b):
    c = b.page("4-5", "Count the shells together. Is it more or fewer than five?")
    numeral(c, "8", CX - 55, 640, 190)
    for i in range(8):
        creature(c, "shell", 112 + (i % 4) * 125, 440 if i < 4 else 250, 105)


def dot_ten(b):
    c = b.page("4-5", "Join the dots from 1 to 10, then back to 1. Add an eye and some bubbles!")
    dots(c, [(110, 440), (190, 540), (300, 590), (400, 540), (500, 610),
             (500, 270), (400, 340), (300, 290), (190, 340), (125, 385)])
    bubbles(c, [(150, 700, 24), (210, 740, 14), (460, 150, 28)])


def hard_maze(b):
    c = b.page("4-5", "A longer way home for the fish. Try it with a finger first.")
    draw_maze(c, 6, 8, 82, CX - 246, 780, 21, "fish", "shell")


def certificate(b):
    c = b.page("1-5", "Print this page and add the date.")
    creature(c, "star", CX, 480, 300)
    c.setFont("Helvetica-Bold", 40)
    c.drawCentredString(CX, 700, "Well done!")
    c.setFont("Helvetica", 16)
    c.drawCentredString(CX, 300, "Name")
    c.line(CX - 140, 280, CX + 140, 280)
    bubbles(c, [(110, 640, 30), (480, 600, 36), (450, 720, 16), (150, 720, 18)])


def main():
    b = Book()
    cover(b)
    trace_lines(b)
    trace_down(b)
    colour_page(b, "1-2", "Colour the big fish any colours you like. Scribbles are perfect.",
                "fish", 440, [(110, 700, 30), (480, 720, 22), (470, 200, 26)])
    colour_page(b, "1-3", "Colour the whale. Big crayon strokes are fine.", "whale", 460,
                [(110, 190, 26), (500, 700, 28)])
    size_big(b)
    trace_waves(b)
    colour_page(b, "2-3", "Colour the starfish.", "star", 400,
                [(120, 690, 28), (470, 200, 20)])
    count_rows(b)
    colour_page(b, "2-4", "Colour the crab.", "crab", 450, [(110, 700, 26), (490, 680, 18)])
    trace_shapes(b)
    find_circles(b)
    colour_page(b, "3-5", "Colour the octopus. How many arms does it have?", "octopus", 330,
                [(120, 700, 28), (480, 700, 20), (460, 160, 24)])
    easy_maze(b)
    matching(b)
    count_five(b)
    colour_page(b, "3-5", "Colour the turtle. Can you find the shell pattern?", "turtle", 440,
                [(120, 680, 24), (480, 200, 28)])
    odd_one_out(b)
    trace_zigzag(b)
    dot_five(b)
    pattern(b)
    colour_page(b, "4-5", "Colour the sailing boat. Add some waves and a sun if you like.",
                "boat", 400, [(480, 700, 40), (110, 720, 22)])
    size_order(b)
    count_eight(b)
    dot_ten(b)
    hard_maze(b)
    certificate(b)
    b.save()
    print(f"wrote {OUT}: {b.n} pages")


if __name__ == "__main__":
    main()
