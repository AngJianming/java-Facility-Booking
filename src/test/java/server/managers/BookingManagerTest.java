package server.managers;

import commons.utils.Day;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.entities.Booking;
import server.entities.Facility;


import java.time.*;
import java.util.*;
import javafx.util.Pair;

import static org.junit.jupiter.api.Assertions.*;

class BookingManagerTest {

    BookingManager bookingManager;
    Hashtable<String, Facility> facilTable;

    @BeforeEach
    void setUp() {

        facilTable = new Hashtable<>();
        bookingManager = new BookingManager();

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


    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void queryAvailability_NoConflict_NoException() {
        Day expectedDay = Day.Tuesday;
        ArrayList<Day> queryDays = new ArrayList<Day>();
        queryDays.add(expectedDay);
        Pair<Hashtable, Exception> queryResults = bookingManager.queryAvailability("badmintoncourt", queryDays, facilTable);
        Hashtable outputBookings = queryResults.getKey();
        ArrayList outputBookingsList =(ArrayList) outputBookings.get(expectedDay);
        Exception outputException = queryResults.getValue();

        System.out.println(outputBookings);

        ArrayList expectedBookingsList = new ArrayList<>();
        ArrayList<LocalTime> slotOne = new ArrayList<>();
        ArrayList<LocalTime> slotTwo = new ArrayList<>();
        slotOne.add(LocalTime.of(8, 0));
        slotOne.add(LocalTime.of(14, 0));
        slotTwo.add(LocalTime.of(16, 0));
        slotTwo.add(LocalTime.of(20, 0));
        expectedBookingsList.add(slotOne);
        expectedBookingsList.add(slotTwo);
        Exception expectedException = null;


        assertEquals(outputBookingsList, expectedBookingsList);
        assertEquals(outputException, expectedException);
        assertEquals(1, outputBookings.size());
    }



    @Test
    void createBooking_NoConflict_NoException() {
        int bookingId = 1;
        int clientId = 100;
        Day day = Day.Tuesday;
        String facilName = "badmintoncourt";

        LocalTime startThree = LocalTime.of( 18, 00);
        LocalTime endThree = LocalTime.of(20, 00);

        Pair<Booking, Exception> createBookingResults = bookingManager.createBooking(day, bookingId, clientId, facilName, startThree, endThree, facilTable);
        Booking outputBooking = createBookingResults.getKey();
        Exception outputException = createBookingResults.getValue();

        assertEquals(outputException, null);
        assertEquals(outputBooking.getBookingId(), bookingId);
        assertEquals(outputBooking.getFacilityName(), facilName);
        assertEquals(outputBooking.getStart(), startThree);
        assertEquals(outputBooking.getEnd(), endThree);
    }

    @Test
    void createBooking_Conflict_Exception() {
        int bookingId = 1;
        int clientId = 100;
        Day day = Day.Tuesday;
        String facilName = "badmintoncourt";

        LocalTime startThree = LocalTime.of(19, 00);
        LocalTime endThree = LocalTime.of( 21, 00);

        Pair<Booking, Exception> createBookingResults = bookingManager.createBooking(day, bookingId, clientId, facilName, startThree, endThree, facilTable);
        Booking outputBooking = createBookingResults.getKey();
        Exception outputException = createBookingResults.getValue();

        assertEquals(NoSuchElementException.class, outputException.getClass() );
        assertEquals("Timeslot is not available", outputException.getMessage());
    }


    @Test
    void createBooking_TooEarly_Exception() {

    }

    @Test
    void offsetBooking_NoConflict_NoException(){
        int bookingId = 1;
        int clientId = 100;
        int offset = 30;
        String facilName = "badmintoncourt";


        Pair<Booking, Exception> results =  bookingManager.offsetBooking(facilName, bookingId, offset, this.facilTable);
        Booking outputBooking = results.getKey();
        Exception outputException = results.getValue();

        assertEquals(outputBooking.getStart(), LocalTime.of(14, 30));
        assertEquals(outputBooking.getEnd(), LocalTime.of( 16, 30));
        assertEquals(outputBooking.getBookingId(), bookingId);

    }

    @Test
    void offsetBooking_Conflict_Exception(){
        int bookingId = 1;
        int clientId = 100;
        int offset = 270;
        String facilName = "badmintoncourt";

        Pair<Booking, Exception> results =  bookingManager.offsetBooking(facilName, bookingId, offset, this.facilTable);
        Booking outputBooking = results.getKey();
        Exception outputException = results.getValue();

        assertEquals(outputException.getMessage(), "Timeslot is not available");

    }

    @Test
    void extendBooking_NoConflict_NoException(){
        int bookingId = 1;
        int clientId = 100;
        int offset = 30;
        String facilName = "badmintoncourt";

        Pair<Booking, Exception> results =  bookingManager.extendBooking(facilName, bookingId, offset, this.facilTable);
        Booking outputBooking = results.getKey();
        Exception outputException = results.getValue();

        assertEquals(LocalTime.of( 14, 00), outputBooking.getStart());
        assertEquals(LocalTime.of(16, 30),outputBooking.getEnd());
        assertEquals(null, outputException);

    }

    @Test
    void extendBooking_TooLate_Exception(){
        int bookingId = 2;
        int clientId = 100;
        int offset = 30;
        String facilName = "badmintoncourt";

        Pair<Booking, Exception> results =  bookingManager.extendBooking(facilName, bookingId, offset, this.facilTable);
        Booking outputBooking = results.getKey();
        Exception outputException = results.getValue();

        assertEquals(String.format("Start time is before %s or End time is after %s", bookingManager.getOpenTime().toString(), bookingManager.getCloseTime().toString()), outputException.getMessage());

    }



    @Test
    void deleteBooking_NoConflict_NoException(){
        int bookingId = 1;
        int clientId = 100;
        String facilName = "badmintoncourt";

        Exception outputException =  bookingManager.deleteBooking(facilName, bookingId, this.facilTable);
        assertEquals(null, outputException);
    }

    @Test
    void deleteBooking_WrongId_Exception(){
        int bookingId = 3;
        int clientId = 100;
        String facilName = "badmintoncourt";

        Exception outputException =  bookingManager.deleteBooking(facilName, bookingId, this.facilTable);
        assertEquals("Booking does not exist", outputException.getMessage());
    }

}

