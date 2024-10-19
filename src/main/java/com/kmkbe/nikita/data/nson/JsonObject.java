package com.kmkbe.nikita.data.nson;


import com.kmkbe.nikita.data.Nson;


public interface JsonObject {
    public Nson set(String key, Nson value) ;

    public Nson set(String key, String value) ;

    public Nson set(String key, Boolean value) ;

    public Nson set(String key, Number value) ;

    public Nson set(String key, Object value) ;

    public Nson get(String key) ;

    public int size();
    public Nson asNson();
}
