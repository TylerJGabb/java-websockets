package com.gabb.sb.architecture;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public final class WhiteBoard {

	public static void main(String[] args) throws IOException {
		IResolver resolver = new ResolverBase() {};
		resolver.registerTypeCode(0x01,  Message.class);
		resolver.registerTypeCode(0x02, FooBar.class);
		IPayload payload = new Message(1);
		Buffer buf = Buffer.buffer();
		buf.appendInt(0x01);
		String encoded = Json.encode(payload);
		buf.appendString(encoded);
		IPayload recieved = resolver.resolve(buf);
		brokerTest(recieved);
	}

	private static void brokerTest(IPayload payload) {
		ListenerBase<Message> messageListener = new ListenerBase<Message>() {
			@Override
			public void consume(Message payload) {
				System.out.println("Got Message");
			}
		};
		System.out.println(messageListener.acceptedPayload());

		IListener<FooBar> fooListener = new ListenerBase<FooBar>(){

			@Override
			public void consume(FooBar payload) {
				System.out.println("Got FooBar");
			}
		};
		System.out.println(fooListener.acceptedPayload());

		PayloadBrokerBase broker = new PayloadBrokerBase(){};
		broker.registerListener(messageListener);
		broker.registerListener(fooListener);
//		broker.delegate(new Message(2));
//		broker.delegate(new FooBar());
		broker.delegate(payload);
	}
}

class Message implements IPayload{

	public Message() {
	}

	public Message(int aFoo) {
		foo = aFoo;
	}

	@JsonProperty
	int foo;
}

class FooBar implements  IPayload{
	
	@JsonProperty
	int bar;
	
}

