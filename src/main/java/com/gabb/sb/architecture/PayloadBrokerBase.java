package com.gabb.sb.architecture;

import java.util.HashSet;
import java.util.Set;

//TODO: make thread safe??
public abstract class PayloadBrokerBase implements IPayloadBroker{
	
	private Set<IListener> oListeners;

	public PayloadBrokerBase() {
		oListeners = new HashSet<>();
	}

	@Override
	public boolean registerListener(IListener aListener) {
		return oListeners.add(aListener);
	}

	@Override
	public boolean delegate(IPayload aPayload) {
		for(IListener l : oListeners){
			if(l.acceptedPayload().equals(aPayload.getClass())){
				l.consume(aPayload);
				return true;
			}
		}
		return false;
	}
}
