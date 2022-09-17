/*
 * WebContentHandlerTest.java
 * Description: Test `WebContentHandler.java`.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package site.overwrite.auditranscribe.network;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class WebContentHandlerTest {
    @Test
    void readContentOnWebpage() throws IOException {
        // This has no encoding attached to it
        assertEquals(
                "This is a file with some text hosted online.",
                WebContentHandler.readContentOnWebpage(new URL("https://pastebin.com/raw/PqNK5dvw"))
        );

        // This URL will raise a 404 error
        assertThrows(
                IOException.class,
                () -> WebContentHandler.readContentOnWebpage(new URL("https://example.com/12345"))
        );
    }
}