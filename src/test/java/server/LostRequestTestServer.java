package server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.entities.Booking;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.*;

class LostRequestTestServer {
    @Test
    void LostRequestTest() {
        int serverPort = 5000;
        int sleep = 5000;
        float packetDropOffRate = 0;
        Server server = new Server(serverPort, true, packetDropOffRate);
        System.out.println("Lost Request Test");
        // only open port for 1ms every 5000ms
        server.serverCommunicator.close();
        for (int i = 0; i < 3; i++){
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                server.serverCommunicator.socket = new DatagramSocket(serverPort);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            server.run(5000);
            server.serverCommunicator.close();
        }
        // server never receives request
        Hashtable bookingsTable = server.facilTable.get("gym").getBookingsTable();
        System.out.println(bookingsTable);
        assertEquals(0, bookingsTable.size());
        System.out.println("Number of Bookings: Expected: 0, Actual: " + bookingsTable.size());
    }
}