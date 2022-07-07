"""
Program that prints the different hex values of the colour scales.
"""

# IMPORTS
import plotly.express as px

# CONSTANTS
COLOUR_SCALE_NAME = "Inferno"
COLOUR_SCALE = px.colors.sequential.Inferno

# MAIN CODE
# Arrange CSS hex values for the colour scale in DESCENDING order of lightness
# (Currently they are arranged in ASCENDING order of lightness)
hexVals = COLOUR_SCALE[::-1]

# Replace the octothorpe (#) with "0x"
hexVals = [val.replace("#", "0x") for val in hexVals]

# Nicely print the colour scale values for Java processing
print(f"""{COLOUR_SCALE_NAME.upper()}(new int[] {{
        {', '.join(hexVals)}
}}, \"{COLOUR_SCALE_NAME.title()}\")""")
