package org.primesoft.holostats.utils;

import java.util.Set;
import org.json.simple.JSONObject;

/**
 *
 * @author SBPrime
 */
public class Utils {

    /**
     * Try to parse a string
     *
     * @param s
     * @param result
     * @return
     */
    public static boolean tryParseInteger(String s, InOutParam<Integer> result) {
        if (s == null || result == null) {
            return false;
        }

        try {
            result.setValue(Integer.parseInt(s));

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Do the set contain any of the elements
     *
     * @param <T>
     * @param keySet
     * @param values
     * @return
     */
    public static <T> boolean containsAny(Set<T> keySet, T[] values) {
        if (keySet == null || values == null) {
            return false;
        }

        for (T entry : values) {
            if (keySet.contains(entry)) {
                return true;
            }
        }
        return false;
    }
}
