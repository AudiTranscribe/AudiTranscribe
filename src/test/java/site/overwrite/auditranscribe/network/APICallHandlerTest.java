/*
 * APICallHandlerTest.java
 *
 * Created on 2022-07-07
 * Updated on 2022-08-12
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
    void sendAPIRequest1() throws IOException, APIServerException {
        JsonObject returned = APICallHandler.sendAPIRequest("test-api-server", RequestMethod.GET);

        assertEquals("OK", returned.get("status").getAsString());
        assertEquals("Hello World!", returned.get("data1").getAsString());
        assertFalse(returned.get("data2").getAsBoolean());
        assertEquals(12.345, returned.get("data3").getAsDouble(), 1e-5);
    }

    @Test
    @Order(2)
    void sendAPIRequest2() throws IOException, APIServerException {
        HashMap<String, String> params = new HashMap<>();
        params.put("is-testing", "True");

        JsonObject returned = APICallHandler.sendAPIRequest("test-api-server", RequestMethod.GET, params);

        assertEquals("OK", returned.get("status").getAsString());
        assertEquals("Hello World!", returned.get("data1").getAsString());
        assertFalse(returned.get("data2").getAsBoolean());
        assertEquals(12.345, returned.get("data3").getAsDouble(), 1e-5);
        assertEquals(678.9, returned.get("data4").getAsDouble(), 1e-5);
    }

    @Test
    @Order(3)
    void sendAPIRequest3() throws IOException, APIServerException {
        JsonObject returned = APICallHandler.sendAPIRequest("test-api-server", RequestMethod.POST);

        assertEquals("OK", returned.get("status").getAsString());
        assertEquals("Eggs and spam", returned.get("data1").getAsString());
        assertFalse(returned.get("data2").getAsBoolean());
        assertEquals(12.345, returned.get("data3").getAsDouble(), 1e-5);
    }
}