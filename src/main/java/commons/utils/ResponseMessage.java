package commons.utils;

public class ResponseMessage {
    public Integer statusCode;
    public String message;

    public ResponseMessage(){}

    /**
     * To be sent in all Response objects to indicate success/failure of remote method invocation
     * @param statusCode
     * @param message
     */
    public ResponseMessage(int statusCode, String message){
        this.statusCode = statusCode;
        this.message = message;
    }

}
