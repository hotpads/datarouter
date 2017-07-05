package io.datarouter.util.mutable;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("serial")
public class MutableBoolean extends AtomicBoolean{

	public MutableBoolean(boolean initialValue){
		super(initialValue);
	}

	public boolean isTrue(){
		return get();
	}

}
