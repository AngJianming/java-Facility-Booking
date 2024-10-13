package commons.rpc;

import commons.Deserializer;
import commons.Serializer;
import commons.requests.Request;
import commons.responses.Response;
import commons.utils.ClientRequest;
import commons.utils.Packet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Server's communicator module to receive incoming requests and send responses,
 * handles duplicate requests and caches responses for at-most-once semantics
 */
public class ServerCommunicator {
    public ArrayList<ClientRequest> clientRequests;
    ArrayList<Integer> clientRequestsHashed;

    int serverPort;
    int packetSize;
    int messageSize;
    int headerSize;
    public DatagramSocket socket;
    int requestID;
    boolean atMostOnce;
    double packetDropOffRate;
    boolean retryReceive;
    Packet[] packetsOrdered;

    public ServerCommunicator(int serverPort, boolean atMostOnce, double packetDropOffRate) {
        this.clientRequests = new ArrayList<>();
        this.clientRequestsHashed = new ArrayList<>();
        this.requestID = 0;
        this.packetSize = 512;
        this.headerSize = 16;
        this.messageSize = this.packetSize - this.headerSize;
        this.serverPort = serverPort;
        this.atMostOnce = atMostOnce;
        this.packetDropOffRate = packetDropOffRate;
        this.retryReceive = false;
        try {
            this.socket = new DatagramSocket(serverPort);
        } catch (SocketException e) {}
        /**
         * Headersize Param
         * 4 bytes for requestID
         * 4 bytes for the datagram number
         * 4 bytes to send the total number of datagrams.
         * 4 bytes to send length of message in datagram
         */
    }

    /**
     * To Test, run ServerCommunicator then run ClientCommunicator
     */

    public static void main(String[] args) {
        ServerCommunicator serverCommunicator = new ServerCommunicator(5000, true, 0);
        while (true){
            serverCommunicator.receive(100);
        }
    }

    /**
     * Allows server to listen for requests from client
     * @return ClientRequest object which is a wrapper for a Request object with metadata such as client IP and port
     */
    public Optional<ClientRequest> receive() {
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
                System.out.println("packet " + j + "lost");
                packetsLost++;
            }
        }

        if (packetsLost > 0){
            System.out.println(packetsLost + " packets lost");
            return Optional.empty();
        }
        else{
            for (Packet p : this.packetsOrdered){
                combinedMessageBuffer.put(p.messageBuffer);
            }
        }

        combinedMessageBuffer.flip();
        Object deserializedRequest = Deserializer.deserializeObject(combinedMessageBuffer);
        ClientRequest clientRequest;

        if (deserializedRequest instanceof commons.requests.Request){
            clientRequest = new ClientRequest(currPacket.senderAddress, currPacket.senderPort,
                    currPacket.requestID, (Request)deserializedRequest);
        }
        else{
            throw new Error("Server only receives Requests");
        }

        int duplicateIndex = checkDuplicateRequest(clientRequest);

        if (duplicateIndex == -999){ // original request
            return Optional.of(clientRequest);
        }
        else{ // duplicate request
            if (this.atMostOnce){ // at most once implementation
                System.out.println("At Most Once Implementation - sending Original Response...");
                ClientRequest orgRequest = this.clientRequests.get(duplicateIndex);
                send(orgRequest.sentResponse, clientRequest.clientAddress, clientRequest.clientPort, true);
                return Optional.empty();
            }
            else{ // at least once implementation
                System.out.println("At least Once Implementation - processing Request...");
                return Optional.of(clientRequest);
            }
        }
    }

    /**
     * Helper method to test server timeout
     * @param timeout
     * @return ClientRequest object which is a wrapper for a Request object with metadata such as client IP and port
     */
    public Optional<ClientRequest> receive(int timeout) {
        try {
            this.socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try{
            return this.receive();
        } catch (RuntimeException e){
            if (e.getCause() instanceof SocketTimeoutException) {
                System.out.println("Socket Timeout");
            }
        }
        return Optional.empty();
    }

    /**
     * Sends a Response object to a client
     * @param r Response object constructed by Translator object
     * @param clientAddress IP address of client
     * @param clientPort Port of client socket
     */
    public void send(Response r, InetAddress clientAddress, int clientPort, boolean packetDrop) {
        System.out.println("Sending " + r.getClass().getName() + " to " + clientAddress + " port " + clientPort);
        ByteBuffer dataBuf = ByteBuffer.allocate(20000);
        Serializer.serializeObject(r, dataBuf);

        int totalDatagramPackets = (int) Math.ceil(dataBuf.position() / (float) this.messageSize);
        int dataBufPtr = 0;
        int dataBufMaxPos = dataBuf.position();

        System.out.println("Number of packets required: " + totalDatagramPackets);

        for (int i = 0; i < totalDatagramPackets; i++){
            ByteBuffer packetBuf = ByteBuffer.allocate(this.packetSize);
            packetBuf.putInt(this.requestID);
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
            DatagramPacket message = new DatagramPacket(packetByteArray, packetByteArray.length, clientAddress, clientPort);
            boolean sendPacket = true;
            if (packetDrop && Math.random() < this.packetDropOffRate){
                sendPacket = false;
                System.out.println("Packet " + i +" Dropped");
            }

            if (sendPacket){
                try {
                    this.socket.send(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /**
         * comment out row below to test duplicate request
         */
//        this.requestID += 1;
//        System.out.println("Sent" + " to Address: " + clientAddress + ", Port: " + clientPort);

    }

    /**
     * Helper method to receive indiviudal datagram packets
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
     * Helper method to check for duplicate request
     * @param clientRequest
     * @return index of request object in the list of cached request objects
     */
    private int checkDuplicateRequest(ClientRequest clientRequest){
        int clientRequestHash = hashClientRequest(clientRequest);
//        System.out.println("Hashed: " + clientRequestHash);
        if (clientRequestsHashed.contains(clientRequestHash)){
            System.out.println("Duplicate request of " + clientRequest.request.getClass().getName() + " received");
            int index = clientRequestsHashed.indexOf(clientRequestHash);
            return index;
        }
        else{
            this.clientRequests.add(clientRequest);
            this.clientRequestsHashed.add(clientRequestHash);
            return -999;
        }
    }

    /**
     * Helper method to hash a client request
     * @param clientRequest
     * @return hashcode
     */
    private int hashClientRequest(ClientRequest clientRequest){
        int hash = 0;
        hash += clientRequest.clientAddress.hashCode();
        hash += clientRequest.clientPort;
        hash += clientRequest.requestID;

//        hash += clientRequest.request.hashCode();
        Field[] fields = clientRequest.request.getClass().getDeclaredFields();
        for (Field f: fields) {
            hash += f.hashCode();
        }
        return hash;
    }

    /**
     * Helper method to close the socket
     */
    public void close() {
        System.out.println("Closing socket");
        this.socket.close();
    }
}
