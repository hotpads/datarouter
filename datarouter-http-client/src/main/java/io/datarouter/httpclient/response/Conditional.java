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
package io.datarouter.httpclient.response;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Response wrapper object that allows the caller to avoid try/catch blocks
 */
public class Conditional<T>{

	private final boolean success;
	private final T response;
	private final Exception exception;

	private Conditional(boolean success, T response, Exception exception){
		this.success = success;
		this.response = response;
		this.exception = exception;
	}

	public static <T> Conditional<T> success(T response){
		return new Conditional<>(true, response, null);
	}

	public static <T> Conditional<T> failure(Exception exception){
		return failure(exception, null);
	}

	public static <T> Conditional<T> failure(Exception exception, T failureResponse){
		return new Conditional<>(false, failureResponse, exception);
	}

	public <U> Conditional<U> map(Function<? super T,? extends U> mapper){
		if(success){
			return Conditional.success(mapper.apply(response));
		}
		return Conditional.failure(exception);
	}

	public T orElseThrow(){
		if(success){
			return response;
		}
		if(exception instanceof RuntimeException){
			throw (RuntimeException)exception;
		}
		throw new RuntimeException(exception);
	}

	public T orElseThrow(Function<Exception,RuntimeException> mapper){
		if(success){
			return response;
		}
		throw mapper.apply(exception);
	}

	public T orElse(T alternative){
		return success ? response : alternative;
	}

	public T orElseGet(Function<Exception,T> alternative){
		if(success){
			return response;
		}
		return alternative.apply(exception);
	}

	public boolean isFailure(){
		return !success;
	}

	public boolean isSuccess(){
		return success;
	}

	public Conditional<T> ifSuccess(Consumer<? super T> consumer){
		if(success){
			consumer.accept(response);
		}
		return this;
	}

	public Conditional<T> ifFailure(Consumer<Exception> consumer){
		if(!success){
			consumer.accept(exception);
		}
		return this;
	}

	public Exception getException(){
		return exception;
	}

	@Override
	public String toString(){
		return "Conditional [success=" + success + ", response=" + response + ", exception=" + exception + "]";
	}

}
