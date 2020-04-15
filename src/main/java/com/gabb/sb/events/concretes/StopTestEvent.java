package com.gabb.sb.events.concretes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.events.IEvent;

public class StopTestEvent implements IEvent {

    @JsonProperty //won't serialize without at least one field
    private boolean force = true;

}
