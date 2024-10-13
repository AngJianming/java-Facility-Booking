package server;

import commons.requests.DeleteBookingRequest;
import commons.requests.QueryAvailabilityRequest;
import commons.requests.Request;

import commons.requests.*;
import commons.responses.*;
import commons.responses.Response;
import commons.responses.TestResponse;
import commons.rpc.ServerCommunicator;
import commons.utils.ClientRequest;
import server.entities.*;
import server.managers.*;
import server.translator.Translator;


import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Optional;

/**
 * Entrypoint for server, initialises state of in-memory store and
 * instantiates Managers. Keeps listening for requests and calls on
 * the corresponding Translator methods
 */
public class Server {
    BookingManager bookingManager;
    ObserverManager observerManager;
    Hashtable<String, Facility> facilTable;
    ServerCommunicator serverCommunicator;
    Translator translator;

    int testCounter = 0;
    int bookingIdCounter;


    public Server(int serverPort, boolean atMostOnce, double packetDropOffRate) {
        this.bookingManager = new BookingManager();
        this.observerManager = new ObserverManager();
        this.facilTable = new Hashtable<>();
        this.serverCommunicator = new ServerCommunicator(serverPort, atMostOnce, packetDropOffRate);
        this.translator = new Translator(serverCommunicator);

        bookingIdCounter =0;
        Facility badmintonCourt = new Facility("badmintonCourt");
        Facility gym = new Facility("gym");
        facilTable.put("badmintoncourt", badmintonCourt);
        facilTable.put("gym", gym);
    }

    /**
     * Infinite loop to keep socket on to listen for requests
     * @param args server port and flag to select at-most-once or at-least-once semantics
     */
    public static void main(String[] args) {
        int serverPort = Integer.parseInt(args[0]);
        boolean atMostOnce = Boolean.parseBoolean(args[1]);
        double packetDropOffRate = Double.parseDouble(args[2]);
        Server server = new Server(serverPort, atMostOnce, packetDropOffRate);
        while (true) {
            server.run(0);
        }
    }

    void run(int timeout) {
        Optional<ClientRequest> optionalClientRequest;
        if (timeout == 0) {
            optionalClientRequest = serverCommunicator.receive();
        } else {
            optionalClientRequest = serverCommunicator.receive(timeout);
        }


        if (optionalClientRequest.isPresent()) {
            ClientRequest clientRequest = optionalClientRequest.get();
            Request request = clientRequest.request;
            Response response = null;
            String facilName = null;
            Exception e = null;
            Boolean packetDrop = true;


            switch(request.getClass().getName()){
                case "commons.requests.BookFacilityRequest":
                    System.out.println("Book Facility Request with ID " + clientRequest.requestID + " received, processing...");
                    Response createResponse = translator.createBooking((BookFacilityRequest)request, bookingIdCounter, 0, bookingManager, facilTable);
                    if (createResponse instanceof BookFacilityResponse) {
                        bookingIdCounter +=1;
                        facilName = ((BookFacilityRequest) request).facilityName;
                        e = translator.notifyObservers(facilName, bookingManager, observerManager, facilTable);
                    }
                    response = createResponse;
                    break;
                case "commons.requests.DeleteBookingRequest":
                    System.out.println("Delete Booking Request with ID " + clientRequest.requestID + " received, processing...");
                    Response deleteResponse = translator.deleteBooking((DeleteBookingRequest) request, bookingManager, facilTable);
                    if (deleteResponse instanceof DeleteBookingResponse) {
                        facilName = ((DeleteBookingRequest) request).facilityName;
                        e = translator.notifyObservers(facilName, bookingManager, observerManager, facilTable);
                    }
                    response = deleteResponse;
                    break;
                case "commons.requests.OffsetBookingRequest":
                    System.out.println("Offset Booking Request with ID " + clientRequest.requestID + " received, processing...");
                    Response offsetResponse = translator.offsetBooking((OffsetBookingRequest) request, bookingManager, facilTable);
                    if (offsetResponse instanceof  OffsetBookingResponse){
                        facilName = ((OffsetBookingRequest) request).facilityName;
                        e = translator.notifyObservers(facilName, bookingManager, observerManager, facilTable);
                    }
                    response = offsetResponse;
                    break;
                case "commons.requests.QueryAvailabilityRequest":
                    System.out.println("Query Availability Request with ID " + clientRequest.requestID + " received, processing...");
                    response = translator.queryAvailability((QueryAvailabilityRequest) request, bookingManager, facilTable);
                    break;
                case "commons.requests.RegisterInterestRequest":
                    System.out.println("Register Interest Request with ID " + clientRequest.requestID + " received, processing...");
                    InetAddress clientAddress = clientRequest.clientAddress;
                    int clientPort = clientRequest.clientPort;
                    response = translator.addObserver((RegisterInterestRequest) request, observerManager, facilTable, clientAddress, clientPort);
                    packetDrop = false;
                    break;
                case "commons.requests.ExtendBookingRequest":
                    System.out.println("Extend Booking Request with ID " + clientRequest.requestID + " received, processing...");
                    Response extendResponse = translator.extendBooking((ExtendBookingRequest) request, bookingManager, facilTable);
                    if (extendResponse instanceof  ExtendBookingResponse){
                        facilName = ((ExtendBookingRequest) request).facilityName;
                        e = translator.notifyObservers(facilName, bookingManager, observerManager, facilTable);
                    }
                    response = extendResponse;
                    break;
                case "commons.requests.TestRequest":
                    System.out.println("Test Request with ID " + clientRequest.requestID + " received, processing...");
                    this.testCounter++;
                    response = new TestResponse();
                    break;
                default:
                    System.out.println("Invalid Request Received");
                    throw new RuntimeException("Invalid Request Type");
            }
            clientRequest.setSentResponse(response);
            serverCommunicator.send(response, clientRequest.clientAddress, clientRequest.clientPort, packetDrop);
        }
    }


    public int getTestCounter() {
        return testCounter;
    }

    public void setTestCounter(int testCounter) {
        this.testCounter = testCounter;
    }
}

