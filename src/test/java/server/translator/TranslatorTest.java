package server.translator;

import commons.requests.*;
import commons.responses.*;
import commons.utils.Datetime;
import commons.utils.Day;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.entities.Booking;
import server.entities.Facility;
import server.entities.FacilityObserver;
import server.managers.BookingManager;
import server.managers.ObserverManager;


import java.awt.print.Book;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TranslatorTest {


    BookingManager bookingManager;
    Hashtable<String, Facility> facilTable;
    Translator translator;
    ObserverManager observerManager;

    @BeforeEach
    void setUp() {

        facilTable = new Hashtable<>();
        bookingManager = new BookingManager();
        translator = new Translator(null);
        observerManager = new ObserverManager();

        //Populate facilityList
        Facility badmintonCourt = new Facility("badmintonCourt");
        Facility gym = new Facility("gym");
        facilTable.put("badmintoncourt", badmintonCourt);
        facilTable.put("gym", gym);

        //Populate booking
        LocalTime startOne = LocalTime.of(14, 00);
        LocalTime endOne = LocalTime.of( 16, 00);
        Booking bookingOne = new Booking(Day.Tuesday,1, "badmintoncourt", startOne, endOne);

        LocalTime startTwo = LocalTime.of(20, 00);
        LocalTime endTwo = LocalTime.of(22, 00);
        Booking bookingTwo = new Booking(Day.Tuesday, 2, "badmintoncourt", startTwo, endTwo);

        Facility targetFacil = facilTable.get("badmintoncourt");
        targetFacil.addBooking(bookingOne);
        targetFacil.addBooking(bookingTwo);

        //Populate observers
        String facilNameOne = "badmintoncourt";
        LocalDateTime endDateOne = LocalDateTime.now().plusMinutes(30);
        InetAddress ipOne = null;
        int portOne = 21;
        try {
            ipOne = InetAddress.getByName("127.0.0.1");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        FacilityObserver observerOne = new FacilityObserver(facilNameOne, endDateOne, ipOne, portOne);

        String facilNameTwo = "badmintoncourt";
        LocalDateTime endDateTwo = LocalDateTime.now().plusMinutes(-30);
        InetAddress ipTwo = null;
        int portTwo = 22;
        try {
            ipOne = InetAddress.getByName("127.0.0.1");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        FacilityObserver observerTwo = new FacilityObserver(facilNameTwo, endDateTwo, ipTwo, portTwo);

        targetFacil.addObserver(observerOne);
        targetFacil.addObserver(observerTwo);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void queryAvailability_NoConflict_NoException() {

        String facilName = "badmintoncourt";
        ArrayList expectedInterval = new ArrayList();
        expectedInterval.addAll(Arrays.asList(8,22,8,14,16,20));
        ArrayList outputInterval = new ArrayList<>();

        List<Day> days = new ArrayList<>();
        days.add(Day.Monday);
        days.add(Day.Tuesday);

        QueryAvailabilityRequest request = new QueryAvailabilityRequest(facilName, days);

        Response response = translator.queryAvailability(request, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.QueryAvailabilityResponse");
        List availabilities =  ((QueryAvailabilityResponse) response).intervals;

        for (ArrayList dayAvail: (ArrayList<ArrayList>) availabilities){
            for (ArrayList slot: (ArrayList<ArrayList>) dayAvail){
                Datetime start = (Datetime) slot.get(0);
                Datetime end = (Datetime) slot.get(1);
                outputInterval.add(start.hour);
                outputInterval.add(end.hour);

            }
        }
        assertEquals(expectedInterval, outputInterval);
    }

    @Test
    void queryAvailability_NoFacil_Exception() {

        String facilName = "fake";
        ArrayList expectedInterval = new ArrayList();
        ArrayList outputInterval = new ArrayList<>();

        List<Day> days = new ArrayList<>();
        days.add(Day.Monday);
        days.add(Day.Tuesday);

        QueryAvailabilityRequest request = new QueryAvailabilityRequest(facilName, days);

        Response response = translator.queryAvailability(request, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.ErrorResponse");
        String message = ((ErrorResponse) response).responseMessage.message;
        int statusCode = ((ErrorResponse) response).responseMessage.statusCode;
        assertEquals(statusCode, 401);
        assertEquals(message, "Facility does not exist");

    }

    @Test
    void createBooking_NoConflict_NoException(){

        int bookingId = 100;
        int clientId = 22;

        String facilName = "badmintoncourt";
        Day day = Day.Wednesday;
        Datetime start = new Datetime(day,11,30 );
        Datetime end = new Datetime(day,13,30 );

        BookFacilityRequest request = new BookFacilityRequest(facilName, start, end);

        Response response = translator.createBooking(request, bookingId, clientId, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.BookFacilityResponse");
        assertEquals(start.hour, ((BookFacilityResponse) response).startTime.hour);
        assertEquals(start.minute, ((BookFacilityResponse) response).startTime.minute);
        assertEquals(end.hour, ((BookFacilityResponse) response).endTime.hour);
        assertEquals(end.minute, ((BookFacilityResponse) response).endTime.minute);

        assertEquals("success", ((BookFacilityResponse) response).responseMessage.message);


    }

    @Test
    void createBooking_Conflict_Exception(){

        int bookingId = 100;
        int clientId = 22;

        String facilName = "badmintoncourt";
        Day day = Day.Tuesday;
        Datetime start = new Datetime(day,13,30 );
        Datetime end = new Datetime(day,14,30 );

        BookFacilityRequest request = new BookFacilityRequest(facilName, start, end);

        Response response = translator.createBooking(request, bookingId, clientId, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.ErrorResponse");
        assertEquals("Timeslot is not available", ((ErrorResponse) response).responseMessage.message);
        int statusCode = ((ErrorResponse) response).responseMessage.statusCode;
        assertEquals(statusCode, 403);

    }

    @Test
    void createBooking_StartTimeTooEarly_Exception(){

        int bookingId = 100;
        int clientId = 22;

        String facilName = "badmintoncourt";
        Day day = Day.Tuesday;
        Datetime start = new Datetime(day,7,30 );
        Datetime end = new Datetime(day,14,30 );

        BookFacilityRequest request = new BookFacilityRequest(facilName, start, end);

        Response response = translator.createBooking(request, bookingId, clientId, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.ErrorResponse");
        assertEquals("Start time is before 08:00 or End time is after 22:00", ((ErrorResponse) response).responseMessage.message);
        int statusCode = ((ErrorResponse) response).responseMessage.statusCode;
        assertEquals(statusCode, 405);

    }

    @Test
    void createBooking_StartTimeAfterEnd_Exception(){

        int bookingId = 100;
        int clientId = 22;

        String facilName = "badmintoncourt";
        Day day = Day.Tuesday;
        Datetime start = new Datetime(day,13,30 );
        Datetime end = new Datetime(day,12,30 );

        BookFacilityRequest request = new BookFacilityRequest(facilName, start, end);

        Response response = translator.createBooking(request, bookingId, clientId, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.ErrorResponse");
        assertEquals("Start time must be before end time", ((ErrorResponse) response).responseMessage.message);
        int statusCode = ((ErrorResponse) response).responseMessage.statusCode;
        assertEquals(statusCode, 404);

    }

    @Test
    void offsetBooking_NoConflict_NoException(){

        int bookingId = 1;
        int clientId = 100;
        int offset = 1;
        String facilName = "badmintoncourt";


        OffsetBookingRequest request = new OffsetBookingRequest(bookingId, facilName, offset);

        Response response = translator.offsetBooking(request, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.OffsetBookingResponse");
        assertEquals(14, ((OffsetBookingResponse) response).startTime.hour);
        assertEquals(30, ((OffsetBookingResponse) response).startTime.minute);
        assertEquals(16, ((OffsetBookingResponse) response).endTime.hour);
        assertEquals(30, ((OffsetBookingResponse) response).endTime.minute);
    }

    @Test
    void offsetBooking_Conflict_Exception(){

        //Populate booking
        LocalTime startOne = LocalTime.of(16, 00);
        LocalTime endOne = LocalTime.of( 17, 00);
        Booking bookingOne = new Booking(Day.Tuesday,3, "badmintoncourt", startOne, endOne);
        Facility targetFacil = facilTable.get("badmintoncourt");
        targetFacil.addBooking(bookingOne);


        int bookingId = 1;
        int clientId = 100;
        int offset = 1;
        String facilName = "badmintoncourt";


        OffsetBookingRequest request = new OffsetBookingRequest(bookingId, facilName, offset);

        Response response = translator.offsetBooking(request, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.ErrorResponse");
        assertEquals("Timeslot is not available", ((ErrorResponse) response).responseMessage.message);

        int statusCode = ((ErrorResponse) response).responseMessage.statusCode;
        assertEquals(statusCode, 403);
    }


    @Test
    void offsetBooking_NoBookingNoFacil_Exception(){
        int bookingId = 100;
        int clientId = 100;
        int offset = 1;
        String facilName = "pop";

        OffsetBookingRequest request = new OffsetBookingRequest(bookingId, facilName, offset);

        Response response = translator.offsetBooking(request, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.ErrorResponse");
    }


    @Test
    void extendBooking_NoConflict_NoException(){

        int bookingId = 1;
        int clientId = 100;
        int extension = 1;
        String facilName = "badmintoncourt";


        ExtendBookingRequest request = new ExtendBookingRequest(bookingId, facilName, extension);

        Response response = translator.extendBooking(request, bookingManager, facilTable);
        assertEquals(response.getClass().getName(),"commons.responses.ExtendBookingResponse");
        assertEquals(14, ((ExtendBookingResponse) response).startTime.hour);
        assertEquals(00, ((ExtendBookingResponse) response).startTime.minute);
        assertEquals(16, ((ExtendBookingResponse) response).endTime.hour);
        assertEquals(30, ((ExtendBookingResponse) response).endTime.minute);
    }

    @Test
    void extendBooking_Conflict_Exception(){

        //Populate booking
        LocalTime startOne = LocalTime.of(16, 00);
        LocalTime endOne = LocalTime.of( 17, 00);
        Booking bookingOne = new Booking(Day.Tuesday,3, "badmintoncourt", startOne, endOne);
        Facility targetFacil = facilTable.get("badmintoncourt");
        targetFacil.addBooking(bookingOne);

        int bookingId = 1;
        int clientId = 100;
        int extension = 1;
        String facilName = "badmintoncourt";


        ExtendBookingRequest request = new ExtendBookingRequest(bookingId, facilName, extension);

        Response response = translator.extendBooking(request, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.ErrorResponse");
        assertEquals("Timeslot is not available", ((ErrorResponse) response).responseMessage.message);
    }

    @Test
    void extendBooking_NoBookingNoFacil_Exception(){
        int bookingId = 100;
        int clientId = 100;
        int offset = 1;
        String facilName = "pop";

        ExtendBookingRequest request = new ExtendBookingRequest(bookingId, facilName, offset);

        Response response = translator.extendBooking(request, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.ErrorResponse");
        assertEquals("Facility does not exist",((ErrorResponse)response).responseMessage.message);
    }

    @Test
    void deleteBooking_NoConflict_NoException(){
        int bookingId = 1;
        int clientId = 100;
        String facilName = "badmintoncourt";

        DeleteBookingRequest request = new DeleteBookingRequest(bookingId, facilName);

        Response response = translator.deleteBooking(request, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.DeleteBookingResponse");
        assertEquals("success", ((DeleteBookingResponse) response).responseMessage.message);
    }

    @Test
    void deleteBooking_WrongId_Exception(){
        int bookingId = 3;
        int clientId = 100;
        String facilName = "badmintoncourt";

        DeleteBookingRequest request = new DeleteBookingRequest(bookingId, facilName);

        Response response = translator.deleteBooking(request, bookingManager, facilTable);
        assertEquals(response.getClass().getName(), "commons.responses.ErrorResponse");
        assertEquals("Booking does not exist", ((ErrorResponse) response).responseMessage.message);
    }

    @Test
    void addObserver_NoConflict_NoException(){
        String facilNameOne = "badmintoncourt";
        Facility facil = facilTable.get(facilNameOne);
        int numDays = 3;
        InetAddress ipOne = null;
        int portOne = 22;
        try {
            ipOne = InetAddress.getByName("127.0.0.1");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        RegisterInterestRequest request = new RegisterInterestRequest(facilNameOne, numDays);

        Response response = translator.addObserver(request, observerManager, facilTable, ipOne, portOne);
        assertEquals(response.getClass().getName(), "commons.responses.RegisterInterestResponse");
        assertEquals("success", ((RegisterInterestResponse) response).responseMessage.message);
    }

    @Test
    void addObserver_SameObserver_Exception(){
        String facilNameOne = "badmintoncourt";
        Facility facil = facilTable.get(facilNameOne);
        int numDays = 3;
        InetAddress ipOne = null;
        int portOne = 22;
        try {
            ipOne = InetAddress.getByName("127.0.0.1");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        RegisterInterestRequest request = new RegisterInterestRequest(facilNameOne, numDays);


        Response response = translator.addObserver(request, observerManager, facilTable, ipOne, portOne);
        response = translator.addObserver(request, observerManager, facilTable, ipOne, portOne);
        assertEquals(response.getClass().getName(), "commons.responses.ErrorResponse");
        assertEquals("Observer already exists", ((ErrorResponse) response).responseMessage.message);
    }

    @Test
    void notifyObservers_wip(){

        String facilName = "badmintoncourt";


        translator.notifyObservers(facilName, bookingManager, observerManager, facilTable);
    }


}