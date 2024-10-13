package commons.rpc;
import commons.Deserializer;
import commons.Serializer;
import commons.requests.Request;
import commons.requests.TestRequest;
import commons.responses.NullResponse;
import commons.responses.Response;
import commons.responses.TestResponse;
import commons.utils.Packet;
import commons.utils.ResponseMessage;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.lang.Math;

// To use, run TestServer on 1 terminal then run TestClient on another terminal

/**
 * Client's communicator module to send requests and receive responses via sockets using UDP,
 * handles retransmissions upon socket timeout
 */
public class ClientCommunicator {
    int clientPort;
    InetAddress serverAddress;
    int serverPort;
    int packetSize;
    int messageSize;
    int headerSize;
    public DatagramSocket socket;
    int maxTries;
    int requestID;
    public int socketTimeout;
    double packetDropOffRate;
    Packet[] packetsOrdered;
    boolean retryReceive;
    /**
     * Creates a socket to send / receive UDP packets at specified port number
     * Packet size should be fixed across both client and main.java.server (hence its not an arg)
     *
     * @param clientPort port number for UDP socket
     */
    public ClientCommunicator(int clientPort, InetAddress serverAddress, int serverPort, int maxTries, int timeout, double packetDropOffRate) {
        this.clientPort = clientPort;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.packetSize = 512;
        /**
         * 4 bytes for requestID
         * 4 bytes for the datagram number
         * 4 bytes to send the total number of datagrams.
         * 4 bytes to send length of message in datagram
         */
        this.headerSize = 16;
        this.messageSize = this.packetSize - this.headerSize;
        this.maxTries = maxTries;
        try {
            this.socket = new DatagramSocket(clientPort);
            this.socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        this.requestID = (int)(Math.random()*1000);
        this.socketTimeout = timeout;
        this.packetDropOffRate = packetDropOffRate;
        this.retryReceive = false;
        System.out.println("Starting Client on Port " + clientPort);
        System.out.println();
    }

    /**
     * To Test, run ServerCommunicator then run ClientCommunicator
     */

    public static void main(String[] args) {
        // Success
        InetAddress serverAddress = null;
        String hostname = args[0];
        int serverPort = Integer.parseInt(args[1]);
        try {
            serverAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println("\n###### Testing Successful Request #######");
        ClientCommunicator clientCommunicator = new ClientCommunicator(22, serverAddress, serverPort, 3, 5000, 0);
        TestRequest request = new TestRequest();
        Response response = clientCommunicator.sendRequest(request);
        System.out.println("Client Hash: " + request.hashCode());
        TestResponse expected = new TestResponse();

        System.out.println("Response: " + response.name + " , Expected: " + expected.name);

        //Test Duplicate
        System.out.println("\n###### Testing Duplicate Request #######");
        response = clientCommunicator.sendRequest(request, 0);
        System.out.println("Client Hash should be same as above: " + request.hashCode());
        System.out.println("Response: " + response.name + " , Expected: " + expected.name);

        //Test Timeout
        System.out.println("\n###### Testing Timeout ######");
        clientCommunicator.socket.close();
        clientCommunicator = new ClientCommunicator(22, serverAddress, 17, 3, 1, 0);

        response = clientCommunicator.sendRequest(request);
        expected = new TestResponse();

        try{
            System.out.println("Response: " + response.name + " , Expected: " + expected.name);
        }
        catch (NullPointerException e){
            System.out.println("No Response after all retries");
        }
    }

    /**
     * Sends a request, retransmitting if necessary, to the server and
     * receives a corresponding response object
     * @param r request object constructed by ServiceManager
     * @return response object
     */
    public Response sendRequest(Request r){
        int currTries = 0;
        Response response = null;
        ByteBuffer dataBuf = ByteBuffer.allocate(2000);
        Serializer.serializeObject(r, dataBuf);

        while (currTries < this.maxTries){
            currTries += 1;
            System.out.println("Sending " + r.getClass().getName() + " to server with ID " + this.requestID);
            this.send(dataBuf, this.requestID);
            try{
                response = this.receive();
                if (response != null){ //message is complete
                    this.retryReceive = false;
                    this.requestID += 1;
                    return response;
                }
                this.retryReceive = true; //need to retry
            } catch (RuntimeException e){
                if (e.getCause() instanceof SocketTimeoutException) {
                    System.out.println("Socket Timeout, No Response Received");
                } else{
                    e.printStackTrace();
                }
            }
        }
        if (currTries == this.maxTries){
            System.out.println("No response from server after all retries exceeded");
        }
        this.requestID += 1;
        ResponseMessage responseMessage = new ResponseMessage(500, "Request Timeout while waiting for reply.");
        this.retryReceive = false;
        return new NullResponse(responseMessage);
    }

    /**
     * Helper method to send duplicate requests
     * @param r request object
     * @param requestID request identifier
     * @return response object
     */
    public Response sendRequest(Request r, int requestID){
        int currTries = 0;
        Response response = null;
        ByteBuffer dataBuf = ByteBuffer.allocate(2000);
        Serializer.serializeObject(r, dataBuf);

        while (currTries < this.maxTries){
            currTries += 1;
            System.out.println("Sending " + r.getClass().getName() + " to server");
            this.send(dataBuf, requestID);
            try{
//                received = this.receive();
//                if (received.getKey() != null){ // no packet loss
//                    return received.getKey();
//                }
                response = this.receive();
                if (response != null){
                    this.retryReceive = false;
                    return response;
                }
                this.retryReceive = true;
                for (int i = 0; i < this.packetsOrdered.length; i++){
                    System.out.println("Global Packet Hash " + this.packetsOrdered[i].messageBuffer.hashCode());
                }
            } catch (RuntimeException e){
                if (e.getCause() instanceof SocketTimeoutException) {
                    System.out.println("Socket Timeout");
                }
            }
        }
        if (currTries == this.maxTries){
            System.out.println("No response from server after all retries exceeded");
        }
        ResponseMessage responseMessage = new ResponseMessage(500, "Request Timeout while waiting for reply.");
        return new NullResponse(responseMessage);
    }

    /**
     * Helper method used by sendRequest to send individual datagram packets to the server
     * @param dataBuf ByteBuffer that contains the byte sequence of serialised request
     * @param requestID request identifier
     */
    public void send(ByteBuffer dataBuf, int requestID) {
        int totalDatagramPackets = (int) Math.ceil(dataBuf.position() / (float) this.messageSize);
        int dataBufPtr = 0;
        int dataBufMaxPos = dataBuf.position();
//        System.out.println("Number of packets required: " + totalDatagramPackets);

        for (int i = 0; i < totalDatagramPackets; i++){
            ByteBuffer packetBuf = ByteBuffer.allocate(this.packetSize);
            packetBuf.putInt(requestID);
            packetBuf.putInt(i);
            packetBuf.putInt(totalDatagramPackets);
            byte[] packetByteArray;
            dataBuf.position(dataBufPtr);

            if (i == totalDatagramPackets - 1){ // last packet or when only 1 packet needed - only writes remainder of dataBuf
                int lengthRemaining = dataBufMaxPos - dataBufPtr;
                packetBuf.putInt(lengthRemaining);
                packetByteArray = packetBuf.array();
                dataBuf.get(packetByteArray, this.headerSize, lengthRemaining);
            }
            else{ // writes messageSize worth of data to DatagramPacket
                packetBuf.putInt(this.messageSize);
                packetByteArray = packetBuf.array();
                dataBuf.get(packetByteArray, this.headerSize, this.messageSize);
                dataBufPtr += this.messageSize;
            }
            DatagramPacket message = new DatagramPacket(packetByteArray, packetByteArray.length, this.serverAddress, this.serverPort);

            if (Math.random() > this.packetDropOffRate){
                try {
                    this.socket.send(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println("Packet Dropped");
            }
        }
    }

    /**
     * Allows socket to listen for UDP packets
     * @return Response object, deserialised from the ByteBuffer
     */
    public Response receive() {
        System.out.println("listening...");
        Packet currPacket = this.receivePacket();
        int combinedMessageSize = currPacket.totalDatagramPackets * this.packetSize;
        if (!this.retryReceive){ //init new array of correct size for new Response
            this.packetsOrdered = new Packet[currPacket.totalDatagramPackets];
        }
        this.packetsOrdered[currPacket.datagramNum] = currPacket;
        ByteBuffer combinedMessageBuffer = ByteBuffer.allocate(combinedMessageSize);

        if (currPacket.totalDatagramPackets != 1) {
            for (int i = 0; i < currPacket.totalDatagramPackets - 1; i++){
                try{
                    currPacket = this.receivePacket();
                    this.packetsOrdered[currPacket.datagramNum] = currPacket;
                } catch (RuntimeException e){
                    if (e.getCause() instanceof SocketTimeoutException) {
                        System.out.println("Socket Timeout");
                    }
                }
            }
        }
        int packetsLost = 0;
        for (int j = 0; j < this.packetsOrdered.length; j++) {
            Packet p = this.packetsOrdered[j];
            if (p == null) { // any packet is missing from the message
//                throw new LostPacketError();
                System.out.println("packet " + j + " lost");
                packetsLost++;
            }
        }

        if (packetsLost > 0){
            System.out.println(packetsLost + " Response Packet(s) Lost in Transmission, Retrying...");
            return null;
        }
        else{
            for (Packet p : this.packetsOrdered){
                combinedMessageBuffer.put(p.messageBuffer);
            }
        }
        combinedMessageBuffer.flip();
        Response response = (Response) Deserializer.deserializeObject(combinedMessageBuffer);
        return response;
    }

    /**
     * Helper method to receive individual datagram packets
     * @return datagram packet
     */
    private Packet receivePacket(){
        byte[] buffer = new byte[this.packetSize];
        DatagramPacket message = new DatagramPacket(buffer, buffer.length);
        try {
            this.socket.receive(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ByteBuffer received_bb = ByteBuffer.wrap(buffer);
        int requestID = received_bb.getInt();
        int datagramNum = received_bb.getInt();
        int totalDatagramPackets = received_bb.getInt();
        int messageSize = received_bb.getInt();

        InetAddress senderAddress = message.getAddress();
        int senderPort = message.getPort();

        ByteBuffer messageBuffer = ByteBuffer.allocate(messageSize);
        messageBuffer.put(buffer, this.headerSize, messageSize);
        messageBuffer.position(0);

        return new Packet(requestID, datagramNum, totalDatagramPackets, messageSize, senderAddress, senderPort, messageBuffer);
    }

    /**
     * Helper method to close the socket
     */
    public void close(){
        this.socket.close();
    }

    /**
     * Helper method to set socket timeout
     * @param timeout
     */
    public void setSocketTimeout(int timeout){
        try {
            this.socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}