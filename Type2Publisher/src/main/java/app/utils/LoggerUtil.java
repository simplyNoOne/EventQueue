package app.utils;

import java.util.logging.Logger;

public class LoggerUtil {
    public static Logger getLogger(Class<?> classToLog) {
        return Logger.getLogger(classToLog.getName());
    }
}