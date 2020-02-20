package com.gabb.sb.architecture;


import java.util.HashMap;
//TODO: make thread safe??
public abstract class PayloadRouterBase implements IPayloadRouter {

	private HashMap<Class<? extends IPayload>, IListener> oListeners;

	protected PayloadRouterBase() {
		oListeners = new HashMap<>();
	}

	@Override
	public boolean registerListener(IListener aListener) {
		Class<? extends IPayload> mClass = aListener.acceptedPayload();
		if(oListeners.containsKey(mClass)){
			System.out.println("Listener already registered for " + mClass);
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
