package org.banana.util;

public final class JpqlHelper {

    private JpqlHelper() {
    }

    public static String formatSearchParam(String param) {
        return param
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("%", "\\%").toLowerCase();
    }
}
