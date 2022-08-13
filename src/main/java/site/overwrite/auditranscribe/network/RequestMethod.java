/*
 * RequestMethod.java
 *
 * Created on 2022-08-12
 * Updated on 2022-08-12
 *
 * Description: Enum storing the supported request methods to the API server.
 */

package site.overwrite.auditranscribe.network;

/**
 * Enum storing the supported request methods to the API server.
 */
public enum RequestMethod {
    // Enum values
    GET("GET"),
    POST("POST");

    // Attributes
    public final String method;

    // Enum constructor
    RequestMethod(String method) {
        this.method = method;
    }
}
