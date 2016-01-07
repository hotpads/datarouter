package com.hotpads.datarouter.util.core;

import com.hotpads.util.core.Functor;


public class DrIdentityFunctor<T> implements Functor<T,T>{
	@Override
	public T invoke(T param) {
		return param;
	}
}
