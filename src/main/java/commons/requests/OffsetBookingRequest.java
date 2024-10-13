package commons.requests;


public class OffsetBookingRequest extends Request{
    public Integer bookingID;
    public String facilityName;
    public Integer offset;

    public OffsetBookingRequest(){}

    public OffsetBookingRequest(int bookingID, String facilityName, int offset) {
        this.bookingID = bookingID;
        this.facilityName = facilityName;
        this.offset = offset;
    }
}
