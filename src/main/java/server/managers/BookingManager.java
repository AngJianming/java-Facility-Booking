package server.managers;

import commons.utils.Day;
import server.entities.*;

import java.util.*;
import java.time.*;
import javafx.util.Pair;

/**
 * Manages all booking-related queries, which include
 * computing availability from list of bookings, creating,
 * extending, offsetting and deleting of bookings.
 */
public class BookingManager {

    private static LocalTime openTime = LocalTime.of(8,0,0);
    private static LocalTime closeTime = LocalTime.of(22, 0,0);

    public static LocalTime getOpenTime() {
        return openTime;
    }

    public static LocalTime getCloseTime() {
        return closeTime;
    }

    /**
     * Returns the list of availabilities for a particular facility
     * @param facilName name of facility
     * @param dates days to query
     * @param facilTable facility table
     * @return pair of hashtable keyed on day of the week with list of availabilities, and exception to be returned to the user
     */
    public Pair<Hashtable, Exception> queryAvailability(String facilName, ArrayList<Day> dates, Hashtable facilTable){

        Exception e;
        Hashtable allResults = new Hashtable();
        Facility currFacil = (Facility) facilTable.get(facilName);

        //Checks
        e= doValidFacilCheck(facilName, facilTable);
        if (e!=null){
            return new Pair<Hashtable, Exception>(null, e);
        }

        for (int j=0; j < dates.size();j++){
            Day currDay = dates.get(j);
            ArrayList dayResults = new ArrayList<>();

            if(!currFacil.getBookingsTable().containsKey(currDay)){

                ArrayList slot = new ArrayList();
                slot.add(openTime);
                slot.add(closeTime);
                dayResults.add(slot);
                allResults.put(currDay, dayResults);
                continue;
            }

            ArrayList<Booking> bookingList = currFacil.getBookingsByDay(currDay);

            this.sortBookings(bookingList);
            LocalTime availStart = openTime;
            LocalTime availEnd = closeTime;
            ArrayList<LocalTime> slot;

            for(int k=0; k < bookingList.size();k++){
                Booking currBooking = bookingList.get(k);
                availEnd = currBooking.getStart();

                Boolean check1 = availEnd.compareTo(openTime) == 0;
                Boolean check2 = availEnd.compareTo(availStart) ==0;
                if(check1 || check2 ){
                    availStart = currBooking.getEnd();
                    continue;
                }

                slot = new ArrayList<LocalTime>();
                slot.add(availStart);
                slot.add(availEnd);
                dayResults.add(slot);

                availStart = currBooking.getEnd();
            }

            // if the last booking ends at closing time
            if(availStart.compareTo(closeTime) == 0){
                allResults.put(currDay, dayResults);
                continue;
            }
            else{
                slot = new ArrayList<LocalTime>();
                slot.add(availStart);
                slot.add(closeTime);
                dayResults.add(slot);
                allResults.put(currDay, dayResults);
            }
        }
        return new Pair<Hashtable, Exception>(allResults, null);
    }

    /**
     * Creates a booking
     * @param day day of the week
     * @param bookingId identifier of booking
     * @param clientId identifier of client
     * @param facilName name of facility
     * @param newStart start time of booking
     * @param newEnd end time of booking
     * @param facilTable facility table
     * @return Pair of Booking object and exception (if any error is thrown)
     */
    public Pair<Booking, Exception> createBooking(Day day, int bookingId, int clientId, String facilName, LocalTime newStart, LocalTime newEnd, Hashtable facilTable){

        Exception e;

        Facility f = (Facility) facilTable.get(facilName);
        e = doValidFacilCheck(facilName, facilTable);
        if(e != null){
            return new Pair<Booking, Exception>(null, e);
        }


        e = doBookingCheck(facilName,newStart,newEnd,facilTable);
        if (e != null){
            return new Pair<Booking, Exception>(null, e);
        }
        e = doAvailabilityCheck(day, facilName,newStart,newEnd,facilTable);
        if (e != null){
            return new Pair<Booking, Exception>(null, e);
        }

        //Booking procedure
        Booking b = new Booking(day, bookingId, facilName, newStart, newEnd);

        try{
            f.addBooking(b);
        }catch (Exception unexpectedE){
            return new Pair<>(null,unexpectedE);
        }

        return new Pair<Booking, Exception>(b, null);
    }

    /**
     * Offset a booking
     * @param facilName name of the facility
     * @param bookingId identifier of the booking
     * @param offset number of slots to offset by
     * @param facilTable facility table
     * @return Pair of updated booking object and exception (if any error is thrown)
     */
    public Pair<Booking, Exception> offsetBooking(String facilName, int bookingId, int offset, Hashtable facilTable){
        Exception e;
        Facility f = (Facility) facilTable.get(facilName);
        e = doValidFacilCheck(facilName, facilTable);
        if(e != null){
            return new Pair<Booking, Exception>(null, e);
        }

        Booking b = this.findFacilBookingById(facilName, bookingId, facilTable);
        e = doBookingNotNullCheck(b);
        if(e != null){
            return new Pair<Booking, Exception>(null, e);
        }

        Day day = b.getDay();
        LocalTime newStart = b.getStart().plusMinutes(offset);
        LocalTime newEnd = b.getEnd().plusMinutes(offset);
        e = doBookingCheck(facilName,newStart,newEnd,facilTable);
        if (e != null){
            return new Pair<Booking, Exception>(null, e);
        }
        e = doAvailabilityCheckExceptCurrent(day, bookingId, facilName,newStart,newEnd,facilTable);
        if (e != null){
            return new Pair<Booking, Exception>(null, e);
        }

        f.offsetBooking(b, newStart, newEnd);

        return new Pair<Booking, Exception>(b, null);

    }

    /**
     * Extend a booking
     * @param facilName name of the facility
     * @param bookingId identifier of the booking
     * @param offset number of slots to offset by
     * @param facilTable facility table
     * @return Pair of updated booking object and exception (if any error is thrown)
     */
    public Pair<Booking, Exception> extendBooking(String facilName, int bookingId, int offset, Hashtable facilTable){
        Exception e;
        Facility f = (Facility) facilTable.get(facilName);
        e = doValidFacilCheck(facilName, facilTable);
        if(e != null){
            return new Pair<Booking, Exception>(null, e);
        }

        Booking b = findFacilBookingById(facilName, bookingId, facilTable);
        e = doBookingNotNullCheck(b);
        if(e != null){
            return new Pair<Booking, Exception>(null, e);
        }

        LocalTime bStart = b.getStart();
        LocalTime bEnd = b.getEnd();
        LocalTime newEnd = bEnd.plusMinutes(offset);
        Day day = b.getDay();
        e = doBookingCheck(facilName,bStart,newEnd,facilTable);
        if (e != null){
            return new Pair<Booking, Exception>(null, e);
        }
        e = doAvailabilityCheckExceptCurrent(day, bookingId, facilName,bStart,newEnd,facilTable);
        if (e != null){
            return new Pair<Booking, Exception>(null, e);
        }

        f.extendBooking(b, newEnd);
        return new Pair<Booking, Exception>(b, null);
    }

    /**
     * Deletes a booking
     * @param facilName name of the facility
     * @param bookingId identifier of booking
     * @param facilTable facility table
     * @return exception (if error is thrown, else nil)
     */
    public Exception deleteBooking(String facilName, int bookingId, Hashtable facilTable){

        Exception e;

        Facility facil =(Facility) facilTable.get(facilName);
        e = doValidFacilCheck(facilName, facilTable);
        if(e != null){
            return e;
        }

        Booking b = findFacilBookingById(facilName, bookingId, facilTable);

        e = doBookingNotNullCheck(b);
        if(e != null){
            return e;
        }

        try {
            facil.removeBooking(b);
        }
        catch (Exception unexpectedE){
            return unexpectedE;
        }
        return e;
    }

/*------------------------------------- Helper Functions ----------------------------------------------------------------------*/

    /**
     * Helper method to sort bookings
     * @param bookingsList list of bookings
     */
    public void sortBookings(ArrayList<Booking> bookingsList){
        Collections.sort(bookingsList, Booking.BookingComparator);
    }

    /**
     * Helper method to get Booking object by ID and facility name
     * @param facilName name of the facility
     * @param bookingId identifier of booking
     * @param facilTable facility table
     * @return booking object
     */
    private Booking findFacilBookingById(String facilName, int bookingId, Hashtable facilTable){
        Facility facil = (Facility) facilTable.get(facilName);
        Booking b = facil.getBookingById(bookingId);
        return b;
    }

    /**
     * Helper method to check if facility exists
     * @param facilName name of the facility
     * @param facilTable facility table
     * @return exception if facility name is invalid, else null
     */
    private Exception doValidFacilCheck(String facilName, Hashtable facilTable){
        if (!facilTable.containsKey(facilName)){
            Exception e = new NoSuchElementException("Facility does not exist");
            return e;
        }
        else{
            return null;
        }
    }

    /**
     * Helper method to check if booking exists
     * @param b booking object
     * @return exception if booking does not exist, else null
     */
    private Exception doBookingNotNullCheck(Booking b){
        if(b == null){
            Exception e = new NoSuchElementException("Booking does not exist");
            return e;
        }
        else{
            return null;
        }

    }

    /**
     * Helper method to check if new booking does not overlap with existing bookings, for
     * @param day day of the week
     * @param facilName name of the facility
     * @param newStart start time of new booking
     * @param newEnd end time of new booking
     * @param facilTable facility table
     * @return exception if slot is not available, else null
     */
    private Exception doAvailabilityCheck(Day day, String facilName, LocalTime newStart, LocalTime newEnd, Hashtable facilTable){
        //Check if timeslot is available
        ArrayList<Day> queryDates = new ArrayList<>();
        queryDates.add(day);


        Pair<Hashtable, Exception> queryResults = this.queryAvailability(facilName, queryDates, facilTable);
        Hashtable queryTable = queryResults.getKey();
        ArrayList availStartEnd = (ArrayList) queryTable.get(day);

        boolean isAvailable = false;
        for (int i =0; i< availStartEnd.size(); i++){
            ArrayList startEnd = (ArrayList) availStartEnd.get(i);
            LocalTime start = (LocalTime) startEnd.get(0);
            LocalTime end = (LocalTime) startEnd.get(1);

            Boolean check1 = (newStart.compareTo(start)>=0) && (newEnd.compareTo(end)<=0);
            if (check1){
                isAvailable = true;
            }
        }

        if(isAvailable){
            return null;
        }
        else{
            Exception e = new NoSuchElementException("Timeslot is not available");
            return e;
        }
    }

    /**
     * Helper method to check if updated booking overlaps with existing bookings (for offset/extend use cases)
     * @param day day of the week
     * @param bookingId identifier of booking
     * @param facilName name of the facility
     * @param newStart start time of booking
     * @param newEnd end time of booking
     * @param facilTable facility table
     * @return exception if slot is not available, else null
     */
    private Exception doAvailabilityCheckExceptCurrent(Day day, int bookingId, String facilName, LocalTime newStart, LocalTime newEnd, Hashtable facilTable){


        Facility facil =(Facility) facilTable.get(facilName);
        ArrayList<Booking> bookingsList = facil.getBookingsByDay(day);

        for (Booking b:bookingsList){
            LocalTime bStart = b.getStart();
            LocalTime bEnd = b.getEnd();

            Boolean conditionOne = (newStart.compareTo(bStart) <0) &&  (newEnd.compareTo(bStart)<=0);
            Boolean conditionTwo = (newStart.compareTo(bEnd) >=0) &&  (newEnd.compareTo(bEnd)>0);
            Boolean conditionThree = b.getBookingId() == bookingId;
            if (conditionOne || conditionTwo){
                continue;
            }
            else{
                if (conditionThree){
                    continue;
                }
                else{
                    Exception e = new NoSuchElementException("Timeslot is not available");
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * Helper method to check if booking is valid
     * @param facilName name of the facility
     * @param newStart start time of booking
     * @param newEnd end time of booking
     * @param facilTable facility table
     * @return exception if slot is not available, else null
     */
    private Exception doBookingCheck(String facilName, LocalTime newStart, LocalTime newEnd, Hashtable facilTable){

        if (!isStartBeforeEnd(newStart, newEnd)){
            Exception e = new NoSuchElementException("Start time must be before end time");
            return e;
        }

        if (!this.isValidStartEndTime(newStart, newEnd)){
            Exception e = new NoSuchElementException(String.format("Start time is before %s or End time is after %s", this.openTime.toString(), this.closeTime.toString()));
            return e;
        }

        return null;
    }

    /**
     * Helper method to check if start time is before end time
     * @param newStart start time
     * @param newEnd end time
     * @return boolean
     */
    private Boolean isStartBeforeEnd(LocalTime newStart, LocalTime newEnd){
        if(newStart.compareTo(newEnd) < 0){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Helper method to check if time is within bounds of facility's opening and closing
     * @param newStart start time
     * @param newEnd end time
     * @return boolean
     */
    private Boolean isValidStartEndTime(LocalTime newStart, LocalTime newEnd){
        Boolean conditionOne = newStart.compareTo(this.openTime) <0;
        Boolean conditionTwo = newEnd.compareTo(this.closeTime)>0;
        if (conditionOne || conditionTwo){
            return false;
        }
        else{
            return true;
        }
    }

}

