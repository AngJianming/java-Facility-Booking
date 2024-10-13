package client;

import commons.requests.*;
import commons.rpc.ClientCommunicator;
import org.junit.jupiter.api.Test;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

class LostResponseIdempotentTestClient {
    @Test
    void main(){
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
        ClientCommunicator router = new ClientCommunicator(3000, serverAddress, serverPort, 3, 1000, packetDropOffRate);
        Request req = new BookFacilityRequest(
                "gym",
                ServiceManager.getDatetimeFromString("Monday/10/00"),
                ServiceManager.getDatetimeFromString("Monday/11/00")
        );
        ServiceManager.request(router, req);
        router.close();

        router = new ClientCommunicator(3000, serverAddress, serverPort, 3, 1, packetDropOffRate);

        List<String> days = new ArrayList<>();
        days.add("Monday");
        req = new DeleteBookingRequest(
                0,
                "gym"
        );
        ServiceManager.request(router, req);
        router.close();
    }
}