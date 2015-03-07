package org.primesoft.holostats.utils;

import org.primesoft.holostats.HoloStatsMain;

/**
 *
 * @author SBPrime
 */
public class ExceptionHelper {

    private static void log(String m) {
        HoloStatsMain.log(m);
    }

    public static void printException(Throwable ex, String message) {
        if (ex == null) {
            return;
        }

        log("***********************************");
        log(message);
        log("***********************************");
        log("* Exception: " + ex.getClass().getCanonicalName());
        log("* Error message: " + ex.getLocalizedMessage());
        log("* Stack: ");
        printStack(ex, "* ");
        log("***********************************");
    }

    public static void printStack(Throwable ex, String lead) {
        for (StackTraceElement element : ex.getStackTrace()) {
            log(lead + element.toString());
        }
    }
}
