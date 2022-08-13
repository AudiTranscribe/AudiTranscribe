/*
 * APICallHandler.java
 *
 * Created on 2022-07-07
 * Updated on 2022-08-13
 *
 * Description: Methods that handle the reading and processing of API calls.
 */

package site.overwrite.auditranscribe.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import site.overwrite.auditranscribe.exceptions.network.APIServerException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Methods that handle the reading and processing of API calls.
 */
public final class APICallHandler {
    // Constants
    static final String API_SERVER_URL = "https://api.auditranscribe.app/";

    public static int CONNECTION_TIMEOUT = 5000;  // In milliseconds; duration to wait for connecting to server
    public static int READ_TIMEOUT = 5000;  // Duration to wait for reading the data

    private APICallHandler() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that sends an API request to the API server using the desired method.
     *
     * @param page   Page path to go to.
     * @param method Method to use to send the request.
     * @return JSON object data as sent by the server.
     * @throws IOException If something went wrong when processing the URL or when opening a
     *                     connection to the API server.
     */
    public static JsonObject sendAPIRequest(String page, RequestMethod method) throws IOException,
            APIServerException {
        return sendAPIRequest(page, method, null);
    }

    /**
     * Method that sends an API request to the API server using the desired method and with the
     * specified parameters.
     *
     * @param page   Page path to go to.
     * @param method Method to use to send the request.
     * @param params Parameters to include in the request.
     * @return Raw request data from the server,
     * @throws IOException If something went wrong when processing the URL or when opening a
     *                     connection to the API server.
     */
    public static JsonObject sendAPIRequest(
            String page, RequestMethod method, Map<String, String> params
    ) throws IOException, APIServerException {
        // Form the destination URL
        String urlString = API_SERVER_URL + page;
        if (params != null) {
            urlString += "?" + paramsMapToString(params);
        }
        URL url = new URL(urlString);

        // Set up a connection to the URL
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        StringBuilder content = new StringBuilder();

        try {
            // Set the request method for the connection
            con.setRequestMethod(method.method);

            // Set timeouts
            con.setConnectTimeout(CONNECTION_TIMEOUT);
            con.setReadTimeout(READ_TIMEOUT);

            // Get output from server
            Reader streamReader;

            if (con.getResponseCode() > 299) {
                streamReader = new InputStreamReader(con.getErrorStream());
            } else {
                streamReader = new InputStreamReader(con.getInputStream());
            }

            BufferedReader in = new BufferedReader(streamReader);
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Close the input stream
            in.close();
        } catch (ConnectException e) {
            throw new APIServerException("Connection to '" + urlString + "' refused");
        } catch (SocketTimeoutException e) {
            throw new APIServerException("Connection to '" + urlString + "' timed out");
        } finally {
            // Must remember to disconnect
            con.disconnect();
        }

        // Parse the content as JSON data
        return JsonParser.parseString(content.toString()).getAsJsonObject();
    }

    // Private methods

    /**
     * Helper method that converts a map of the parameters into a string that can be appended to the
     * URL.
     *
     * @param params Parameter map.
     * @return String that contains all the parameters.
     */
    private static String paramsMapToString(Map<String, String> params) {
        // Build the main part of the string
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        // Remove trailing ampersand before returning
        return result.substring(0, result.length() - 1);
    }
}
