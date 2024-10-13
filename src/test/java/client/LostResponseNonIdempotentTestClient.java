package client;

import commons.requests.QueryAvailabilityRequest;
import commons.requests.Request;
import commons.requests.TestRequest;
import commons.rpc.ClientCommunicator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LostResponseNonIdempotentTestClient {
    @Test
    void main() {
        System.out.println("Lost Response Test");
        InetAddress serverAddress = null;
        String hostname = "localhost";
        int serverPort = 5000;
        float packetDropOffRate = 0;
        try {
            serverAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ClientCommunicator router = new ClientCommunicator(3000, serverAddress, serverPort, 3, 1, packetDropOffRate);
        Request req = new TestRequest();
        ServiceManager.request(router, req);
        router.close();
    }
}