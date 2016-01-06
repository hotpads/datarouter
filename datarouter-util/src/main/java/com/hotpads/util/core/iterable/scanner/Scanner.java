package com.hotpads.util.core.iterable.scanner;

import java.util.Optional;

//Alternative to Iterator when it is hard to do hasNext()
public interface Scanner<T>{

	T getCurrent();

	/*
	 * Return true if the current value was advanced, otherwise false.  Repeated calls after the initial false should
	 * continue to return false without side effects.
	 */
	boolean advance();

	/**
	 * @return true if the scanner was advanced by the offset
	 */
	default boolean advanceBy(Integer count){
		for(int i = 0 ; i < Optional.ofNullable(count).orElse(0) ; i++){
			if(!advance()){
				return false;
			}
		}
		return true;
	}

}
