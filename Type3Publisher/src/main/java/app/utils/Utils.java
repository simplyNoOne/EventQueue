package app.utils;

import java.util.List;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.Set;

public class Utils {
    public static List<String> getEventTypes() {
        String packageName = "domain";
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

        List<String> eventClassNames = classes.stream()
                .map(Class::getSimpleName)
                .filter(className -> className.endsWith("Event"))
                .toList();

        return eventClassNames;
    }



}
