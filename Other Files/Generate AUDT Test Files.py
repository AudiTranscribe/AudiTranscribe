"""
Generate AUDT Test Files.py

Description: Python program that generates the testing files for the specific file version.

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
import os
import re

# CONSTANTS
VERSION = "0x000B0001"

ORIGINAL_AUDT_FILE = f"test-AUDTFile{VERSION}Test.audt"  # Original file to be edited
TESTING_FILES_FOLDER = "../src/main/resources/app/auditranscribe/test-files/io/audt_file/" + \
                       f"v{VERSION}/AUDTFile{VERSION}Test"

NUM_SECTIONS = 5
print(TESTING_FILES_FOLDER)

print(f"Generating test files for version '{VERSION}'.")
input("Press any key to start.")

# MAIN CODE
# Read original file
with open(ORIGINAL_AUDT_FILE, "rb") as f:
    fileData = f.read()

# Make output folder if not exist
if not os.path.exists(TESTING_FILES_FOLDER):
    os.makedirs(TESTING_FILES_FOLDER)
    print("Created testing files folder")

# For each section, modify the ID and the EOS delimiter
sectionIDBytePos = 32  # First 32 bytes are the AUDT file delimiter
for sectionID in range(1, NUM_SECTIONS + 1):
    # Get the current section ID's position
    searchBytes = bytearray(b"\x00\x00\x00")
    searchBytes.append(sectionID)

    sectionIDBytePos = fileData.find(searchBytes, sectionIDBytePos)
    while fileData[sectionIDBytePos - 4:sectionIDBytePos] != b"\xe0\x5e\x05\xe5":
        sectionIDBytePos = fileData.find(searchBytes, sectionIDBytePos + 1)

    # Get the End Of Section (EOS) delimiter position
    if sectionID != NUM_SECTIONS:
        searchBytes = bytearray(b"\x00\x00\x00")
        searchBytes.append(sectionID + 1)

        eosBytePos = fileData.find(searchBytes, sectionIDBytePos)
        while fileData[eosBytePos - 4:eosBytePos] != b"\xe0\x5e\x05\xe5":
            eosBytePos = fileData.find(searchBytes, eosBytePos + 1)

        eosBytePos -= 4  # Go back 4 bytes to reach start of EOS delimiter

    else:
        eosBytePos = len(fileData) - 12

    # Replace the section ID for one of the files
    idIncorrectBytes = bytearray(fileData)
    for i in range(sectionIDBytePos, sectionIDBytePos + 4):
        idIncorrectBytes[i] = 255  # Byte FF

    with open(f"{TESTING_FILES_FOLDER}/section{sectionID}-id-incorrect.audt", "wb") as f:
        f.write(idIncorrectBytes)

    # Replace the EOS for one of the files
    idIncorrectBytes = bytearray(fileData)

    for i in range(eosBytePos, eosBytePos + 4):
        idIncorrectBytes[i] = 255  # Byte FF

    with open(f"{TESTING_FILES_FOLDER}/section{sectionID}-eos-incorrect.audt", "wb") as f:
        f.write(idIncorrectBytes)

    print(f"Section {sectionID} files written")

print("Done")
