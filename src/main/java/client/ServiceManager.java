package client;

import commons.exceptions.*;
import commons.requests.*;
import commons.responses.*;
import commons.rpc.ClientCommunicator;
import commons.utils.Datetime;
import commons.utils.ResponseMessage;
import commons.utils.Day;

import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.lang.Math.min;
import static java.lang.Math.toIntExact;

/**
 * Handles all client-side business logic,
 * constructs Request objects to be sent to server and
 * processes Response objects for display to the user.
 */
public class ServiceManager {
    ClientCommunicator router;
    InetAddress serverAddress;
    int serverPort;
    static final List<String> daysOfWeek = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

    /**
     * Constructor for ServiceManager
     * @param router ClientCommunicator object for sending and receving of packets
     * @param serverAddress IP address of server
     * @param serverPort Port number of server
     */
    public ServiceManager(ClientCommunicator router, InetAddress serverAddress, int serverPort) {
        this.router = router;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Sends a request to query for availability for a particular facility over a given list of days
     * @throws InvalidDayException thrown when input day is not a valid day of the week
     */
    public void queryAvailability() throws InvalidDayException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter facility name: ");
        String facilityName = scanner.nextLine();
        System.out.println("Please enter list of days (Monday, Tuesday etc), separated by spaces: ");
        String daysString = scanner.nextLine();

        List<String> days = new ArrayList<>(Arrays.asList(daysString.split(" ")));
        List<Day> daysEnum = new ArrayList<Day>();
        for (String d : days) {
            if (!checkValidDay(d)) {
                throw new InvalidDayException();
            } else {
                daysEnum.add(Day.valueOf(d));
            }
        }
        Request req = new QueryAvailabilityRequest(facilityName, daysEnum);
        request(router, req);
    }

    /**
     * Sends a request to book a particular facility for a given interval
     * @throws InvalidDateFormatException thrown when input date format is invalid (i.e. violate this expression DAY/HH/MM)
     * @throws InvalidDayException thrown when input day is not a valid day of the week
     * @throws InvalidTimeException thrown when hour or minute invalid
     * @throws InvalidIntervalException thrown when the interval provided is invalid
     */
    public void bookFacility() throws InvalidDateFormatException, InvalidDayException, InvalidTimeException, InvalidIntervalException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter facility name: ");
        String facilityName = scanner.nextLine();
        System.out.println("Please enter start day and time [D/HH/MM]: ");
        String startDatetimeStr = scanner.nextLine();
        if (!checkValidDateFormat(startDatetimeStr)) {
            throw new InvalidDateFormatException();
        }
        String[] startDatetimeParts = startDatetimeStr.split("/");

        if (!checkValidDay(startDatetimeParts[0])) {
            throw new InvalidDayException();
        }
        if (!checkValidHour(startDatetimeParts[1]) || !checkValidMinute(startDatetimeParts[2])) {
            throw new InvalidTimeException();
        }
        Datetime startDatetime = getDatetimeFromString(startDatetimeStr);

        System.out.println("Please enter end day and time [D/HH/MM]: ");
        String endDatetimeStr = scanner.nextLine();
        if (!checkValidDateFormat(endDatetimeStr)) {
            throw new InvalidDateFormatException();
        }
        String[] endDatetimeParts = endDatetimeStr.split("/");

        if (!checkValidDay(endDatetimeParts[0])) {
            throw new InvalidDayException();
        }
        if (!checkValidHour(endDatetimeParts[1]) || !checkValidMinute(endDatetimeParts[2])) {
            throw new InvalidTimeException();
        }
        Datetime endDatetime = getDatetimeFromString(endDatetimeStr);

        if (!checkValidStartEnd(startDatetime, endDatetime)) {
            throw new InvalidIntervalException();
        }
        Request req = new BookFacilityRequest(
                facilityName,
                startDatetime,
                endDatetime
            );
        request(router, req);
    }

    /**
     * Sends a request to offset an existing booking
     */
    public void offsetBooking() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter booking ID: ");
        int bookingID = Integer.parseInt(scanner.nextLine());
        System.out.println("Please enter facility name: ");
        String facilityName =scanner.nextLine();
        System.out.println("Please enter offset in terms of number of 30-minute slots. \n" +
                "1 = push back by 30 minutes \n" +
                "2 = push back by 1 hour \n " +
                "-1 = push forward by 30 minutes \n" +
                "and so on. ");
        int offset = Integer.parseInt(scanner.nextLine());

        Request req = new OffsetBookingRequest(bookingID, facilityName, offset);
        request(router, req);
    }

    /**
     * Sends a request to extend the end time of an existing booking
     */
    public void extendBooking() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter booking ID: ");
        int bookingID = Integer.parseInt(scanner.nextLine());
        System.out.println("Please enter facility name: ");
        String facilityName =scanner.nextLine();
        System.out.println("Please enter number of 30-minute slots to extend by: ");
        int extension = scanner.nextInt();

        Request req = new ExtendBookingRequest(
                bookingID,
                facilityName,
                extension
        );
        request(router, req);
    }

    /**
     * Sends a request to delete an existing booking
     */
    public void deleteBooking() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter booking ID: ");
        int bookingID = Integer.parseInt(scanner.nextLine());
        System.out.println("Please enter facility name: ");
        String facilityName =scanner.nextLine();

        Request req = new DeleteBookingRequest(
                bookingID,
                facilityName
        );
        request(router, req);
    }

    /**
     * Sends a request to register interest for availability updates for a particular facility over a given interval
     */
    public void registerInterest() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter facility name: ");
        String facilityName = scanner.nextLine();
        System.out.println("Please enter number of days you want to monitor: ");
        int numDays = scanner.nextInt();

        Request req = new RegisterInterestRequest(facilityName, numDays);
        request(router, req);
        System.out.println("=====================================");
        System.out.println("Monitoring availability for " + facilityName);

        // for demo purposes, we monitor in the magnitude of minutes instead (1 day = 1 minute)
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(numDays);
        while (LocalDateTime.now().isBefore(endTime)) {
            int timeout = toIntExact(LocalDateTime.now().until(endTime, ChronoUnit.MILLIS));
            System.out.println("Time left: " + timeout);
                // expecting QueryAvailabilityResponse; can use the same one because it is essentially a query for availability
            router.setSocketTimeout(timeout); // allow socket to listen for the duration of the listen
            try{
                Response res = router.receive();
                generateResponse(res);
//                ResponseMessage responseMessage = new ResponseMessage(200, "success");
//                AcknowledgementRequest reply = new AcknowledgementRequest(responseMessage);
//                router.sendRequest(reply);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof SocketTimeoutException) {
                    System.out.println("Listening Duration Over");
                }
            }
        }
        router.setSocketTimeout(router.socketTimeout); //reset socket to default timeout
    }

    /**
     * Returns a Datetime object given a string
     * @param datetime string representation of datetime given by the user
     * @return Datetime object
     */
    public static Datetime getDatetimeFromString(String datetime) {
        String[] datetimeParts = datetime.split("/");
        return new Datetime(
                Day.valueOf(datetimeParts[0]),
                Integer.parseInt(datetimeParts[1]),
                Integer.parseInt(datetimeParts[2])
        );
    }

    /**
     * Prints to standard output the booking details returned by the server
     * @param bookingID unique identifier of the booking
     * @param facilityName faciliy name for the booking
     * @param startTime start time for the booking
     * @param endTime end time for the booking
     */
    public static void generateBookingDetails(int bookingID, String facilityName, Datetime startTime, Datetime endTime) {
        System.out.println("Booking Confirmation ID: " + bookingID);
        System.out.println("Facility Name: " + facilityName);
        Day day = startTime.day;
        String startHour = startTime.hour > 9 ? String.valueOf(startTime.hour) : "0" + startTime.hour;
        String startMinute = startTime.minute > 0 ? String.valueOf(startTime.minute) : "00";
        String endHour = endTime.hour > 9 ? String.valueOf(endTime.hour) : "0" + endTime.hour;
        String endMinute = endTime.minute > 0 ? String.valueOf(endTime.minute) : "00";
        String dayTime = day.name() + " " + startHour + ":" + startMinute + " - " + endHour + ":" + endMinute;
        System.out.println("Day and time: " + dayTime);
    }

    /**
     * Generic method to generate response depending on type of Response object
     * @param genericResponse Response object
     */
    public static void generateResponse(Response genericResponse) {
        String message = "";
        System.out.println(message);
        if (genericResponse instanceof QueryAvailabilityResponse) {
            QueryAvailabilityResponse response = ((QueryAvailabilityResponse) genericResponse);
            System.out.println(response.responseMessage.message);
            if (response.responseMessage.statusCode == 200) {
                System.out.println("======== AVAILABLITIY FOR " + response.facilityName + " ========");
                for (List<List<Datetime>> l : response.intervals) {
                    for (List<Datetime> m : l){
                        Datetime startDatetime = m.get(0);
                        Datetime endDatetime = m.get(1);
                        Day day = startDatetime.day;
                        String startHour = startDatetime.hour > 9 ? String.valueOf(startDatetime.hour) : "0" + startDatetime.hour;
                        String startMinute = startDatetime.minute > 0 ? String.valueOf(startDatetime.minute) : "00";
                        String endHour = endDatetime.hour > 9 ? String.valueOf(endDatetime.hour) : "0" + endDatetime.hour;
                        String endMinute = endDatetime.minute > 0 ? String.valueOf(endDatetime.minute) : "00";
                        System.out.println(day.name() + " " + startHour + ":" + startMinute + " - " + endHour + ":" + endMinute);

                    }
                }
            }

        } else if (genericResponse instanceof BookFacilityResponse) {
            BookFacilityResponse response = (BookFacilityResponse) genericResponse;
            System.out.println(response.responseMessage.message);
            if (response.responseMessage.statusCode == 200) {
                System.out.println("======== NEW BOOKING CONFIRMATION ========");
                generateBookingDetails(
                        response.bookingID,
                        response.facilityName,
                        response.startTime,
                        response.endTime
                );
            }

        } else if (genericResponse instanceof OffsetBookingResponse) {
            OffsetBookingResponse response = (OffsetBookingResponse) genericResponse;
            System.out.println(response.responseMessage.message);
            if (response.responseMessage.statusCode == 200) {
                System.out.println("======== UPDATED BOOKING CONFIRMATION ========");
                generateBookingDetails(
                        response.bookingID,
                        response.facilityName,
                        response.startTime,
                        response.endTime
                );
            }

        } else if (genericResponse instanceof ExtendBookingResponse) {
            ExtendBookingResponse response = (ExtendBookingResponse) genericResponse;
            System.out.println(response.responseMessage.message);
            if (response.responseMessage.statusCode == 200) {
                System.out.println("======== UPDATED BOOKING CONFIRMATION ========");
                generateBookingDetails(
                        response.bookingID,
                        response.facilityName,
                        response.startTime,
                        response.endTime
                );

            }

        } else if (genericResponse instanceof DeleteBookingResponse) {
            DeleteBookingResponse response = (DeleteBookingResponse) genericResponse;
            System.out.println(response.responseMessage.message);

        } else if (genericResponse instanceof RegisterInterestResponse) {
            RegisterInterestResponse response = (RegisterInterestResponse) genericResponse;
            System.out.println(response.responseMessage.message);

        } else if (genericResponse instanceof TestResponse){
            TestResponse response = (TestResponse) genericResponse;
            System.out.println(response.testString);

        } else if (genericResponse instanceof NullResponse){
            NullResponse response = (NullResponse) genericResponse;
            System.out.println(response.responseMessage.message);
        } else if (genericResponse instanceof ErrorResponse){
            ErrorResponse response = (ErrorResponse) genericResponse;
            System.out.println(response.responseMessage.message);
        }
        else {
            throw new Error("Not implemented");
        }
    }

    /**
     * Helper method to send a request to the server via the communicator module
     * @param router ClientCommunicator object initialised with server configurations
     * @param req Request object
     */
    public static void request(ClientCommunicator router, Request req) {
        try {
            Response res = router.sendRequest(req);
//            System.out.println(res.getClass().getName());
            generateResponse(res);
            System.out.println(res.getClass().getName());
        } catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    /**
     * Returns a boolean indicating validity of the input date format
     * @param datetime string representation of datetime given by user
     * @return true if format is valid
     */
    public static boolean checkValidDateFormat(String datetime) {
        String[] datetimeParts = datetime.split("/");
        return datetimeParts.length == 3;
    }

    /**
     * Returns a boolean indicating validity of input day
     * @param inputDay day of the week provided by user
     * @return true if day is a valid day of the week
     */
    public static boolean checkValidDay(String inputDay) {
        for (Day d: Day.values()){
            if (d.name().equals(inputDay)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a boolean indicating validity of input hour
     * @param hourStr hour provided by the user
     * @return true if hour is valid (between 0 and 23, inclusive)
     */
    public static boolean checkValidHour(String hourStr) {
        int hour = Integer.parseInt(hourStr);
        return (hour >= 0 && hour <= 23);
    }

    /**
     * Returns a boolean indicating validity of input minute
     * @param minuteStr minute provided by the user
     * @return true if minute is valid (00 or 30)
     */
    public static boolean checkValidMinute(String minuteStr) {
        int minute = Integer.parseInt(minuteStr);
        return (minute == 0 || minute == 30);
    }

    /**
     * Returns a boolean indicating validity of the input interval
     * @param startTime start time of booking
     * @param endTime end time of booking
     * @return true if start time is before end time
     */
    public static boolean checkValidStartEnd(Datetime startTime, Datetime endTime) {
        if (isDayBeforeOrEqual(startTime.day, endTime.day)) {
            return (startTime.hour * 60 + startTime.minute < endTime.hour * 60 + endTime.minute);
        }
        return false;
    }

    /**
     * Returns a boolean indicating if the first day is before or same as the second day
     * @param start day of start datetime given by user
     * @param end day of end datetime given by user
     * @return true if start is before or same as end
     */
    public static boolean isDayBeforeOrEqual(Day start, Day end) {
        return start.ordinal() <= end.ordinal();
    }
}
