package commons;

import commons.requests.BookFacilityRequest;
import commons.requests.TestRequest;
import commons.responses.BookFacilityResponse;
import commons.responses.ErrorResponse;
import commons.utils.Datetime;
import commons.utils.Day;
import commons.utils.ResponseMessage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import commons.requests.QueryAvailabilityRequest;
import commons.Serializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class DeserializerTest {

    @Test
    void deserializeQueryAvailabilityRequest() {
        List<Day> days = new ArrayList<Day>(Arrays.asList(Day.Monday, Day.Tuesday));
        QueryAvailabilityRequest request = new QueryAvailabilityRequest("badminton court", days);
        ByteBuffer bb = ByteBuffer.allocate(2000);
        Serializer.serializeObject(request, bb);
        bb.flip();
        Object deserialisedRequest = Deserializer.deserializeObject(bb);
        assert deserialisedRequest != null;
        assertEquals(request.days, ((QueryAvailabilityRequest) deserialisedRequest).days);
        assertEquals(request.facilityName, ((QueryAvailabilityRequest) deserialisedRequest).facilityName);
    }

    @Test
    void deserializeBookFacilityRequest() {
        Datetime startTime = new Datetime(Day.Monday, 13,30);
        Datetime endTime = new Datetime(Day.Monday, 14,30);
        BookFacilityRequest request = new BookFacilityRequest("badminton court", startTime, endTime);
        ByteBuffer bb = ByteBuffer.allocate(2000);
        Serializer.serializeObject(request, bb);
        bb.flip();
        Object deserialisedRequest = Deserializer.deserializeObject(bb);
        assert deserialisedRequest != null;
        assertEquals(request.facilityName, ((BookFacilityRequest) deserialisedRequest).facilityName);
        assertEquals(request.startTime.day, ((BookFacilityRequest) deserialisedRequest).startTime.day);
        assertEquals(request.startTime.hour, ((BookFacilityRequest) deserialisedRequest).startTime.hour);
        assertEquals(request.startTime.minute, ((BookFacilityRequest) deserialisedRequest).startTime.minute);
        assertEquals(request.endTime.day, ((BookFacilityRequest) deserialisedRequest).endTime.day);
        assertEquals(request.endTime.hour, ((BookFacilityRequest) deserialisedRequest).endTime.hour);
        assertEquals(request.endTime.minute, ((BookFacilityRequest) deserialisedRequest).endTime.minute);
    }

    @Test
    void deserializeTestRequest() {
        TestRequest request = new TestRequest();
        ByteBuffer bb = ByteBuffer.allocate(2000);
        Serializer.serializeObject(request, bb);
        bb.flip();
        Object deserialisedRequest = Deserializer.deserializeObject(bb);
        assert deserialisedRequest != null;
        assertEquals(request.testInt, ((TestRequest) deserialisedRequest).testInt);
        assertEquals(request.testString, ((TestRequest) deserialisedRequest).testString);
        assertEquals(request.testList, ((TestRequest) deserialisedRequest).testList);
        assertEquals(request.testNestedList, ((TestRequest) deserialisedRequest).testNestedList);
        System.out.println(request.testNestedList);
        System.out.println(((TestRequest) deserialisedRequest).testNestedList);

    }
    @Test
    void deserializeDatetime(){
        Datetime expectedDate = new Datetime(Day.Monday, 13, 30);
        ByteBuffer bb = ByteBuffer.allocate(2000);
        Serializer.serializeObject(expectedDate, bb);
        bb.flip();
        Object actualDate = Deserializer.deserializeObject(bb);
        assert actualDate != null;
        assertEquals(((Datetime) actualDate).day, expectedDate.day);
    }

    @Test
    void deserializeResponseMessage(){
        ResponseMessage expected = new ResponseMessage(200, "Successful");
        ByteBuffer bb = ByteBuffer.allocate(2000);
        Serializer.serializeObject(expected, bb);
        bb.flip();
        Object actual = Deserializer.deserializeObject(bb);
        assertEquals(((ResponseMessage) actual).message, expected.message);
        assertEquals(((ResponseMessage) actual).statusCode, expected.statusCode);
    }

    @Test
    void deserializeBookFacilityResponse() {
        ResponseMessage responseMessage = new ResponseMessage(200, "Successful");
        Datetime startTime = new Datetime(Day.Monday, 13,30);
        Datetime endTime = new Datetime(Day.Monday, 14,30);
        BookFacilityResponse response = new BookFacilityResponse(1, "badminton court", startTime, endTime, responseMessage);
        ByteBuffer bb = ByteBuffer.allocate(2000);
        Serializer.serializeObject(response, bb);
        bb.flip();
        Object deserialisedResponse = Deserializer.deserializeObject(bb);
        assert deserialisedResponse != null;
        assertEquals(response.facilityName, ((BookFacilityResponse) deserialisedResponse).facilityName);
        assertEquals(response.startTime.day, ((BookFacilityResponse) deserialisedResponse).startTime.day);
        assertEquals(response.startTime.hour, ((BookFacilityResponse) deserialisedResponse).startTime.hour);
        assertEquals(response.startTime.minute, ((BookFacilityResponse) deserialisedResponse).startTime.minute);
        assertEquals(response.endTime.day, ((BookFacilityResponse) deserialisedResponse).endTime.day);
        assertEquals(response.endTime.hour, ((BookFacilityResponse) deserialisedResponse).endTime.hour);
        assertEquals(response.endTime.minute, ((BookFacilityResponse) deserialisedResponse).endTime.minute);
        assertEquals(response.responseMessage.statusCode, ((BookFacilityResponse) deserialisedResponse).responseMessage.statusCode);
        assertEquals(response.responseMessage.message, ((BookFacilityResponse) deserialisedResponse).responseMessage.message);
    }

    @Test
    void deserializeErrorResponse(){
        ResponseMessage responseMessage = new ResponseMessage(200, "Successful");
        ErrorResponse response = new ErrorResponse(responseMessage);
        ByteBuffer bb = ByteBuffer.allocate(2000);
        Serializer.serializeObject(response, bb);
        bb.flip();
        Object deserialisedResponse = Deserializer.deserializeObject(bb);
        assert deserialisedResponse != null;
        assertEquals(response.responseMessage.message, ((ErrorResponse) response).responseMessage.message);
        assertEquals(response.responseMessage.statusCode, ((ErrorResponse) response).responseMessage.statusCode);
    }
}