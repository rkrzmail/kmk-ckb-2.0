package com.kmkbe.helpers.base;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class BaseClassesTest {

  @Test
  void baseResponseBuilder_constructorsAndAccessors() {
    BaseResponseBuilder<String> empty = new BaseResponseBuilder<>();
    empty.setSuccess(true);
    empty.setCode(200);
    empty.setMessage("OK");
    assertThat(empty.isSuccess()).isTrue();
    assertThat(empty.getCode()).isEqualTo(200);
    assertThat(empty.getMessage()).isEqualTo("OK");
    assertThat(empty.toString()).contains("OK");

    BaseResponseBuilder<String> withData = new BaseResponseBuilder<>(true, 201, "Created", "payload");
    assertThat(withData.isSuccess()).isTrue();
    assertThat(withData.getCode()).isEqualTo(201);
    assertThat(withData.getMessage()).isEqualTo("Created");

    BaseResponseBuilder<String> noData = new BaseResponseBuilder<>(false, 400, "Bad");
    assertThat(noData.isSuccess()).isFalse();
    assertThat(noData.getCode()).isEqualTo(400);
    assertThat(noData.getMessage()).isEqualTo("Bad");
  }

  @Test
  void baseEntity_lifecycleAndAccessors() {
    BaseEntity entity = new BaseEntity();
    entity.setCreatedBy("admin");
    entity.setUpdatedBy("user");
    Timestamp now = new Timestamp(System.currentTimeMillis());
    entity.setCreatedTime(now);
    entity.setUpdatedTime(now);

    assertThat(entity.getCreatedBy()).isEqualTo("admin");
    assertThat(entity.getUpdatedBy()).isEqualTo("user");
    assertThat(entity.getCreatedTime()).isEqualTo(now);
    assertThat(entity.getUpdatedTime()).isEqualTo(now);
    assertThat(entity.toString()).contains("admin");
    assertThat(entity.hashCode()).isNotZero();
    assertThat(entity.equals(entity)).isTrue();

    BaseEntity other = new BaseEntity();
    other.setCreatedBy("admin");
    other.setUpdatedBy("user");
    other.setCreatedTime(now);
    other.setUpdatedTime(now);
    assertThat(entity).isEqualTo(other);

    entity.onCreate();
    entity.onUpdate();
    assertThat(entity.getCreatedTime()).isNotNull();
    assertThat(entity.getUpdatedTime()).isNotNull();
  }

  @Test
  void emptyRequestAndResponse_canInstantiate() {
    EmptyRequest req = new EmptyRequest();
    EmptyResponse res = new EmptyResponse();
    BaseResponse base = new BaseResponse();
    assertThat(req).isInstanceOf(BaseRequest.class);
    assertThat(res).isInstanceOf(BaseResponse.class);
    assertThat(base).isNotNull();
  }
}
