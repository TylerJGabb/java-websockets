package com.gabb.sb.architecture;

public final class WhiteBoard {

	public static void main(String[] args) {
		ListenerBase<Message> messageListener = new ListenerBase<Message>() {
			@Override
			public void consume(Message payload) {
				System.out.println("Message");
			}
		};
		System.out.println(messageListener.acceptedPayload());
		
		IListener<FooBar> fooListener = new ListenerBase<FooBar>(){

			@Override
			public void consume(FooBar payload) {
				System.out.println("FooBar");
			}
		};
		System.out.println(fooListener.acceptedPayload());
		
		PayloadBrokerBase broker = new PayloadBrokerBase(){};
		broker.registerListener(messageListener);
		broker.registerListener(fooListener);
		broker.delegate(new Message());
		broker.delegate(new FooBar());
	}
}

class Message implements IPayload{
	
}

class FooBar implements  IPayload{
	
}

