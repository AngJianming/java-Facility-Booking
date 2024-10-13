package client;

import commons.exceptions.InvalidDayException;
import commons.requests.BookFacilityRequest;
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

import static org.junit.jupiter.api.Assertions.*;

class LostRequestTestClient {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void main() throws InvalidDayException {
        InetAddress serverAddress = null;
        String hostname = "localhost";
        int serverPort = 5000;
        float packetDropOffRate = 0;
        try {
            serverAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ClientCommunicator router = new ClientCommunicator(3000, serverAddress, serverPort, 3, 100, packetDropOffRate);

        Request req = new BookFacilityRequest(
                "gym",
                ServiceManager.getDatetimeFromString("Monday/10/00"),
                ServiceManager.getDatetimeFromString("Monday/11/00")
        );
        ServiceManager.request(router, req);
    }
}