package server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LostResponseNonIdempotentTestServer {
    @Test
    void Main(){
        System.out.println("Lost Response Test");
        int serverPort = 5000;
        float packetDropOffRate = 0;
        Server server = new Server(serverPort, false, packetDropOffRate);

        for (int i = 0; i < 3; i++) {
            server.run(10000);
        }
        assertEquals(3, server.getTestCounter());
        System.out.println("Expected: 3, Actual: " + server.getTestCounter());
        server.serverCommunicator.close();
    }
}