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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.secret.exception.SecretClientException;
import io.datarouter.secret.exception.SecretValidationException;
import io.datarouter.secret.op.SecretOpType;

public abstract class SecretClientOp<I,O>{

	private final String validationCounterName;
	private final String opCounterName;
	private final Optional<BiConsumer<SecretClient,I>> validationMethod;
	private final BiFunction<SecretClient,I,O> opMethod;
	private final SecretOpType opType;

	public SecretClientOp(String opCounterName, BiFunction<SecretClient,I,O> opMethod, SecretOpType opType){
		this(null, opCounterName, Optional.empty(), opMethod, opType);
	}

	public SecretClientOp(String validationCounterName, String opCounterName,
			BiConsumer<SecretClient,I> validationMethod, BiFunction<SecretClient,I,O> opMethod, SecretOpType opType){
		this(validationCounterName, opCounterName, Optional.of(validationMethod), opMethod, opType);
	}

	private SecretClientOp(String validationCounterName, String opCounterName,
			Optional<BiConsumer<SecretClient,I>> validationMethod, BiFunction<SecretClient,I,O> opMethod,
			SecretOpType opType){
		this.validationCounterName = validationCounterName;
		this.opCounterName = opCounterName;
		this.validationMethod = validationMethod;
		this.opMethod = opMethod;
		this.opType = opType;
	}

	public SecretOpType getOpType(){
		return opType;
	}

	public SecretClientOpResult<O> validateAndExecute(SecretClient secretClient, I input){
		try{
			validationMethod.ifPresent(validator -> {
				validator.accept(secretClient, input);
				countSuccess(validationCounterName);
			});
		}catch(RuntimeException e){
			countError(validationCounterName, e);
			return SecretClientOpResult.validationError(new SecretValidationException(e));
		}
		return execute(secretClient, input);
	}

	private SecretClientOpResult<O> execute(SecretClient secretClient, I input){
		try{
			O opResult = opMethod.apply(secretClient, input);
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

	static <I> BiFunction<SecretClient,I,Void> wrapVoid(BiConsumer<SecretClient,I> function){
		return (secretClient, input) -> {
			function.accept(secretClient, input);
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
