package com.kmkbe.nikita.data.nson;

public final  class Nullable {
    public static final Nullable NULL = new Nullable();
    public static boolean isNull(Object obj) {
        return obj == null || obj == NULL;
    }

    @Override
    public String toString() {
        return "";
    }


    public String asString() {
        return "";
    }
}
