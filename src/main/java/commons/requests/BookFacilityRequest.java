package commons.requests;

import commons.utils.Datetime;


public class BookFacilityRequest extends Request{
    public String facilityName;
    public Datetime startTime;
    public Datetime endTime;

    public BookFacilityRequest(){}

    public BookFacilityRequest(String facilityName, Datetime startTime, Datetime endTime){
        this.facilityName = facilityName;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
