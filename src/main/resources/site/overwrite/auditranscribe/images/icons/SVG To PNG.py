"""
SVG To PNG.py

Created on 2022-04-23
Updated on 2022-05-29

Description: Python file that converts SVG files into PNG files.
"""

# IMPORTS
import glob
import os

import numpy as np
import cairosvg
from PIL import Image

# CONSTANTS
FOR_DARK_MODE = True  # Whether the PNG files are meant for use in the "Dark Mode" theme
DARK_MODE_COLOUR = "#bbbbbb"  # The colour of the "Dark Mode" theme; without alpha value

# MAIN CODE
# Create temporary directory
os.makedirs("temp", exist_ok=True)

# Convert all SVG files into PNG files
for svgPath in glob.glob("SVGs/*.svg"):
    filename = os.path.basename(svgPath)
    cairosvg.svg2png(url=svgPath, write_to="temp/" + filename[:-4] + ".png")

# Iterate through all the generated PNG files
for pngPath in glob.glob("temp/*.png"):
    # Check if we need to do further processing for dark mode
    if FOR_DARK_MODE:
        # Open the PNG image as RGBA
        im = Image.open(pngPath)
        im = im.convert("RGBA")

        # Convert the image data into numpy arrays
        data = np.array(im)
        red, green, blue, alpha = data.T  # Temporarily unpack the bands for readability

        # Replace black with white (leaves alpha values alone)
        blackAreas = (red == 0) & (blue == 0) & (green == 0)
        data[..., :-1][blackAreas.T] = tuple(int(DARK_MODE_COLOUR.lstrip("#")[i:i+2], 16) for i in (0, 2, 4))

        # Save the image data back to the PNG file
        im = Image.fromarray(data)
        im.save(pngPath)

    # Move the PNG file to the "PNGs" directory
    os.rename(pngPath, "PNGs/" + os.path.basename(pngPath))

# Delete the temporary directory
os.rmdir("temp")

# Report completion
print("All done!")
