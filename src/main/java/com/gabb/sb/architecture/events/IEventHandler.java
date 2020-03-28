package com.gabb.sb.architecture.events;

@FunctionalInterface
public interface IEventHandler<E> {
	void handle(E e);
}
