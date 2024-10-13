package commons.requests;


public class DeleteBookingRequest extends Request{
    public Integer bookingID;
    public String facilityName;

    public DeleteBookingRequest(){}

    public DeleteBookingRequest(int bookingID, String facilityName){
        this.bookingID = bookingID;
        this.facilityName = facilityName;
    }

}
