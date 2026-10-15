#!/usr/bin/env python3
"""Renders selected pages of book.pdf to preview/*.png (needs `pip install pymupdf`)."""
import os
import sys

import pymupdf as fitz

PAGES = [int(p) for p in sys.argv[1:]] or [1, 4, 14, 20]
os.makedirs("preview", exist_ok=True)
doc = fitz.open("book.pdf")
for n in PAGES:
    doc[n - 1].get_pixmap(dpi=70).save(f"preview/page-{n:02d}.png")
    print(f"preview/page-{n:02d}.png")
