package commons.requests;


public class ExtendBookingRequest extends Request{
    public Integer bookingID;
    public String facilityName;
    public Integer extension;

    public ExtendBookingRequest(){}

    public ExtendBookingRequest(int bookingID, String facilityName, int extension) {
        this.bookingID = bookingID;
        this.facilityName = facilityName;
        this.extension = extension;
    }
}
