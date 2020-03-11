package com.gabb.sb.architecture.messages.publish;


import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.messages.subscribe.AbstractMessageSubscriber;
import com.gabb.sb.architecture.messages.subscribe.IMessageSubscriber;
import com.gabb.sb.architecture.resolver.IMessageResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * When an incoming {@link io.vertx.core.buffer.Buffer} is resolved by an
 * {@link IMessageResolver} into an {@link IMessage},
 * it is then passed to an oInstance of this class, who then finds an
 * {@link IMessageSubscriber} capable of processing it.
 */

@SuppressWarnings("unchecked")
public abstract class AbstractMessagePublisher implements IMessagePublisher {
	//TODO: make thread safe??
	
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractMessagePublisher.class);

	private HashMap<Class<? extends IMessage>, List<IMessageSubscriber>> oMessageSubscriberMap;

	protected AbstractMessagePublisher() {
		oMessageSubscriberMap = new HashMap<>();
	}

	@Override
	public void addSubscriber(IMessageSubscriber messageSubscriber) {
		Class subscription = messageSubscriber.subscription();
		oMessageSubscriberMap.computeIfAbsent(subscription, __ -> new ArrayList<>());
		oMessageSubscriberMap.get(subscription).add(messageSubscriber);
	}

	@Override
	public boolean publish(IMessage message) {
		if(message == null){
			LOGGER.warn("Tried to publish null message");
			return false;
		}
		List<IMessageSubscriber> subscribers = oMessageSubscriberMap.get(message.getClass());
		if(subscribers == null) return false;
		subscribers.forEach(sub -> sub.process(message));
		return true;
	}
	
	public static IMessagePublisher publisher() {
		return new AbstractMessagePublisher() { };
	}
	
	public static Builder builder(){
		return new Builder();
	}
	
	public static class Builder {
		
		private IMessagePublisher oInstance;
		private boolean built = false;

		public Builder() {
			oInstance = AbstractMessagePublisher.publisher();
		}
		
		public <M extends IMessage> Builder addSubscriber(Class<M> aClass, IMessageProcessor<M> aProcess) {
			oInstance.addSubscriber(new AbstractMessageSubscriber<>(aClass) {
				@Override
				public void process(M message) {
					aProcess.process(message);
				}
			});
			return this;
		}
		
		public IMessagePublisher build(){
			if(built) throw new IllegalStateException("Already Built");
			built = true;
			return oInstance;
		}
	}
	
	@FunctionalInterface
	public interface IMessageProcessor<M> {
		void process(M message);
	}
}


