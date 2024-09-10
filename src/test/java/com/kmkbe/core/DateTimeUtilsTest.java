package com.kmkbe.core;

import com.kmkbe.core.utils.DateTimeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class DateTimeUtilsTest {

    @Test
    @DisplayName("should return non null formatted")
    public void cSharpTimeStampToDateTest() {
        String dateStr = "2023-07-20T00:00:00";
        Date date = DateTimeUtils.cSharpTimeStampToDate(dateStr);
        Assertions.assertNotNull(date);
    }
}
