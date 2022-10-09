"""
Python program that generates the testing files for the specific file version.
"""

# IMPORTS
import re

# CONSTANTS
ORIGINAL_AUDT_FILE = "test-AUDTFile0x00080001Test.audt"  # Original file to be edited
TESTING_FILES_FOLDER = "../src/main/resources/site/overwrite/auditranscribe/testing-files/audt-test-files"

NUM_SECTIONS = 5

# READ FILE DATA
with open(ORIGINAL_AUDT_FILE, "rb") as f:
    fileData = f.read()

# GET AUDT FILE VERSION
fileVersion = "v" + re.match("test-AUDTFile(0x[0-9]{8})Test.audt", ORIGINAL_AUDT_FILE).group(1)
print(f"Generating test files for version '{fileVersion}'.")
input("Press any key to start.")

# MAIN CODE
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

    with open(f"{TESTING_FILES_FOLDER}/{fileVersion}/section{sectionID}-id-incorrect.audt", "wb") as f:
        f.write(idIncorrectBytes)

    # Replace the EOS for one of the files
    idIncorrectBytes = bytearray(fileData)

    for i in range(eosBytePos, eosBytePos + 4):
        idIncorrectBytes[i] = 255  # Byte FF

    with open(f"{TESTING_FILES_FOLDER}/{fileVersion}/section{sectionID}-eos-incorrect.audt", "wb") as f:
        f.write(idIncorrectBytes)

    print(f"Section {sectionID} files written")

print("Done")