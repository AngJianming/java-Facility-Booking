package commons.utils;

import commons.requests.Request;
import commons.responses.Response;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class ClientRequest {
    public InetAddress clientAddress;
    public int clientPort;
    public int requestID;
    public Request request;
    public Response sentResponse;

    /**
     * Wrapper class for Request objects with client metadata
     * @param clientAddress client IP address
     * @param clientPort client port
     * @param requestID request identifier
     * @param request Request object
     */
    public ClientRequest(InetAddress clientAddress, int clientPort, int requestID, Request request) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.requestID = requestID;
        this.request = request.getClass().cast(request);
    }

    public Response getSentResponse() {
        return sentResponse;
    }

    public void setSentResponse(Response sentResponse) {
        this.sentResponse = sentResponse;
    }

}
