/*
 * APICallHandler.java
 * Description: Methods that handle the reading and processing of API calls.
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
    public static int CONNECTION_TIMEOUT = 5000;  // In milliseconds; duration to wait for connecting to server
    static final String API_SERVER_URL = "https://api.auditranscribe.app/";
//    static final String API_SERVER_URL = "http://127.0.0.1:5000/";  // For testing

    private APICallHandler() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that sends an API GET request to the API server using the desired method.
     *
     * @param page Page path to go to.
     * @return JSON object data as sent by the server.
     * @throws IOException If something went wrong when processing the URL or when opening a
     *                     connection to the API server.
     */
    public static JsonObject sendAPIGetRequest(String page) throws IOException,
            APIServerException {
        return sendAPIGetRequest(page, null, 5000);
    }

    /**
     * Method that sends an API GET request to the API server using the desired method and with the
     * specified parameters.
     *
     * @param page    Page path to go to.
     * @param params  Parameters to include in the request.
     * @param timeout Duration to wait (in <b>milliseconds</b>) before timing out.
     * @return Raw request data from the server,
     * @throws IOException If something went wrong when processing the URL or when opening a
     *                     connection to the API server.
     */
    public static JsonObject sendAPIGetRequest(
            String page, Map<String, String> params, int timeout
    ) throws IOException, APIServerException {
        // Form the destination URL
        String urlString = API_SERVER_URL + page;
        if (params != null) {
            urlString += "?" + paramsMapToString(params);
        }
        URL url = new URL(urlString);

        // Set up a connection to the URL
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String output;

        try {
            // Set the request method for the connection
            con.setRequestMethod("GET");

            // Set timeouts
            con.setConnectTimeout(CONNECTION_TIMEOUT);
            con.setReadTimeout(timeout);

            // Get the output from the connection
            output = getConnectionOutput(con);
        } catch (ConnectException e) {
            throw new APIServerException("Connection to '" + urlString + "' refused");
        } catch (SocketTimeoutException e) {
            throw new APIServerException("Connection to '" + urlString + "' timed out");
        } finally {
            // Must remember to disconnect
            con.disconnect();
        }

        // Parse the content as JSON data
        return JsonParser.parseString(output).getAsJsonObject();
    }

    /**
     * Method that sends an API POST request to the API server using the desired method.
     *
     * @param page   Page path to go to.
     * @param params Parameters to send along the POST request.
     * @return JSON object data as sent by the server.
     * @throws IOException If something went wrong when processing the URL or when opening a
     *                     connection to the API server.
     */
    public static JsonObject sendAPIPostRequest(String page, Map<String, String> params) throws IOException,
            APIServerException {
        return sendAPIPostRequest(page, params, 5000);
    }

    /**
     * Method that sends an API POST request to the API server using the desired method and with the
     * specified parameters.
     *
     * @param page    Page path to go to.
     * @param params  Parameters to include in the request.
     * @param timeout Duration to wait (in <b>milliseconds</b>) before timing out.
     * @return Raw request data from the server,
     * @throws IOException If something went wrong when processing the URL or when opening a
     *                     connection to the API server.
     */
    public static JsonObject sendAPIPostRequest(
            String page, Map<String, String> params, int timeout
    ) throws IOException, APIServerException {
        // Form the destination URL
        String urlString = API_SERVER_URL + page;
        URL url = new URL(urlString);

        // Set up a connection to the URL
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String output;

        try {
            // Set the request method for the connection
            con.setRequestMethod("POST");

            // Set timeouts
            con.setConnectTimeout(CONNECTION_TIMEOUT);
            con.setReadTimeout(timeout);

            // Add POST data
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(paramsMapToString(params));
            out.flush();
            out.close();

            // Get the output from the connection
            output = getConnectionOutput(con);
        } catch (ConnectException e) {
            throw new APIServerException("Connection to '" + urlString + "' refused");
        } catch (SocketTimeoutException e) {
            throw new APIServerException("Connection to '" + urlString + "' timed out");
        } finally {
            // Must remember to disconnect
            con.disconnect();
        }

        // Parse the content as JSON data
        return JsonParser.parseString(output).getAsJsonObject();
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

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                result.append("&");
            }

            // Remove trailing ampersand before returning
            return result.substring(0, result.length() - 1);
        }

        return "";
    }

    /**
     * Helper method that gets the connection's output.
     *
     * @param con Connection.
     * @return String representing the output from the connection.
     * @throws IOException If something went wrong when reading the content.
     */
    private static String getConnectionOutput(HttpURLConnection con) throws IOException, APIServerException {
        StringBuilder content = new StringBuilder();
        Reader streamReader;

        try {
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

            in.close();

            return content.toString();
        } catch (UnknownHostException e) {
            throw new APIServerException(e);
        }
    }
}
