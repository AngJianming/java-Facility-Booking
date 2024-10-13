package commons.responses;

import commons.utils.ResponseMessage;

public class NullResponse extends Response {
    public ResponseMessage responseMessage;

    public NullResponse(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;


    }
}
