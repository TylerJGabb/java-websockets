package com.gabb.sb.architecture.listners;

import com.gabb.sb.architecture.payloads.IPayload;

public interface IListener<T extends IPayload> {
	
	void consume(T payload);
	
	Class acceptedPayload();
	
}
