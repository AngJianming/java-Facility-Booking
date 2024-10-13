package server.entities;

import commons.utils.Day;
import server.managers.*;

import java.time.*;
import java.util.*;

/**
 * Represents a facility, holds a table of bookings keyed by day
 * and a list of clients who registered interest for the facility
 */
public class Facility {
    private ArrayList<FacilityObserver> observerList = new ArrayList<FacilityObserver>();
    private String name;
    private Hashtable<Day, ArrayList<Booking>> bookingsTable = new Hashtable<Day, ArrayList<Booking>>();


    public Facility(String name){
        this.name = name;
    }

    /**
     * Adds a booking to the bookingsTable
     * @param booking
     */
    public void addBooking(Booking booking){

        //Check
        LocalTime startTime = booking.getStart();
        LocalTime endTime = booking.getEnd();
        Day day = booking.getDay();

        // All timing related checks are done in booking manager

        if (this.bookingsTable.containsKey(day)){
            this.bookingsTable.get(day).add(booking);
        }
        else{
            ArrayList<Booking> newList = new ArrayList<>();
            newList.add(booking);
            this.bookingsTable.put(day, newList);
        }

    }

    /**
     * Removes a booking from the bookingsTable
     * @param booking
     */
    public void removeBooking(Booking booking){
        Day bookingDay = booking.getDay();
        ArrayList bookingsList = this.bookingsTable.get(bookingDay);
        for (int i =0; i< bookingsList.size(); i++){
            Booking b = (Booking) bookingsList.get(i);
            if (b.getBookingId() == booking.getBookingId()){
                bookingsList.remove(b);
//                return null;
            }
        }
//        Exception e = new NoSuchElementException("Unable to find booking to remove");
//        return e;
    }

    /**
     * Offsets a booking
     * @param booking
     * @param newStart
     * @param newEnd
     */
    public void offsetBooking(Booking booking, LocalTime newStart, LocalTime newEnd){
        Day bookingDay = booking.getDay();
        ArrayList bookingsList = this.bookingsTable.get(bookingDay);
        for (int i =0; i< bookingsList.size(); i++){
            Booking b = (Booking) bookingsList.get(i);
            if (b.getBookingId() == booking.getBookingId()){
                b.setStart(newStart);
                b.setEnd(newEnd);
            }
        }
    }

    /**
     * Extends the end time of a booking
     * @param booking
     * @param newEnd
     */
    public void extendBooking(Booking booking, LocalTime newEnd){
        Day bookingDay = booking.getDay();
        ArrayList bookingsList = this.bookingsTable.get(bookingDay);
        for (int i =0; i< bookingsList.size(); i++){
            Booking b = (Booking) bookingsList.get(i);
            if (b.getBookingId() == booking.getBookingId()){
                b.setEnd(newEnd);
            }
        }
    }

    /**
     * Aads an observer for this facility
     * @param o
     */
    public void addObserver(FacilityObserver o){
        observerList.add(o);
    }

    /**
     * Updates list of observers
     * @return
     */
    public ArrayList<FacilityObserver> getUpdatedObservers(){

        ArrayList<FacilityObserver> observersToRemove = new ArrayList<>();

        for (FacilityObserver o: observerList){
            if (!isValidObserver(o)){
                observersToRemove.add(o);
            }
        }

        for (FacilityObserver o: observersToRemove){
            observerList.remove(o);
        }

        return observerList;
    }

    /**
     * Checks if valid observer is valid
     * @param o
     * @return
     */
    private Boolean isValidObserver(FacilityObserver o){

        if (o.getEndDate().compareTo(LocalDateTime.now()) <0 ){
            return false;
        }
        else{
            return true;
        }

    }

    /**
     * Retrieves booking by day
     * @param day
     * @return
     */
    public ArrayList<Booking> getBookingsByDay(Day day){
        return this.bookingsTable.get(day);
    }

    /**
     * Retrieves booking by ID
     * @param bookingId
     * @return
     */
    public Booking getBookingById (int bookingId){
        Enumeration<ArrayList<Booking>> allbookings = this.bookingsTable.elements();

        //iterate the values
        while(allbookings.hasMoreElements() ){
            ArrayList<Booking> bookingsList = allbookings.nextElement();
            for (Booking b:bookingsList){
                if (b.getBookingId() == bookingId){
                    return b;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves bookings table
     * @return
     */
    public Hashtable getBookingsTable(){
        return this.bookingsTable;
    }

}
