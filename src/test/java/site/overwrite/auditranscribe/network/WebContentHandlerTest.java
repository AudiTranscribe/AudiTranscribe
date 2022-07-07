/*
 * WebContentHandlerTest.java
 *
 * Created on 2022-07-07
 * Updated on 2022-07-07
 *
 * Description: Test `WebContentHandler.java`.
 */

package site.overwrite.auditranscribe.network;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class WebContentHandlerTest {
    @Test
    void readContentOnWebpage() throws IOException {
        assertEquals(
                "This is a file with some text hosted online.",
                WebContentHandler.readContentOnWebpage(new URL("https://pastebin.com/raw/PqNK5dvw"))
        );
        assertThrows(
                FileNotFoundException.class,
                () -> WebContentHandler.readContentOnWebpage(new URL("https://example.com/12345"))
        );
    }
}