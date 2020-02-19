package com.gabb.sb.architecture;

public interface IListener<T extends IPayload> {
	
	void consume(T payload);
	
	Class acceptedPayload();
	
}
