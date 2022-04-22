import glob
import os

import cairosvg

for svgpath in glob.glob("SVGs/*.svg"):
    filename = os.path.basename(svgpath)

    cairosvg.svg2png(url=svgpath, write_to="PNGs/" + filename[:-4] + ".png")
