"""
SVG To PNG.py

Description: Python file that converts SVG files into PNG files.

This program is free software: you can redistribute it and/or modify it under the terms of the
GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
Licence, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public Licence for more details.

You should have received a copy of the GNU General Public Licence along with this program. If not,
see <https://www.gnu.org/licenses/>

Copyright © AudiTranscribe Team
"""

# IMPORTS
import glob
import os
import shutil

import numpy as np
import cairosvg
from tqdm import tqdm
from PIL import Image

# CONSTANTS
THEME_COLOURS = {
    "light-mode": "#000000",
    "dark-mode": "#bbbbbb",
    "high-contrast": "#ffffff"
}
OUTPUT_SIZE = 500

# MAIN CODE
# Create temporary directory
os.makedirs("temp", exist_ok=True)

# Convert all SVG files into PNG files
for svgPath in tqdm(glob.glob("svg/*.svg"), desc="Processing SVG Files"):
    filename = os.path.basename(svgPath)
    cairosvg.svg2png(
        url=svgPath, write_to="temp/" + filename[:-4] + ".png", output_width=OUTPUT_SIZE, output_height=OUTPUT_SIZE
    )

# Iterate through all the generated PNG files
for theme, colour in tqdm(THEME_COLOURS.items(), desc="Processing Theme PNG Files"):
    # Create folder if not exist
    try:
        os.mkdir(os.path.join("PNGs", theme))
    except FileExistsError:
        pass

    # Iterate through all PNG files
    for pngPath in glob.glob("temp/*.png"):
        # Open the PNG image as RGBA
        im = Image.open(pngPath)
        im = im.convert("RGBA")

        # Convert the image data into numpy arrays
        data = np.array(im)
        red, green, blue, alpha = data.T  # Temporarily unpack the bands for readability

        # Replace black with white (leaves alpha values alone)
        blackAreas = (red == 0) & (blue == 0) & (green == 0)
        data[..., :-1][blackAreas.T] = tuple(int(colour.lstrip("#")[i:i + 2], 16) for i in (0, 2, 4))

        # Save the image data back to the PNG file
        im = Image.fromarray(data)
        im.save(pngPath)

        # Copy the PNG file to the "PNGs" directory
        subPath = theme
        shutil.copy(pngPath, os.path.join("PNGs", subPath, os.path.basename(pngPath)))

# Delete files in temporary directory
for pngPath in glob.glob("temp/*.png"):
    os.remove(pngPath)

os.rmdir("temp")

# Report completion
print("All done!")
