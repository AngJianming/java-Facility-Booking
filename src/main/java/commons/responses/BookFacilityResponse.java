package commons.responses;

import commons.utils.Datetime;
import commons.utils.ResponseMessage;

public class BookFacilityResponse extends Response{
    public Integer bookingID;
    public String facilityName;
    public Datetime startTime;
    public Datetime endTime;
    public ResponseMessage responseMessage;

    public BookFacilityResponse(){}

    public BookFacilityResponse(int bookingID, String facilityName, Datetime startTime, Datetime endTime, ResponseMessage responseMessage){
        this.bookingID = bookingID;
        this.facilityName = facilityName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.responseMessage = responseMessage;
    }
}
