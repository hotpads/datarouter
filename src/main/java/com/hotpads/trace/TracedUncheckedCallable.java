package com.hotpads.trace;



/**
 * Do not use the "CallerRunsPolicy" RejectedExecutionHandler with TracedUncheckedCallable
 */
public abstract class TracedUncheckedCallable<T> extends TracedCheckedCallable<T>{
	
	public TracedUncheckedCallable(String name){
		super(name);
	}
	
	@Override
	public T call(){
		try{
			return super.call();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

}
