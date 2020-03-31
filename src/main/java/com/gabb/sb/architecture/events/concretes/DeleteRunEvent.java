package com.gabb.sb.architecture.events.concretes;

import com.gabb.sb.architecture.events.IEvent;

public class DeleteRunEvent implements IEvent {

    private final Integer oRunId;

    public DeleteRunEvent(Integer runId) {
        oRunId = runId;
    }

    public Integer getRunId(){
        return oRunId;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
