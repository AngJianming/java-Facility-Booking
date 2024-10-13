package client;

import commons.exceptions.InvalidTimeException;
import commons.exceptions.InvalidDateFormatException;
import commons.exceptions.InvalidDayException;
import commons.utils.Day;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import commons.utils.Datetime;

class ServiceManagerTest {
//    ServerCommunicator serverCommunicator;
//    ClientCommunicator clientCommunicator;
//    InetAddress serverAddress;
//    InetAddress clientAddress;
//    int serverPort = 5000;
//    int clientPort = 3000;
//    {
//        try {
//            this.serverAddress = InetAddress.getByName("localhost");
//            this.clientAddress = InetAddress.getByName("localhost");
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @BeforeEach
//    void setUp() {
//
//        this.serverCommunicator = new ServerCommunicator(this.serverPort);
//        this.clientCommunicator = new ClientCommunicator(this.clientPort, this.serverAddress, this.serverPort);
//    }
//
//    @AfterEach
//    void tearDown() {
//        this.serverCommunicator.close();
//        this.clientCommunicator.close();
//    }
//
//    @Test
//    void queryAvailability(){
//        Datetime dateOne = new Datetime(2021, 3, 1, 0, 0);
//        Datetime dateTwo = new Datetime(2021, 3, 2, 0, 0);
//        ArrayList<Datetime> dateObjs = new ArrayList<Datetime>(Arrays.asList(dateOne,dateTwo));
//        Request req = new QueryAvailabilityRequest("Badminton Court", dateObjs);
//        ServiceManager.request(this.clientCommunicator, req);
//    }

//    @Test
//    void getListOfDates() {
//        String dateString = "Monday Tuesday";
//        List<Datetime> actualDateObjs = ServiceManager.getListOfDates(dateString);
//        Datetime dateOne = new Datetime("Monday", 0, 0);
//        Datetime dateTwo = new Datetime("Tuesday", 0, 0);
//        List<Datetime> expectedDateObjs = new ArrayList<Datetime>() {{add(dateOne);add(dateTwo);}};
//        for (int i = 0; i < actualDateObjs.size(); i++) {
//            Datetime actualDate = actualDateObjs.get(i);
//            Datetime expectedDate = expectedDateObjs.get(i);
//            assertEquals(actualDate.day, expectedDate.day);
//            assertEquals(actualDate.hour, expectedDate.hour);
//            assertEquals(actualDate.minute, expectedDate.minute);
//        }
//    }

    @Test
    void getDatetimeFromString() {
        String datetimeString = "Monday/13/30";
        Datetime actualDatetime;
        actualDatetime = ServiceManager.getDatetimeFromString(datetimeString);
        Datetime expectedDatetime = new Datetime(Day.Monday, 13, 30);
        assertEquals(actualDatetime.day, expectedDatetime.day);
        assertEquals(actualDatetime.hour, expectedDatetime.hour);
        assertEquals(actualDatetime.minute, expectedDatetime.minute);
    }


}