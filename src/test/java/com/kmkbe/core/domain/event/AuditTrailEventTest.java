package com.kmkbe.core.domain.event;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.constant.AuditActorType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
class AuditTrailEventTest {
  @Test void recordAccessors() {
    var now = LocalDateTime.now();
    var before = JsonNodeFactory.instance.objectNode();
    var after = JsonNodeFactory.instance.objectNode().put("a",1);
    var e = new AuditTrailEvent("t1", AuditActorType.INTERNAL, "u", "id", "127.0.0.1","ua","mod", AuditAction.CREATE,"ent","e1", before, after, "/p","POST",200,true,null, now);
    assertThat(e.traceId()).isEqualTo("t1");
    assertThat(e.actorType()).isEqualTo(AuditActorType.INTERNAL);
    assertThat(e.actorUsername()).isEqualTo("u");
    assertThat(e.success()).isTrue();
    assertThat(e.createdAt()).isEqualTo(now);
    assertThat(e.afterData().get("a").asInt()).isEqualTo(1);
  }
}
