package commons.requests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestRequest extends Request{
    public String testString;
    public Integer testInt;
    public List<Integer> testList;
    public List<List<Integer>> testNestedList;

    public TestRequest(){
        this.name = "TestRequest";
        this.testString = "this is a test string";
        this.testInt = 1234;
        this.testList = new ArrayList<Integer>(Arrays.asList(1,2,3));
        this.testNestedList = new ArrayList<List<Integer>>();
        List<Integer> innerListOne = new ArrayList<Integer>(Arrays.asList(1,2));
        List<Integer> innerListTwo = new ArrayList<Integer>(Arrays.asList(1,2));
        List<Integer> innerListThree = new ArrayList<Integer>();
        this.testNestedList.add(innerListOne);
        this.testNestedList.add(innerListTwo);
        this.testNestedList.add(innerListThree);
    }
}
