package com.bsoft.catalogus.util;

import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Optional;

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

    public static boolean stringChanged(final Optional<String> os1, final String s2) {
        boolean changed = false;

        if (os1.isPresent()) {
            changed = stringChanged(os1.get(), s2);
        } else { // not present
            changed = ((s2 != null) && (s2.length() > 0));
        }

        return changed;
    }

    public static boolean stringChanged(final Optional<String> os1, final Optional<String> os2) {
        boolean changed = false;

        if (os2.isPresent()) {
            changed = stringChanged(os1, os2.get());
        } else { // os1 not present
            if (os2.isPresent()) {
                changed = false;
            }
        }

        return changed;
    }

    public static boolean stringChanged(final JsonNullable<String> os1, final String os2) {
        boolean changed = false;

        if (os1.isPresent() && (os1.get() != null)) {
            changed = stringChanged(os1.get(), os2);
        } else { // os1 not present
            changed = ((os2 != null) && (os2.length() > 0));
        }

        return changed;
    }

    public static boolean stringChanged(final JsonNullable<String> os1, final JsonNullable<String> os2) {
        boolean changed = false;

        if (os1.isPresent() && (os1.get() != null)) {
            changed = stringChanged(os2, os1.get());
        } else { // os1 not present
            if (os2.isPresent()) {
                changed = false;
            }
        }

        return changed;
    }
}
