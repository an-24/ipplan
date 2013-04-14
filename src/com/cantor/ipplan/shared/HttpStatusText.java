package com.cantor.ipplan.shared;

import java.util.Hashtable;

public class HttpStatusText {

    private static final Hashtable map = new Hashtable();

    private static void set(int status, String phrase) {
        map.put(status, phrase);
    }

    public static String get(int status) {
        return (String) map.get(status);
    }

    static {
        set(100, "Continue. The server has received the initial part of the request, and the client can continue with the remainder of its request.");
        set(101, "Switching Protocols. The server is willing to comply with the client's request to switch protocols to the one specified in the request's Upgrade header. This might include switching to a newer HTTP version.");
        set(200, "OK. The client's request was successful and the server's response contains the requested data. This is the default status code.");
        set(201, "Created. A resource has been created on the server, presumably in response to a client request. The response body should include the URL(s) where the new resource can be found, with the most specific URL set in the Location header. If the resource cannot be created immediately, an SC_ACCEPTED status code should be returned instead.");
        set(202, "Accepted. The request has been accepted for processing but has not yet completed. The server should describe the current status of the request in the response body. The server is under no obligation to act on or complete the request. ");
        set(203, "Non-Authoritative Information. The HTTP response headers came from a local or third-party source, rather than the original server. Normal servlets have no reason to use this status code. ");
        set(204, "No Content. The request succeeded but there was no new response body to return. Browsers receiving this code should retain their current document view. This is a useful code for a servlet to use when it accepts data from a form but wants the browser view to stay at the form, as it avoids the 'Document contains no data' error message. ");
        set(205, "Reset Content. The request succeeded and the browser should reset (reload) the current document view. This is a useful code for a servlet to use when it accepts data from a form and wants the form redisplayed in a fresh state. ");
        set(206, "Partial Content. The server has completed a partial GET request and returned the portion of the document specified in the client's Range header. ");
        set(300, "Multiple Choices. The requested URL refers to more than one resource. For example, the URL may refer to a document translated into many languages. The response body should explain the client's options in a format appropriate for the response content type. The server can suggest a choice with the Location header. ");
        set(301, "Moved Permanently. The requested resource has permanently moved to a new location and future references should use the new URL in their requests. The new location is given by the Location header. Most browsers automatically access the new location. ");
        set(302, "Found. The requested resource has temporarily moved to another location, but future references should still use the original URL to access the resource. The new location is given by the Location header. Most browsers automatically access the new location. ");
        set(303, "See Other. The requested resource processed the request but the client should get its response by performing a GET on the URL specified in the Location header. This code is useful for a servlet that wants to receive POST data then redirect the client to another resource for the response. ");
        set(304, "Not Modified. The requested document has not changed since the date specified in the request's If-Modified-Since header. Normal servlets should not need to use this status code. They implement getLastModified() instead.");
        set(305, "Use Proxy. The requested resource must be accessed via the proxy given in the Location header. ");
        set(307, "Temporary Redirect");
        set(400, "Bad Request. The server could not understand the request, probably due to a syntax error.");
        set(401, "Unauthorized. The request lacked proper authorization. Used in conjunction with the WWW-Authenticate and Authorization headers.");
        set(402, "Payment Required. Reserved for future use. Proposals exist to use this code in conjunction with a Charge-To header, but this has not been standardized as of press time.");
        set(403, "Forbidden. The request was understood, but the server is not willing to fulfill it. The server can explain the reason for its unwillingness in the response body. ");
        set(404, "Not Found. The requested resource was not found or is not available.");
        set(405, "Method Not Allowed. The method used by the client is not supported by this URL. The methods that are supported must be listed in the response's Allow header.");
        set(406, "Not Acceptable. The requested resource exists, but not in a format acceptable to the client (as indicated by the Accept header(s) in the request). ");
        set(407, "Proxy Authentication Required. The proxy server needs authorization before it can proceed. Used with the Proxy-Authenticate header. Normal servlets should not need to use this status code. ");
        set(408, "Request Time-out. The client did not completely finish its request within the time that the server was willing to listen. ");
        set(409, "Conflict. The request could not be completed because it conflicted with another request or the server's configuration. This code is most likely to occur with HTTP PUT requests, where the file being put is under revision control and the new version conflicts with some previous changes. The server can send a description of the conflict in the response body. ");
        set(410, "Gone. The resource is no longer available at this server, and no alternate address is known. This code should be used only when the resource has been permanently removed. Normal servlets have no reason to use this status code. ");
        set(411, "Length Required. The server will not accept the request without a Content-Length header. ");
        set(412, "Precondition Failed. A precondition specified by one or more If... headers in the request evaluated to false. ");
        set(413, "Request Entity Too Large. The server will not process the request because the request content is too large. If this limitation is temporary, the server can include a Retry-After header. ");
        set(414, "Request-URI Too Large. The server will not process the request because the request URI is longer than the server is willing to interpret. This can occur when a client has accidentally converted a POST request into a GET request. Normal servlets have no reason to use this status code. ");
        set(415, "Unsupported Media Type. The server will not process the request because the request body is in a format unsupported by the requested resource. ");
        set(416, "Requested range not satisfiable.");
        set(417, "Expectation Failed.");
        set(500, "Internal Server Error. An unexpected error occurred inside the server that prevented it from fulfilling the request. ");
        set(501, "Not Implemented. The server does not support the functionality needed to fulfill the request. ");
        set(502, "Bad Gateway. A server acting as a gateway or proxy did not receive a valid response from an upstream server. ");
        set(503, "Service Unavailable. The service (server) is temporarily unavailable but should be restored in the future. If the server knows when it will be available again, a Retry-After header may also be supplied. ");
        set(504, "Gateway Time-out. A server acting as a gateway or proxy did not receive a valid response from an upstream server during the time it was prepared to wait. ");
        set(505, "HTTP Version not supported. The server does not support the version of the HTTP protocol used in the request. The response body should specify the protocols supported by the server. Normal servlets should not need to use this status code. ");

    }

}
