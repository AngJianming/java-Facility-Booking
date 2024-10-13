package server.translator;

import commons.requests.BookFacilityRequest;
import commons.requests.*;
import commons.responses.*;
import commons.rpc.ServerCommunicator;
import commons.utils.Datetime;
import commons.utils.Day;
import commons.utils.ResponseMessage;
import javafx.util.Pair;
import server.entities.Booking;
import server.entities.FacilityObserver;
import server.managers.BookingManager;
import server.managers.ObserverManager;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Interfaces between the ServerCommunicator and the Manager classes handling the business logics,
 * parses the Request objects into individual function parameters required by the corresponding business logic.
 */
public class Translator {
    private ArrayList<String> allDays = new ArrayList<String>();
    private ServerCommunicator serverCommunicator;

    public Translator(ServerCommunicator serverCommunicator){
        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        allDays.addAll(days);

        this.serverCommunicator = serverCommunicator;
    }

    /**
     * Calls BookingManager's queryAvailability method with appropriate parameters
     * @param r Request object
     * @param bookingManager
     * @param facilTable facility table initialised in Server class
     * @return QueryAvailabilityResponse
     */
    public Response queryAvailability(QueryAvailabilityRequest r, BookingManager bookingManager, Hashtable facilTable) {

        Response response;
        ResponseMessage responseMessage;

        String facilName = r.facilityName;
        ArrayList days = (ArrayList) r.days;

        Pair<Hashtable, Exception> outputPair = bookingManager.queryAvailability(facilName, days, facilTable);
        Hashtable outputBooking = outputPair.getKey();
        Exception outputException = outputPair.getValue();

        if (outputException != null){
            responseMessage = new ResponseMessage(getStatusCode(outputException), outputException.getMessage());
            response = new ErrorResponse(responseMessage);
        }
        else{
            responseMessage = new ResponseMessage(200, "success") ;
            ArrayList convertedList =localToDatetime(outputBooking);
            response = new QueryAvailabilityResponse(facilName, convertedList, responseMessage);
        }

        return response;

    }

    /**
     * Calls BookingManager's createBooking method with appropriate parameters
     * @param r Request object
     * @param bookingId booking identifier
     * @param clientId client identifier
     * @param bookingManager
     * @param facilTable facility table initialised in Server class
     * @return CreateBookingResponse
     */
    public Response createBooking(BookFacilityRequest r, int bookingId, int clientId, BookingManager bookingManager, Hashtable facilTable){

        Response response;
        ResponseMessage responseMessage;

        Datetime requestStart = r.startTime;
        Datetime requestEnd = r.endTime;

        LocalTime start = LocalTime.of(requestStart.hour, requestStart.minute, 00);
        LocalTime end = LocalTime.of(requestEnd.hour, requestEnd.minute, 00);
        Day day = requestStart.day;
        String facilName = r.facilityName;

        Pair<Booking,Exception> outputPair = bookingManager.createBooking(day, bookingId, clientId, facilName, start, end, facilTable);
        Booking outputBooking = outputPair.getKey();
        Exception outputException = outputPair.getValue();

        if (outputException != null){
            responseMessage = new ResponseMessage(getStatusCode(outputException), outputException.getMessage()) ;
            response = new ErrorResponse(responseMessage);
        }
        else{
            responseMessage = new ResponseMessage(200, "success") ;
            Datetime dtStart = localToDatetime(outputBooking.getStart(), day);
            Datetime dtEnd = localToDatetime(outputBooking.getEnd(), day);
            response = new BookFacilityResponse(outputBooking.getBookingId(), facilName, dtStart, dtEnd, responseMessage);
        }
        return response;
    }

    /**
     * Calls BookingManager's offsetBooking method with appropriate parameters
     * @param r Request object
     * @param bookingManager
     * @param facilTable facility table initialised in Server class
     * @return OffsetBookingResposne
     */
    public Response offsetBooking(OffsetBookingRequest r, BookingManager bookingManager,Hashtable facilTable ){

        Response response;
        ResponseMessage responseMessage;

        String facilName = r.facilityName;
        int bookingId = r.bookingID;
        int offset = r.offset;
        int offsetMin = offset*30;

        Pair<Booking, Exception> outputPair = bookingManager.offsetBooking(facilName, bookingId, offsetMin, facilTable);
        Booking outputBooking = outputPair.getKey();
        Exception outputException = outputPair.getValue();

        if (outputException != null){
            responseMessage = new ResponseMessage(getStatusCode(outputException), outputException.getMessage()) ;
            response = new ErrorResponse(responseMessage);

        }
        else{
            responseMessage = new ResponseMessage(200, "success") ;
            Day day = outputBooking.getDay();
            Datetime dtStart = localToDatetime(outputBooking.getStart(),day);
            Datetime dtEnd = localToDatetime(outputBooking.getEnd(), day);
            response = new OffsetBookingResponse(bookingId, facilName, dtStart, dtEnd, responseMessage);
        }

        return response;
    }

    /**
     * Calls BookingManager's extendBooking method with appropriate parameters
     * @param r Request object
     * @param bookingManager
     * @param facilTable facility table initialised in Server class
     * @return ExtendBookingResponse
     */
    public Response extendBooking(ExtendBookingRequest r, BookingManager bookingManager,Hashtable facilTable){
        Response response;
        ResponseMessage responseMessage;

        String facilName = r.facilityName;
        int bookingId = r.bookingID;
        int offset = r.extension;
        int offsetMin = offset*30;

        Pair<Booking, Exception> outputPair = bookingManager.extendBooking(facilName, bookingId, offsetMin, facilTable);
        Booking outputBooking = outputPair.getKey();
        Exception outputException = outputPair.getValue();

        if (outputException != null){
            responseMessage = new ResponseMessage(getStatusCode(outputException), outputException.getMessage()) ;
            response = new ErrorResponse(responseMessage);

        }
        else{
            responseMessage = new ResponseMessage(200, "success") ;
            Day day = outputBooking.getDay();
            Datetime dtStart = localToDatetime(outputBooking.getStart(),day);
            Datetime dtEnd = localToDatetime(outputBooking.getEnd(), day);
            response = new ExtendBookingResponse(bookingId, facilName, dtStart, dtEnd, responseMessage);
        }

        return response;
    }

    /**
     * Calls BookingManager's deleteBooking method with appropriate parameters
     * @param r Request object
     * @param bookingManager
     * @param facilTable facility table initialised in Server class
     * @return DeleteBookingResponse
     */
    public Response deleteBooking(DeleteBookingRequest r, BookingManager bookingManager, Hashtable facilTable){
        Response response;
        ResponseMessage responseMessage;

        String facilName = r.facilityName;
        int bookingId = r.bookingID;

        Exception outputException = bookingManager.deleteBooking(facilName, bookingId, facilTable);
        if (outputException != null){
            responseMessage = new ResponseMessage(getStatusCode(outputException), outputException.getMessage()) ;
            response = new ErrorResponse(responseMessage);

        }
        else{
            responseMessage = new ResponseMessage(200, "success") ;
            response = new DeleteBookingResponse(responseMessage, facilName);
        }
        return response;
    }

    /**
     * Calls ObserverManager's addObserver method with appropriate parameters
     * @param r Request object
     * @param observerManager
     * @param facilTable facility table initialised in Server class
     * @param ip client IP address
     * @param port client port
     * @return RegisterInterestResponse
     */
    public Response addObserver(RegisterInterestRequest r, ObserverManager observerManager, Hashtable facilTable, InetAddress ip, int port){
        Response response;
        ResponseMessage responseMessage;

        String facilName = r.facilityName;
        int numDays = r.numDays;

        Exception outputException = observerManager.addObserver(facilName, numDays, facilTable, ip, port);

        if (outputException != null){
            responseMessage = new ResponseMessage(getStatusCode(outputException), outputException.getMessage()) ;
            response = new ErrorResponse(responseMessage);

        }
        else{
            responseMessage = new ResponseMessage(200, "success") ;
            response = new RegisterInterestResponse(responseMessage);
        }
        return response;
    }

    /**
     * Sends a notification to all clients monitoring a facility's availability
     * @param facilName name of the facility
     * @param bookingManager
     * @param observerManager
     * @param facilTable facility table initialised in Server class
     * @return
     */
    public Exception notifyObservers(String facilName,BookingManager bookingManager, ObserverManager observerManager, Hashtable facilTable){

        Exception e =null;

        ArrayList<FacilityObserver> observers = observerManager.getObservers(facilName, facilTable);

        QueryAvailabilityRequest request = new QueryAvailabilityRequest(facilName, new ArrayList<Day>(Arrays.asList(Day.values())));
        Response response = this.queryAvailability(request, bookingManager, facilTable);

        for (FacilityObserver o : observers){
            InetAddress ip = o.getIp();
            int port = o.getPort();
            try{
                serverCommunicator.send(response, ip, port, false);
            }catch(Exception newE){
                e = newE;
            }

        }

        return e;
    }


    /*---------------------Helper Functions-----------------------------------------------*/

    /**
     * Helper method to convert java's LocalTime object to custom Datetime object
     * @param localTime
     * @param day
     * @return
     */
    public Datetime localToDatetime(LocalTime localTime, Day day){
        Datetime datetime = new Datetime(day, localTime.getHour(), localTime.getMinute());
        return datetime;

    }

    /**
     * Helper method to convert java's LocalTime object to custom datetime object in list of computed availabilities
     * @param resultsTable
     * @return
     */
    public ArrayList localToDatetime(Hashtable resultsTable) {

        ArrayList allConvertedAvailability = new ArrayList();

        for (Day day: Day.values()){
            ArrayList dayResults = new ArrayList();
            if (!resultsTable.containsKey(day)){
                allConvertedAvailability.add(dayResults);
                continue;
            }
            ArrayList availabilityList = (ArrayList) resultsTable.get(day);
            for (ArrayList slot : (ArrayList<ArrayList>) availabilityList) {
                ArrayList newSlot = new ArrayList();
                LocalTime start = (LocalTime) slot.get(0);
                LocalTime end = (LocalTime) slot.get(1);

                Datetime newStart = new Datetime(day, start.getHour(), start.getMinute());
                Datetime newEnd = new Datetime(day, end.getHour(), end.getMinute());

                newSlot.add(newStart);
                newSlot.add(newEnd);

                dayResults.add(newSlot);
            }
            allConvertedAvailability.add(dayResults);


        }
        return allConvertedAvailability;
    }

    /**
     * Helper method to get status code from exception
     * @param e exception thrown by the Managers
     * @return
     */
    public int getStatusCode(Exception e){

        int statusCode = 400;
        String message = e.getMessage();
        switch (message){
            case "Facility does not exist":
                statusCode = 401;
                break;
            case "Booking does not exist":
                statusCode = 402;
                break;
            case "Timeslot is not available":
                statusCode = 403;
                break;

            case "Start time must be before end time":
                statusCode = 404;
                break;
            case "Start time is before 08:00 or End time is after 22:00":
                statusCode = 405;
                break;
            case "Observer already exists":
                statusCode = 406;
                break;
            default:
                statusCode = 400;

        }
        return statusCode;

    }



}


