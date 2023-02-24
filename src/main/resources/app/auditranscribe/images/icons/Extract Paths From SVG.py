"""
Extract Paths From SVG.py

Description: Python file that converts SVG files into a single JSON file that contains the SVG
             paths.

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
import re
import json

# MAIN CODE
# Read SVG files' contents
svgContents = {}

for svgPath in sorted(glob.glob("svg/*.svg")):
    filename = os.path.basename(svgPath)
    name, ext = os.path.splitext(filename)

    with open(svgPath, "r") as f:
        svgContents[name] = f.read()

# Get the path data
svgPathData = {}

for name, fileContents in svgContents.items():
    svgPathData[name] = re.search(r"d=\"(.+)\"", fileContents).group(1)

# Save the paths to a JSON file
with open("icons.json", "w") as f:
    json.dump({"svgPaths": svgPathData}, f, indent=2)
