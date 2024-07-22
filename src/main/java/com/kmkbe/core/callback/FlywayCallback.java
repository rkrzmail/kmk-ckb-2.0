package com.kmkbe.core.callback;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;

@Slf4j
public class FlywayCallback implements Callback {

    @Override
    public boolean supports(Event event, Context context) {
        //return event == Event.BEFORE_EACH_MIGRATE || event == Event.AFTER_EACH_MIGRATE;
        return true;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }

    @Override
    public void handle(Event event, Context context) {
        //log.info("event, {}", event);
        if (event == Event.BEFORE_EACH_MIGRATE) {
            // log.info("before each migrate running {}", context.getStatement());
        } else if (event == Event.AFTER_EACH_MIGRATE) {
            // log.info("after each migrate running {}", context.getStatement());
        } else {
            // log.info("unknown event {}", event);
        }
    }

    @Override
    public String getCallbackName() {
        return FlywayCallback.class.getSimpleName();
    }
}
