package commons.responses;

import java.util.List;
import commons.utils.Datetime;
import commons.utils.ResponseMessage;

public class QueryAvailabilityResponse extends Response{
    public String facilityName;
    public List<List<List<Datetime>>> intervals;
    public ResponseMessage responseMessage;

    public QueryAvailabilityResponse(){}

    public QueryAvailabilityResponse(String facilityName, List<List<List<Datetime>>> intervals, ResponseMessage responseMessage){
        this.facilityName = facilityName;
        this.intervals = intervals;
        this.responseMessage = responseMessage;
    }
}