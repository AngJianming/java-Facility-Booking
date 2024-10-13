package commons.utils;


public class Datetime {
    public Day day;
    public Integer hour;
    public Integer minute;

    public Datetime(){}

    /**
     * Custom datetime object
     * @param day
     * @param hour
     * @param minute
     */
    public Datetime(Day day, int hour, int minute) {
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }
}
