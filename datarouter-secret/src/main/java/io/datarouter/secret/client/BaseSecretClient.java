/**
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
import java.util.function.Supplier;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.secret.exception.SecretClientException;
import io.datarouter.secret.exception.SecretValidationException;

/**
 * This class adds a layer of logging and {@link Counters} to make sure problems with {@link Secret}s are properly
 * exposed, even if a user improperly handles the thrown exceptions.
 */
public abstract class BaseSecretClient implements SecretClient{

	protected abstract void createInternal(Secret secret);
	protected abstract Secret readInternal(String name);
	protected abstract List<String> listInternal(Optional<String> exclusivePrefix);
	protected abstract void updateInternal(Secret secret);
	protected abstract void deleteInternal(String name);

	protected void validateNameInternal(String name){
		Secret.validateName(name);
	}

	protected void validateSecretInternal(Secret secret){
		Secret.validateSecret(secret);
	}

	private <T> SecretClientOpResult<T> validateAndExecute(
			Supplier<Void> validationMethod,
			String validationCounterName,
			Supplier<T> opMethod,
			String opCounterName){
		try{
			validationMethod.get();
			countSuccess(validationCounterName);
		}catch(RuntimeException e){
			countError(validationCounterName, e);
			return SecretClientOpResult.validationError(new SecretValidationException(e));
		}
		return execute(opMethod, opCounterName);
	}

	private <T> SecretClientOpResult<T> execute(Supplier<T> opMethod, String opCounterName){
		try{
			T opResult = opMethod.get();
			countSuccess(opCounterName);
			return SecretClientOpResult.opSuccess(opResult);
		}catch(SecretClientException e){
			countError(opCounterName, e);
			return SecretClientOpResult.opError(e);
		}
	}

	@Override
	public final SecretClientOpResult<Void> create(Secret secret){
		return validateAndExecute(
				() -> validateSecret(secret),
				"validateSecret",
				() -> {
					createInternal(secret);
					return null;
				},
				"create");
	}

	@Override
	public final SecretClientOpResult<Secret> read(String name){
		return validateAndExecute(
				() -> validateName(name),
				"validateName",
				() -> readInternal(name),
				"read");
	}

	@Override
	public SecretClientOpResult<Void> update(Secret secret){
		return validateAndExecute(
				() -> validateSecret(secret),
				"validateSecret",
				() -> {
					updateInternal(secret);
					return null;
				},
				"update");
	}

	@Override
	public final SecretClientOpResult<Void> delete(String name){
		return validateAndExecute(
				() -> validateName(name),
				"validateName",
				() -> {
					deleteInternal(name);
					return null;
				},
				"delete");
	}

	@Override
	public final SecretClientOpResult<List<String>> listNames(Optional<String> exclusivePrefix){
		return execute(
				() -> listInternal(exclusivePrefix),
				"list");
	}

	@Override
	public final Void validateSecret(Secret secret){
		validateSecretInternal(secret);
		return null;
	}

	@Override
	public final Void validateName(String name){
		validateNameInternal(name);
		return null;
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

}
