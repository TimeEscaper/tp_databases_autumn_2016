package ru.forum.helper;

public class Base65 {
    private static int BASE = 65;
    private static String CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_.~";
    private static int PATHLENGTH = 5;

    public static String toString(Long decNumber) {
        final StringBuilder result = new StringBuilder();
        final int offset = (int)(decNumber % BASE);
        if ((decNumber - offset) == 0)
            result.append(CHARSET.charAt(offset));
        else {
            result.append(Long.toString((decNumber - offset) / BASE));
            result.append(CHARSET.charAt(offset));
        }
        return result.toString();
    }

    public static String makePath(Long decnumber) {
        final StringBuilder result = new StringBuilder();
        result.append('/');
        final String baseString = toString(decnumber);
        for (int i = 1; i <= (PATHLENGTH - baseString.length()); i++) {
            result.append(CHARSET.charAt(0));
        }
        result.append(baseString);
        return result.toString();
    }
}
