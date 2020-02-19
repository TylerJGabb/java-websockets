package com.gabb.sb.architecture;

import com.google.common.reflect.TypeToken;

public abstract class ListenerBase<T extends IPayload> implements IListener<T> {

	/**
	 * {@link TypeToken} is written by google and is a way to capture the runtime type of T
	 * despite type erasure. Its in @Beta phase, and is considered unstable...
	 */
	private final TypeToken<T> oToken;

	public ListenerBase(){
		oToken = new TypeToken<T>(getClass()){};
	}

	@Override
	public final Class acceptedPayload() {
		return oToken.getRawType();
	}
}
