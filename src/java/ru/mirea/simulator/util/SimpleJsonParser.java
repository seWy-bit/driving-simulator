package ru.mirea.simulator.util;

import java.util.*;

/**
 * Встроенный легковесный парсер JSON без внешних зависимостей.
 * Обеспечивает компиляцию и запуск на любой установленной Java без сторонних JAR-библиотек.
 */
public class SimpleJsonParser {

    public static Object parse(String json) {
        String trimmed = json.trim();
        int[] index = new int[]{0};
        return parseValue(trimmed, index);
    }

    private static Object parseValue(String s, int[] idx) {
        skipWhitespace(s, idx);
        if (idx[0] >= s.length()) return null;

        char c = s.charAt(idx[0]);
        if (c == '{') return parseObject(s, idx);
        if (c == '[') return parseArray(s, idx);
        if (c == '"') return parseString(s, idx);
        if (c == 't' || c == 'f') return parseBoolean(s, idx);
        if (c == 'n') return parseNull(s, idx);
        if (Character.isDigit(c) || c == '-') return parseNumber(s, idx);

        throw new IllegalArgumentException("Unexpected character at " + idx[0] + ": " + c);
    }

    private static void skipWhitespace(String s, int[] idx) {
        while (idx[0] < s.length() && Character.isWhitespace(s.charAt(idx[0]))) {
            idx[0]++;
        }
    }

    private static Map<String, Object> parseObject(String s, int[] idx) {
        Map<String, Object> map = new LinkedHashMap<>();
        idx[0]++; // skip '{'

        while (true) {
            skipWhitespace(s, idx);
            if (idx[0] >= s.length()) break;
            if (s.charAt(idx[0]) == '}') {
                idx[0]++;
                break;
            }

            String key = parseString(s, idx);
            skipWhitespace(s, idx);
            if (s.charAt(idx[0]) != ':') {
                throw new IllegalArgumentException("Expected ':' at " + idx[0]);
            }
            idx[0]++; // skip ':'

            Object value = parseValue(s, idx);
            map.put(key, value);

            skipWhitespace(s, idx);
            if (idx[0] < s.length() && s.charAt(idx[0]) == ',') {
                idx[0]++;
            } else if (idx[0] < s.length() && s.charAt(idx[0]) == '}') {
                idx[0]++;
                break;
            }
        }
        return map;
    }

    private static List<Object> parseArray(String s, int[] idx) {
        List<Object> list = new ArrayList<>();
        idx[0]++; // skip '['

        while (true) {
            skipWhitespace(s, idx);
            if (idx[0] >= s.length()) break;
            if (s.charAt(idx[0]) == ']') {
                idx[0]++;
                break;
            }

            Object value = parseValue(s, idx);
            list.add(value);

            skipWhitespace(s, idx);
            if (idx[0] < s.length() && s.charAt(idx[0]) == ',') {
                idx[0]++;
            } else if (idx[0] < s.length() && s.charAt(idx[0]) == ']') {
                idx[0]++;
                break;
            }
        }
        return list;
    }

    private static String parseString(String s, int[] idx) {
        skipWhitespace(s, idx);
        idx[0]++; // skip opening quote
        StringBuilder sb = new StringBuilder();

        while (idx[0] < s.length()) {
            char c = s.charAt(idx[0]++);
            if (c == '"') break;
            if (c == '\\') {
                if (idx[0] < s.length()) {
                    char esc = s.charAt(idx[0]++);
                    if (esc == '"') sb.append('"');
                    else if (esc == '\\') sb.append('\\');
                    else if (esc == 'n') sb.append('\n');
                    else if (esc == 't') sb.append('\t');
                    else sb.append(esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Number parseNumber(String s, int[] idx) {
        int start = idx[0];
        boolean isDouble = false;
        while (idx[0] < s.length()) {
            char c = s.charAt(idx[0]);
            if (c == '.' || c == 'e' || c == 'E') {
                isDouble = true;
                idx[0]++;
            } else if (Character.isDigit(c) || c == '-' || c == '+') {
                idx[0]++;
            } else {
                break;
            }
        }
        String numStr = s.substring(start, idx[0]);
        if (isDouble) {
            return Double.parseDouble(numStr);
        } else {
            return Long.parseLong(numStr);
        }
    }

    private static Boolean parseBoolean(String s, int[] idx) {
        if (s.startsWith("true", idx[0])) {
            idx[0] += 4;
            return true;
        } else if (s.startsWith("false", idx[0])) {
            idx[0] += 5;
            return false;
        }
        throw new IllegalArgumentException("Unknown boolean at " + idx[0]);
    }

    private static Object parseNull(String s, int[] idx) {
        if (s.startsWith("null", idx[0])) {
            idx[0] += 4;
            return null;
        }
        throw new IllegalArgumentException("Unknown token at " + idx[0]);
    }
}
