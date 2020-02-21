package com.gabb.sb.architecture.payloads.processing;

import com.gabb.sb.architecture.payloads.IPayload;
import com.google.common.reflect.TypeToken;

public abstract class AbstractPayloadProcessor<T extends IPayload> implements IPayloadProcessor<T> {

	/**
	 * {@link TypeToken} is written by google and is a way to capture the runtime type of T
	 * despite type erasure. Its in @Beta phase, and is considered unstable...
	 */
	private final TypeToken<T> oToken;

	public AbstractPayloadProcessor(){
		oToken = new TypeToken<T>(getClass()){};
	}

	@Override
	public final Class canConsume() {
		return oToken.getRawType();
	}
}
