package commons.responses;

import commons.utils.ResponseMessage;

public class ErrorResponse extends Response {
    public ResponseMessage responseMessage;

    public ErrorResponse(){}
    public ErrorResponse(ResponseMessage responseMessage){
        this.responseMessage = responseMessage;
    }
}


