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
package io.datarouter.secret.client;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.secret.exception.SecretClientException;
import io.datarouter.secret.exception.SecretValidationException;

public abstract class SecretClientOp<I,O>{

	private final String validationCounterName;
	private final String opCounterName;
	private final Optional<Consumer<I>> validationMethod;
	private final Function<I,O> opMethod;

	public SecretClientOp(String opCounterName, Function<I,O> opMethod){
		this(null, opCounterName, Optional.empty(), opMethod);
	}

	public SecretClientOp(String validationCounterName, String opCounterName, Consumer<I> validationMethod,
			Function<I,O> opMethod){
		this(validationCounterName, opCounterName, Optional.of(validationMethod), opMethod);
	}

	private SecretClientOp(String validationCounterName, String opCounterName,
			Optional<Consumer<I>> validationMethod, Function<I,O> opMethod){
		this.validationCounterName = validationCounterName;
		this.opCounterName = opCounterName;
		this.validationMethod = validationMethod;
		this.opMethod = opMethod;
	}

	public SecretClientOpResult<O> validateAndExecute(I input){
		try{
			validationMethod.ifPresent(validator -> {
				validator.accept(input);
				countSuccess(validationCounterName);
			});
		}catch(RuntimeException e){
			countError(validationCounterName, e);
			return SecretClientOpResult.validationError(new SecretValidationException(e));
		}
		return execute(input);
	}

	private SecretClientOpResult<O> execute(I input){
		try{
			O opResult = opMethod.apply(input);
			countSuccess(opCounterName);
			return SecretClientOpResult.opSuccess(opResult);
		}catch(SecretClientException e){
			countError(opCounterName, e);
			return SecretClientOpResult.opError(e);
		}catch(RuntimeException e){
			countError(opCounterName, e);
			return SecretClientOpResult.opError(e);
		}
	}

	private void countSuccess(String operation){
		count(List.of(
				"",
				"success",
				operation,
				operation + " success"));
	}

	private void countError(String operation, RuntimeException exc){
		count(List.of(
				"",
				"error",
				"error " + exc.getClass().getCanonicalName(),
				operation,
				operation + " error",
				operation + " error " + exc.getClass().getCanonicalName()));
	}

	private void count(List<String> suffixes){
		String prefix = "Datarouter secret " + this.getClass().getSimpleName() + " ";
		suffixes.stream()
				.map(prefix::concat)
				.forEach(Counters::inc);
	}

	static <I> Function<I,Void> wrapVoid(Consumer<I> function){
		return input -> {
			function.accept(input);
			return null;
		};
	}

	public static class SecretClientOpResult<T>{

		public static enum SecretClientOpStatus{
			SUCCESS,
			VALIDATION_ERROR,
			OP_ERROR
		}

		public final SecretClientOpStatus status;
		public final Optional<T> result;
		public final Optional<SecretClientException> exception;

		private SecretClientOpResult(SecretClientOpStatus status, Optional<T> result,
				Optional<SecretClientException> exception){
			this.status = status;
			this.result = result;
			this.exception = exception;
		}

		public static <T> SecretClientOpResult<T> validationError(SecretValidationException validationException){
			return new SecretClientOpResult<>(
					SecretClientOpStatus.VALIDATION_ERROR,
					Optional.empty(),
					Optional.of(validationException));
		}

		public static <T> SecretClientOpResult<T> opSuccess(T result){
			return new SecretClientOpResult<>(
					SecretClientOpStatus.SUCCESS,
					Optional.ofNullable(result),
					Optional.empty());
		}

		public static <T> SecretClientOpResult<T> opError(SecretClientException opException){
			return new SecretClientOpResult<>(
					SecretClientOpStatus.OP_ERROR,
					Optional.empty(),
					Optional.of(opException));
		}

		public static <T> SecretClientOpResult<T> opError(RuntimeException opException){
			return opError(new SecretClientException("uncaught SecretClient error", opException));
		}

		public Boolean isSuccess(){
			return this.status == SecretClientOpStatus.SUCCESS;
		}

	}

}
