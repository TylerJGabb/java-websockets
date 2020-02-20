package com.gabb.sb.architecture;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.io.IOException;

public final class WhiteBoard {

	private static PayloadBrokerBase cBroker;

	public static void main(String[] args) throws IOException {
		IPayload meessage = new Message(1);
		IPayload foobar = new FooBar("Tyler");
		Buffer mesBuf = wrapInBuffer(meessage, 0x01);
		Buffer fooBuf = wrapInBuffer(foobar, 0x02);
		IResolver resolver = new ResolverImpl();
		//TODO: brainstorm better way to store these codes...
		resolver.registerTypeCode(0x01,  Message.class);
		resolver.registerTypeCode(0x02, FooBar.class);
		IPayload recieved = resolver.resolve(mesBuf);
		brokerTest(recieved);
		recieved = resolver.resolve(fooBuf);
		brokerTest(recieved);
		
	}

	private static Buffer wrapInBuffer(IPayload aPayload, int code) {
		Buffer buf = Buffer.buffer();
		buf.appendInt(code);
		String encoded = Json.encode(aPayload);
		buf.appendString(encoded);
		return buf;
	}

	private static void brokerTest(IPayload payload) {
		ListenerBase<Message> messageListener = new ListenerBase<Message>() {
			@Override
			public void consume(Message payload) {
				System.out.println("Got Message containing " + payload.foo);
			}
		};

		IListener<FooBar> fooListener = new ListenerBase<FooBar>(){

			@Override
			public void consume(FooBar payload) {
				System.out.println("Got Foobar containing " + payload.bar);
			}
		};

		cBroker = new PayloadBrokerBase(){};
		cBroker.registerListener(messageListener);
		cBroker.registerListener(fooListener);
		cBroker.delegate(payload);
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

	public FooBar() {
	}

	public FooBar(String aBar) {
		bar = aBar;
	}

	@JsonProperty
	String bar;
	
}

