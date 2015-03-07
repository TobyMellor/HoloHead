package org.primesoft.holostats.utils;

import org.json.simple.JSONObject;

/**
 *
 * @author SBPrime
 */
public class JSONUtils {
    public static int getInt(JSONObject o, String field, Integer defaultValue) {
        InOutParam<Integer> v = InOutParam.Out();
        
        if (!getInt(o, field, v)) {
            return defaultValue;
        }
        
        return v.getValue();
    }
    
    
    public static String getString(JSONObject o, String field, String defaultValue) {
        InOutParam<String> v = InOutParam.Out();
        
        if (!getString(o, field, v)) {
            return defaultValue;
        }
        
        return v.getValue();
    }
    

    /**
     * Try to get integer field from JSON object
     *
     * @param o
     * @param field
     * @param out
     * @return
     */
    public static boolean getInt(JSONObject o, String field, InOutParam<Integer> out) {
        if (o == null || field == null || !o.containsKey(field) || out == null) {
            return false;
        }

        Object value = o.get(field);
        if (value == null) {
            return false;
        }
        if (value instanceof Integer) {
            out.setValue((Integer) value);
            return true;
        }

        return Utils.tryParseInteger(value.toString(), out);
    }

    /**
     * Try to get string field from JSON object
     *
     * @param o
     * @param field
     * @param out
     * @return
     */
    private static boolean getString(JSONObject o, String field, InOutParam<String> out) {
        if (o == null || field == null || !o.containsKey(field) || out == null) {
            return false;
        }

        Object value = o.get(field);
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            out.setValue((String) value);
        }

        out.setValue(value.toString());

        return true;
    }
}
