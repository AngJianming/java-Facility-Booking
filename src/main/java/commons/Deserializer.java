package commons;

import commons.utils.Day;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Deserializer {
    /**
     * Generic deserialisation method for any request or response objects
     * @param bb input ByteBuffer to hold sequence of bytes of the deserialised object
     * @return
     */
    public static Object deserializeObject(ByteBuffer bb) {
        String className = deserializeString(bb);
        int numFields = deserializeInteger(bb);
        try {
            Class<?> c = Class.forName(className);
            Object o = c.newInstance();

            HashMap<String, Object> fieldTypeMap = new HashMap<>();
            Field[] fields = c.getFields();
            for (Field f: fields) {
                fieldTypeMap.put(f.getName(), f.getType());
            }
            for (int i = 0; i < numFields; i++) {
                String fieldName = deserializeString(bb);
                Object val;
                if (fieldTypeMap.get(fieldName).equals(String.class)) {
                    val = deserializeString(bb);
                } else if (fieldTypeMap.get(fieldName).equals(Integer.class)) {
                    val = deserializeInteger(bb);
                } else if (fieldTypeMap.get(fieldName).equals(List.class)) {
                    val = deserializeList(bb);
                } else if (fieldTypeMap.get(fieldName).equals(Day.class)) {
                    val = deserializeDayEnum(bb);
                } else {
                    val = Deserializer.deserializeObject(bb);
                }
                try {
                    o.getClass().getDeclaredField(fieldName).set(o, val);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
            return c.cast(o);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Helper method to deserialise an integer
     * @param bb ByteBuffer holding the sequence of bytes
     * @return deserialised integer
     */
    public static int deserializeInteger(ByteBuffer bb) {
        return bb.getInt();
    }

    /**
     * Helper method to deserialise a string
     * @param bb ByteBuffer holding the sequence of bytes
     * @return deserialised string
     */
    public static String deserializeString(ByteBuffer bb) {
        int length = bb.getInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++){
            bytes[i] = bb.get();
        }
        return new String(bytes);
    }

    /**
     * Helper method to deserialise a list
     * @param bb ByteBuffer holding the sequence of bytes
     * @return deserialised list
     */
    public static List<Object> deserializeList(ByteBuffer bb) {
        int length = deserializeInteger(bb);
        if (length == 0) {
            return new ArrayList<>();
        }
        String listObjectType = deserializeString(bb);
        List<Object> l = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            if (listObjectType.equals("java.lang.String")) {
                l.add(deserializeString(bb));
            } else if (listObjectType.equals("java.lang.Integer")) {
                l.add(deserializeInteger(bb));
            } else if (listObjectType.equals("java.util.ArrayList")) {
                l.add(deserializeList(bb));
            } else if (listObjectType.equals("commons.utils.Day")) {
                l.add(deserializeDayEnum(bb));
            } else {
                l.add(deserializeObject(bb));
            }
        }
        return l;
    }

    public static Day deserializeDayEnum(ByteBuffer bb) {
        String eName = deserializeString(bb);
        return Day.valueOf(eName);
    }
}
