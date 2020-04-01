package com.gabb.sb.architecture.events;

@FunctionalInterface
public interface IEventHandler<E extends IEvent> {
	void handle(E e);
}
