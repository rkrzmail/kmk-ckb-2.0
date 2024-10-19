package com.kmkbe.nikita.data.nson;


import com.kmkbe.nikita.data.Nson;

public interface JsonArray {
    public Nson add(Nson value) ;

    public Nson add(String value) ;

    public Nson add(Boolean value) ;

    public Nson add(Number value) ;

    public Nson add(Object value) ;
    public Nson get(int index);

    public int size();
    public Nson asNson();
}
