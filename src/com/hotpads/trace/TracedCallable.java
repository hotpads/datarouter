package com.hotpads.trace;


public abstract class TracedCallable<V> extends TracedCheckedCallable<V>{
	
	public TracedCallable(String threadName) {
		super(threadName);
	}

	@Override
	public V call()/* limited to RuntimeException */{
		try{
			return super.call();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public abstract V wrappedCall()/* limited to RuntimeException */;
	
}
