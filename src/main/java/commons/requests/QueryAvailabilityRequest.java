package commons.requests;

import commons.utils.Day;
import java.util.List;


public class QueryAvailabilityRequest extends Request {
    public String facilityName;
    public List<Day> days;

    public QueryAvailabilityRequest(){}

    public QueryAvailabilityRequest(String facilityName, List<Day> days) {
        this.facilityName = facilityName;
        this.days = days;
    }
}
