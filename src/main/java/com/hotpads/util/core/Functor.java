package com.hotpads.util.core;


public interface Functor<Return, Parameter> {
	Return invoke(Parameter param);
}
