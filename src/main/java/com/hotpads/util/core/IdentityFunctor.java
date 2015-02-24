package com.hotpads.util.core;


public class IdentityFunctor<T> implements Functor<T,T>{
	@Override
	public T invoke(T param) {
		return param;
	}
}
