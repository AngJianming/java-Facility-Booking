package client;

import commons.rpc.ClientCommunicator;
import client.CLI;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Entrypoint for client, takes in 2 runtime arguments: server hostname and server port
 * @param args list of String arguments to configure the server (hostname and port)
 */

public class Main {
    public static void main(String[] args) {
        InetAddress serverAddress = null;
        String hostname = args[0];
        int serverPort = Integer.parseInt(args[1]);
        int clientPort = Integer.parseInt(args[2]);
        double packetDropOffRate = Double.parseDouble(args[3]);
        int timeout = Integer.parseInt(args[4]);; // in ms
        int maxTries = Integer.parseInt(args[5]);

        try {
            serverAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ClientCommunicator router = new ClientCommunicator(clientPort, serverAddress, serverPort, maxTries, timeout, packetDropOffRate);
        CLI.run(router, serverAddress, serverPort);
    }



}
