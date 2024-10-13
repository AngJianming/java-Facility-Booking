package commons.requests;


public class RegisterInterestRequest extends Request{
    public String facilityName;
    public Integer numDays;

    public RegisterInterestRequest(){}

    public RegisterInterestRequest(String facilityName, int numDays){
        this.facilityName = facilityName;
        this.numDays = numDays;
    }
}
