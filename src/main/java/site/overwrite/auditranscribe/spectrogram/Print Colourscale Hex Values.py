"""
Program that prints the different hex values of the colour scales.
"""

# IMPORTS
import re

import plotly.express as px

# CONSTANTS
COLOUR_SCALE_NAME = "Ice"
COLOUR_SCALE = px.colors.sequential.ice

# MAIN CODE
# Arrange CSS hex values for the colour scale in DESCENDING order of lightness
# (Currently they are arranged in ASCENDING order of lightness)
colourScaleVals = COLOUR_SCALE[::-1]

# Check if the value is in RGB instead of hex
for i in range(len(colourScaleVals)):
    if colourScaleVals[i].find("rgb") != -1:
        # Parse the RGB into red, green, blue parts
        matches = re.match("rgb\\((\\d{1,3}), (\\d{1,3}), (\\d{1,3})\\)", colourScaleVals[i])
        rgb = tuple(int(matches.group(i)) for i in range(1, 4))

        # Convert each part to hex
        hexRGB = tuple(f"{val:02x}" for val in rgb)

        # Update the colour scale value
        colourScaleVals[i] = "#" + "".join(hexRGB)

# Replace the octothorpe (#) with "0x"
hexVals = [val.replace("#", "0x") for val in colourScaleVals]

# Nicely print the colour scale values for Java processing
print(f"""{COLOUR_SCALE_NAME.upper()}(new int[] {{
        {', '.join(hexVals)}
}}, \"{COLOUR_SCALE_NAME.title()}\")""")
