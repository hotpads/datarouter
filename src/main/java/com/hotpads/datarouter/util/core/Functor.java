package com.hotpads.datarouter.util.core;


public interface Functor<Return, Parameter> {
	Return invoke(Parameter param);
}
