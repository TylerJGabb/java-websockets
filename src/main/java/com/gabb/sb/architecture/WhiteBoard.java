package com.gabb.sb.architecture;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.io.IOException;

public final class WhiteBoard {

	private final IResolver oResolver;
	private final IPayloadRouter oPayloadRouter;

	public static void main(String[] args) throws IOException {
		WhiteBoard wb = new WhiteBoard();
		wb.mockIncomingFooBar();
		wb.mockIncomingMessage();
	}

	private Buffer getFooBar() {
		IPayload foobar = new FooBar("Tyler");
		return wrapInBuffer(foobar, 0x02);
	}

	private Buffer getMessage() {
		IPayload meessage = new Message(1);
		return wrapInBuffer(meessage, 0x01);
	}

	public WhiteBoard(){
		oResolver = buildResolver();
		oPayloadRouter = buildRouter();
	}

	private void resolveAndRoute(Buffer aIncomingFromSocket) {
		IPayload payload = oResolver.resolve(aIncomingFromSocket);
		oPayloadRouter.route(payload);
	}

	private void mockIncomingFooBar() {
		Buffer mIncomingFromSocket = getFooBar();
		resolveAndRoute(mIncomingFromSocket);
	}

	private void mockIncomingMessage() {
		Buffer mIncomingFromSocket = getMessage();
		resolveAndRoute(mIncomingFromSocket);
	}

	private IResolver buildResolver() {
		//TODO: brainstorm better way to store these codes...
		IResolver mResolver = new ResolverImpl();
		mResolver.registerTypeCode(Message.class, 0x01);
		mResolver.registerTypeCode(FooBar.class, 0x02);
		return mResolver;
	}

	private Buffer wrapInBuffer(IPayload aPayload, int code) {
		Buffer buf = Buffer.buffer();
		buf.appendInt(code);
		String encoded = Json.encode(aPayload);
		buf.appendString(encoded);
		return buf;
	}

	private IPayloadRouter buildRouter() {
		IPayloadRouter mPayloadRouter = new PayloadRouterBase() {};
		mPayloadRouter.registerListener(new ListenerBase<FooBar>() {

			@Override
			public void consume(FooBar payload) {
				System.out.println("Got Foobar containing " + payload.bar);
			}
		});

		mPayloadRouter.registerListener(new ListenerBase<Message>() {

			@Override
			public void consume(Message payload) {
				System.out.println("Got Message containing " + payload.foo);
			}
		});
		return mPayloadRouter;
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

