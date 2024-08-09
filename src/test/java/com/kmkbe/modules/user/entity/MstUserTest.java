package com.kmkbe.modules.user.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MstUserTest {

    @Test
    public void mstUserTest_shouldReturnValidEntityUsingBuilderPattern(){
        MstUser user = MstUser.builder()
                .userId(1L)
                .username("testing1")
                .password("12345")
                .build();

        Assertions.assertEquals(user.getUserId(), 1L);
        Assertions.assertEquals(user.getUsername(), "testing1");
        Assertions.assertEquals(user.getPassword(), "12345");
    }
}
