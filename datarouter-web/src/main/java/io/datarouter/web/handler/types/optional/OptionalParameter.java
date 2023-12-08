/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.web.handler.types.optional;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import io.datarouter.util.lang.ReflectionTool;

/**
 * @deprecated use the JDK {@code Optional<T>}
 */
@Deprecated
public abstract class OptionalParameter<T>{

	private static final Set<Class<? extends OptionalParameter<?>>> OPTIONAL_PARAMATER_TYPES = Set.of(
			OptionalBoolean.class,
			OptionalInteger.class,
			OptionalString.class,
			OptionalDouble.class,
			OptionalLong.class);

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

	public abstract Class<T> getInternalType();

	public abstract OptionalParameter<T> fromString(String stringValue, Method method, Parameter parameter);

	public static OptionalParameter<?> makeOptionalParameter(
			String stringValue,
			Type type,
			Method method,
			Parameter parameter){
		for(Class<? extends OptionalParameter<?>> optClass : OPTIONAL_PARAMATER_TYPES){
			if(optClass.equals(type)){
				return ReflectionTool.create(optClass).fromString(stringValue, method, parameter);
			}
		}
		return new OptionalString();
	}

	public static Type getOptionalInternalType(Type parameterClass){
		for(Class<? extends OptionalParameter<?>> optClass : OPTIONAL_PARAMATER_TYPES){
			if(optClass.equals(parameterClass)){
				OptionalParameter<?> optParameter = ReflectionTool.create(optClass);
				return optParameter.getInternalType();
			}
		}
		return parameterClass;
	}

	/*-------------- pass through methods ----------------*/

	public T get(){
		return opt.get();
	}

	public void ifPresent(Consumer<? super T> consumer){
		opt.ifPresent(consumer);
	}

	public boolean isPresent(){
		return opt.isPresent();
	}

	public boolean isEmpty(){
		return opt.isEmpty();
	}

	public T orElse(T other){
		return opt.orElse(other);
	}

	public <U> Optional<U> map(Function<? super T,? extends U> mapper){
		return opt.map(mapper);
	}

	public Optional<T> filter(Predicate<? super T> predicate){
		return opt.filter(predicate);
	}

	/*--------------- Object ------------------*/

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj instanceof Optional){
			return Objects.equals(opt, obj);
		}
		if(obj instanceof OptionalParameter<?> other){
			return Objects.equals(opt, other.opt);
		}
		return false;
	}

	@Override
	public int hashCode(){
		return opt.hashCode();
	}

	@Override
	public String toString(){
		return opt.toString();
	}

}
