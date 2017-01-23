package ru.forum.helper;

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

public class QueryHelper {

    public static String format(String formatingString, Object... objects) {
        final StringBuilder stringBuilder = new StringBuilder();
        try (Formatter formatter = new Formatter(stringBuilder, Locale.US)) {
            formatter.format(formatingString, objects);
            return formatter.toString();
        } catch (IllegalFormatException e) {
            return null;
        }
    }
}
