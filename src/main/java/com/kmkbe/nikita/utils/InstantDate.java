package com.kmkbe.nikita.utils;

import javax.xml.crypto.Data;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public class InstantDate extends Date {
ZonedDateTime s;

    public static void main(String[] args) {
        Instant instant = Instant.now();

        System.out.println(instant);
        System.out.println(Utils.Now());
        Date date = Date.from(instant);
        System.out.println(date);



        ZonedDateTime z = ZonedDateTime.now();
        System.out.println(z);
        System.out.println(z.toInstant());
        date = Date.from(z.toInstant());
        System.out.println(date);
        Date d = new Date();

        LocalDateTime l = toInstant(d);
        System.out.println(l);



        System.out.println(d);
        System.out.println(d.toInstant());


    }
    public static LocalDateTime toInstant(Date date) {
        return LocalDateTime.from(date.toInstant()) ;
    }
}
