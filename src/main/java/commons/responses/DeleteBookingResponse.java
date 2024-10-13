package commons.responses;

import commons.utils.ResponseMessage;

public class DeleteBookingResponse extends Response{
    public ResponseMessage responseMessage;
    public String facilityName;

    public DeleteBookingResponse(){}

    public DeleteBookingResponse(ResponseMessage responseMessage, String facilityName){

        this.responseMessage = responseMessage;
        this.facilityName = facilityName;
    }
}
