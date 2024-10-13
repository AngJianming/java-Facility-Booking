package commons.responses;

import commons.utils.ResponseMessage;

public class RegisterInterestResponse extends Response{
    public ResponseMessage responseMessage;

    public RegisterInterestResponse(){}

    public RegisterInterestResponse(ResponseMessage responseMessage){
        this.responseMessage = responseMessage;
    }
}
