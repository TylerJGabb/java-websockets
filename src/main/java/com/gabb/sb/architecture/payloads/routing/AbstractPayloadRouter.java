package com.gabb.sb.architecture.payloads.routing;


import com.gabb.sb.architecture.listners.IListener;
import com.gabb.sb.architecture.payloads.IPayload;

import java.util.HashMap;
//TODO: make thread safe??
public abstract class AbstractPayloadRouter implements IPayloadRouter {

	private HashMap<Class<? extends IPayload>, IListener> oListeners;

	protected AbstractPayloadRouter() {
		oListeners = new HashMap<>();
	}

	@Override
	public boolean registerListener(IListener aListener) {
		Class<? extends IPayload> mClass = aListener.acceptedPayload();
		if(oListeners.containsKey(mClass)){
			System.out.println("AbstractListener already registered for " + mClass);
			return true;
		}
		oListeners.put(mClass, aListener);
		return false;
	}

	@Override
	public boolean route(IPayload aPayload) {
		IListener mIListener = oListeners.get(aPayload.getClass());
		if(mIListener != null){
			mIListener.consume(aPayload);
			return true;
		}
		return false;
	}
}
