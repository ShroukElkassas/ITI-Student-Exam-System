package iti.exam.desktop.util;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

public final class RowUtils {
    private RowUtils() {
    }

    public static Object getIgnoreCase(Map<String, Object> row, String key) {
        if (row == null || key == null) {
            return null;
        }
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String target = key.toLowerCase(Locale.ROOT);
        for (String k : row.keySet()) {
            if (k == null) {
                continue;
            }
            if (k.toLowerCase(Locale.ROOT).equals(target)) {
                return row.get(k);
            }
        }
        return null;
    }

    public static Integer toNullableInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static int toInt(Object value) {
        Integer v = toNullableInt(value);
        return v == null ? 0 : v;
    }

    public static String toNullableString(Object value) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value);
        if (s == null) {
            return null;
        }
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}

