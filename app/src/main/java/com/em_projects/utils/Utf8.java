package com.em_projects.utils;

public final class Utf8 {

    public static boolean isRequireEncode(String text) {
        int len = text.length();

        for (int i = 0; i < len; i++)
            if (text.charAt(i) > 0x7F)
                return true;

        return false;
    }

    public static String encode(String text) {
        StringBuilder buffer = new StringBuilder();
        int len = text.length();
        char ch;

        for (int i = 0; i < len; i++) {
            ch = text.charAt(i);
            if (ch <= 0x7F) {
                buffer.append(ch);
            } else if (ch <= 0x7FF) {
                buffer.append((char) (0xC0 | ((ch >> 6) & 0x1F)));
                buffer.append((char) (0x80 | (ch & 0x3F)));
            } else {
                buffer.append((char) (0xE0 | ((ch >> 12) & 0xF)));
                buffer.append((char) (0x80 | ((ch >> 6) & 0x3F)));
                buffer.append((char) (0x80 | (ch & 0x3F)));
            }
        }

        return buffer.toString();
    }

    public static String decode(String text) {
        StringBuilder buffer = new StringBuilder();
        int len = text.length();
        char ch;

        for (int i = 0; i < len; i++) {
            ch = text.charAt(i);
            if (ch <= 0x7F) {
                buffer.append(ch);
            } else if ((ch & 0xE0) == 0xC0) {
                char ch2 = (char) ((ch & 0x1F) << 6);

                i++;
                ch = text.charAt(i);
                ch2 |= ch & 0x3F;

                buffer.append(ch2);
            } else {
                char ch2 = (char) ((ch & 0xF) << 12);

                i++;
                ch = text.charAt(i);
                ch2 |= (char) ((ch & 0x3F) << 6);

                i++;
                ch = text.charAt(i);
                ch2 |= ch & 0x3F;

                buffer.append(ch2);
            }
        }

        return buffer.toString();
    }
}
