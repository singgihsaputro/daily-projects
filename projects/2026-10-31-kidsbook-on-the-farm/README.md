# On the Farm: printable farm animal activity book (ages 1–5)

A 27-page, ink-friendly A4 activity book of farm animal tracing, colouring, counting, matching, mazes and dot-to-dot pages, drawn entirely by `generate.py`.

![Cover](preview/page-01.png) ![Colour the cow](preview/page-04.png) ![Counting to 3](preview/page-09.png) ![Odd one out](preview/page-18.png)

## How to run

The finished book is already in this folder: print `book.pdf` on A4, portrait, at 100% scale.

To regenerate it from a clean checkout (needs Python 3):

```bash
cd projects/2026-10-31-kidsbook-on-the-farm
python3 -m venv .venv && . .venv/bin/activate
pip install reportlab pymupdf
python3 generate.py                      # writes book.pdf (27 pages)
python3 render_previews.py 1 4 9 18      # optional: writes preview/page-NN.png
```

Rerunning `generate.py` gives a byte-identical `book.pdf` (fixed random seed and invariant PDF mode).

## What's inside

Pages run roughly easiest to hardest. Each carries a small note at the foot for the parent, including which ages it suits.

| Pages | Content | Ages |
|---|---|---|
| 1 | Cover | all |
| 2–3 | Trace straight paths across and down to the barn dot | 1–2 |
| 4, 5, 8, 10, 13, 16, 19 | Colour a cow, pig, duck, sheep, cat, hen, dog | 1–5 |
| 6, 12 | Trace hilly and bumpy paths | 2–4 |
| 7, 24 | Size: circle the biggest hen / the smallest pig | 2–4 |
| 9, 14, 21 | Counting 3, 5 and 8, with an outlined numeral to trace | 2–5 |
| 11, 15 | Trace circles; trace square, triangle, diamond, rectangle | 2–5 |
| 17 | Matching pairs | 3–4 |
| 18 | Odd one out | 3–5 |
| 20, 25 | Mazes with wide corridors (small, then larger) | 3–5 |
| 22 | Pattern completion | 4–5 |
| 23, 26 | Dot-to-dot 1–5 (a house) and 1–10 (a barn) | 3–5 |
| 27 | "Well done" certificate | all |

Design rules followed: black line art on white with no fills, strokes of at least 4pt, 15mm safe margin, no reading needed (text is only for the parent), and only generic farm animals with no licensed characters.

## Stack

Python 3, [ReportLab](https://pypi.org/project/reportlab/) for vector PDF output, and PyMuPDF only to render the preview PNGs.

## Limitations

- Verified by generating the PDF, confirming it has 27 pages (counted with PyMuPDF), rendering every page and checking a contact sheet by eye, and checking that two runs give identical bytes. It has not been printed on a real home printer or tried with a child.
- Animals are simple geometric outlines, so they are stylised. Some are smaller than 4cm where the exercise needs it (the size-choice, maze and pattern pages, and the small animals beside the trace lines).
- Maze difficulty is set by grid size, not tuned with children.
- The `mock/*.json` fixtures the playbook mentions do not apply: this book has no network-shaped data.
