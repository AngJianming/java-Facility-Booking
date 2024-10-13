package server.managers;

import commons.utils.Day;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.entities.Booking;
import server.entities.Facility;
import server.entities.FacilityObserver;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.*;

class ObserverManagerTest {

    BookingManager bookingManager;
    Hashtable<String, Facility> facilTable;
    ObserverManager observerManager;

    @BeforeEach
    void setUp() {

        facilTable = new Hashtable<>();
        bookingManager = new BookingManager();
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
        int portOne = 22;
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



    @Test
    void getObservers_NoConflict_NoException(){
        String facilName = "badmintoncourt";
        InetAddress expectedIp = null;
        int expectedPort = 22;
        try {
            expectedIp = InetAddress.getByName("127.0.0.1");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        ArrayList outputObservers = observerManager.getObservers(facilName, facilTable);
        FacilityObserver o =  (FacilityObserver) outputObservers.get(0);
        InetAddress oIp = o.getIp();
        int oPort = o.getPort();

        assertEquals(1, outputObservers.size());
        assertEquals(expectedIp, oIp);
        assertEquals(expectedPort, oPort);
    }
}