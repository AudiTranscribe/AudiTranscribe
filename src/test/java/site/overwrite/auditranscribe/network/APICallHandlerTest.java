/*
 * APICallHandlerTest.java
 *
 * Created on 2022-07-07
 * Updated on 2022-08-27
 *
 * Description: Test `APICallHandler.java`.
 */

package site.overwrite.auditranscribe.network;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import site.overwrite.auditranscribe.exceptions.network.APIServerException;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class APICallHandlerTest {
    @Test
    @Order(1)
    void sendAPIGetRequest1() throws IOException, APIServerException {
        JsonObject returned = APICallHandler.sendAPIGetRequest("test-api-server-get");

        assertEquals("OK", returned.get("status").getAsString());
        assertEquals("Hello World!", returned.get("data1").getAsString());
        assertFalse(returned.get("data2").getAsBoolean());
        assertEquals(12.345, returned.get("data3").getAsDouble(), 1e-5);
    }

    @Test
    @Order(2)
    void sendAPIGetRequest2() throws IOException, APIServerException {
        HashMap<String, String> params = new HashMap<>();
        params.put("is-testing", "True");

        JsonObject returned = APICallHandler.sendAPIGetRequest("test-api-server-get", params, 5000);

        assertEquals("OK", returned.get("status").getAsString());
        assertEquals("Hello World!", returned.get("data1").getAsString());
        assertFalse(returned.get("data2").getAsBoolean());
        assertEquals(12.345, returned.get("data3").getAsDouble(), 1e-5);
        assertEquals(678.9, returned.get("data4").getAsDouble(), 1e-5);
    }

    @Test
    @Order(3)
    void sendInvalidAPIGetRequest() throws IOException, APIServerException {
        JsonObject returned = APICallHandler.sendAPIGetRequest("test-api-server-post");
        assertEquals("METHOD NOT ALLOWED", returned.get("status").getAsString());
    }

    @Test
    @Order(4)
    void timeoutAPIGetRequest() {
        assertThrows(
                APIServerException.class,
                () -> APICallHandler.sendAPIGetRequest("test-api-server-get", null, 1)
        );
    }

    @Test
    @Order(1)
    void sendAPIPostRequest1() throws IOException, APIServerException {
        JsonObject returned = APICallHandler.sendAPIPostRequest("test-api-server-post", null);

        assertEquals("OK", returned.get("status").getAsString());
        assertEquals("Eggs and spam", returned.get("data1").getAsString());
        assertFalse(returned.get("data2").getAsBoolean());
        assertEquals(12.345, returned.get("data3").getAsDouble(), 1e-5);
    }

    @Test
    @Order(2)
    void sendAPIPostRequest2() throws IOException, APIServerException {
        HashMap<String, String> params = new HashMap<>();
        params.put("is-testing", "True");

        JsonObject returned = APICallHandler.sendAPIPostRequest("test-api-server-post", params);

        assertEquals("OK", returned.get("status").getAsString());
        assertEquals("Eggs and spam", returned.get("data1").getAsString());
        assertFalse(returned.get("data2").getAsBoolean());
        assertEquals(12.345, returned.get("data3").getAsDouble(), 1e-5);
        assertEquals(678.9, returned.get("data4").getAsDouble(), 1e-5);
    }

    @Test
    @Order(3)
    void sendInvalidAPIPostRequest() throws IOException, APIServerException {
        JsonObject returned = APICallHandler.sendAPIGetRequest("test-api-server-post");
        assertEquals("METHOD NOT ALLOWED", returned.get("status").getAsString());
    }

    @Test
    @Order(4)
    void timeoutAPIPostRequest() {
        assertThrows(
                APIServerException.class,
                () -> APICallHandler.sendAPIGetRequest("test-api-server-post", null, 1)
        );
    }
}