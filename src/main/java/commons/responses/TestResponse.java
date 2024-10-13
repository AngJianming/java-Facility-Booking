package commons.responses;

public class TestResponse extends Response {
    public String testString;
    public Integer testInt;

    public TestResponse() {
        this.name = "TestResponse";
        this.testString = "this is a test string";
        this.testInt = 1234;
    }
}
