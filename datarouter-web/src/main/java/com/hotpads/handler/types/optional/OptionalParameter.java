package com.hotpads.handler.types.optional;

import java.lang.reflect.Type;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class OptionalParameter<T>{
	private static final ImmutableSet<Class<? extends OptionalParameter<?>>> OPTIONAL_PARAMATER_TYPES = ImmutableSet.of(
			OptionalBoolean.class, OptionalDouble.class, OptionalInteger.class, OptionalLong.class,
			OptionalString.class);

	protected final Optional<T> opt;

	public OptionalParameter(){
		this.opt = Optional.empty();
	}
	public OptionalParameter(T value){
		this.opt = Optional.ofNullable(value);
	}

	public Optional<T> getOptional(){
		return opt;
	}

	public abstract Class<?> getInternalType();
	public abstract OptionalParameter<T> fromString(String stringValue);

	public static OptionalParameter<?> makeOptionalParameter(String stringValue, Type type){
		for(Class<? extends OptionalParameter<?>> optClass : OPTIONAL_PARAMATER_TYPES){
			if(optClass.equals(type)){
				return ReflectionTool.create(optClass).fromString(stringValue);
			}
		}
		return new OptionalString();
	}

	public static Class<?> getOptionalInternalType(Class<?> parameterClass){
		for(Class<? extends OptionalParameter<?>> optClass : OPTIONAL_PARAMATER_TYPES){
			if(optClass.equals(parameterClass)){
				OptionalParameter<?> optParameter = ReflectionTool.create(optClass);
				return optParameter.getInternalType();
			}
		}
		return parameterClass;
	}

}