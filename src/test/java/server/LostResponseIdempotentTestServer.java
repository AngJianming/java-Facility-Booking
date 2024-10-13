package server;

import org.junit.jupiter.api.Test;

import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LostResponseIdempotentTestServer {
    @Test
    void Main(){
        System.out.println("Lost Response Test");
        int serverPort = 5000;
        float packetDropOffRate = 0;
        Server server = new Server(serverPort, true, packetDropOffRate);

        for (int i = 0; i < 4; i++) {
            server.run(0);
            Hashtable bookingsTable = server.facilTable.get("gym").getBookingsTable();
            System.out.println(bookingsTable);
            if (i == 1){
                server.serverCommunicator.clientRequests.get(1).clientPort = 9999;
            }
        }
//        assertEquals(1, server.getTestCounter());

//        System.out.println("Expected: 1, Actual: " + server.getTestCounter());
        server.serverCommunicator.close();
    }
}