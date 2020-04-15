package com.gabb.sb.events;

@FunctionalInterface
public interface IEventHandler<E extends IEvent> {
	void handle(E e);
}
