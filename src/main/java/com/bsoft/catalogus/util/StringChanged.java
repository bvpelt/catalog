package com.bsoft.catalogus.util;

public class StringChanged {

    public static boolean stringChanged(final String s1, final String s2) {
        boolean changed = false;

        if (s1 == null) {
            changed = (s2 != null);
        } else {
            changed = !s1.equals(s2);
        }

        return changed;
    }
}
