package commons;

import commons.requests.Request;
import commons.requests.TestRequest;
import commons.responses.Response;
import commons.utils.Day;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

public class Serializer {
    public static void main(String[] args) {
        TestRequest r = new TestRequest();
        ByteBuffer bb = ByteBuffer.allocate(2000);
        serializeObject(r, bb);
    }

    /**
     * Generic serialisation methods to serialise any request or response objects
     * @param o object to be serialised
     * @param bb ByteBuffer to hold sequence of bytes
     */
    public static void serializeObject(Object o, ByteBuffer bb) {
        if (o.getClass().equals(Integer.class)) {
            serializeInteger((Integer) o, bb);
        } else if (o.getClass().equals(String.class)) {
            serializeString((String) o, bb);
        } else if (o.getClass().equals(ArrayList.class)) {
            serializeList((ArrayList<?>) o, bb);
        } else if (o.getClass().equals(Day.class)) {
            serializeDayEnum((Day) o, bb);
        } else {
            String className = o.getClass().getName();
            serializeString(className, bb);
            Field[] fields = o.getClass().getDeclaredFields();
            serializeInteger(fields.length, bb);
            for (Field f: fields) {
                serializeString(f.getName(), bb);
                try {
                    Object val = f.get(o);
                    serializeObject(val, bb);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Helper method to serialise integer
     * @param i
     * @param bb
     */
    public static void serializeInteger(Integer i, ByteBuffer bb) {
        bb.putInt(i);
    }

    /**
     * Helper method to serialise string
     * @param s
     * @param bb
     */
    public static void serializeString(String s, ByteBuffer bb) {
        byte[] sBytes = s.getBytes();
        Integer length = sBytes.length;
        bb.putInt(length);
        bb.put(sBytes);
    }

    /**
     * Helper method to serialise list
     * @param l
     * @param bb
     */
    public static void serializeList(List<?> l, ByteBuffer bb) {
        Integer length = l.size();
        serializeInteger(length, bb);
        if (length == 0){
            return;
        }

        String listObjectType = l.get(0).getClass().getName();
        serializeString(listObjectType, bb);

        for (Object o: l) {
            serializeObject(o, bb);
        }
    }

    public static void serializeDayEnum(Day d, ByteBuffer bb) {
        String eName = d.name();
        serializeString(eName, bb);
    }

}
