package com.hotpads.util.core.collections;


@SuppressWarnings("serial")
public class Twin<T> extends Pair<T,T>{
	
	public Twin(){
		super();
	}
	
	public Twin(T left, T right){
		super(left, right);
	}
	
	public static <T> Twin<T> createTwin(T left, T right){
		return new Twin<T>(left, right);
	}
}
