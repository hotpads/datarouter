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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.secret.exception.SecretClientException;
import io.datarouter.secret.exception.SecretValidationException;

/**
 * This class adds a layer of logging and {@link Counters} to make sure problems with {@link Secret}s are properly
 * exposed, even if a user improperly handles the thrown exceptions.
 */
public abstract class BaseSecretClient implements SecretClient{
	private static final Logger logger = LoggerFactory.getLogger(BaseSecretClient.class);

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

	@Override
	public final void create(Secret secret){
		create(secret, true);
	}

	@Override
	public final void create(Secret secret, boolean shouldReportError){
		validateSecret(secret);
		try{
			createInternal(secret);
			countSuccess("create");
		}catch(SecretClientException e){
			if(shouldReportError){
				reportError("create", e);
			}
			throw e;
		}
	}

	@Override
	public final Secret read(String name){
		validateName(name);
		try{
			Secret secret = readInternal(name);
			countSuccess("read");
			return secret;
		}catch(SecretClientException e){
			reportError("read", e);
			throw e;
		}
	}

	@Override
	public void update(Secret secret){
		validateSecret(secret);
		try{
			updateInternal(secret);
			countSuccess("update");
		}catch(SecretClientException e){
			reportError("update", e);
			throw e;
		}
	}

	@Override
	public final void delete(String name){
		validateName(name);
		try{
			deleteInternal(name);
			countSuccess("delete");
		}catch(SecretClientException e){
			reportError("delete", e);
			throw e;
		}
	}

	@Override
	public final List<String> listNames(Optional<String> exclusivePrefix){
		try{
			List<String> names = listInternal(exclusivePrefix);
			countSuccess("list");
			return names;
		}catch(SecretClientException e){
			reportError("list", e);
			throw e;
		}
	}

	@Override
	public final void validateSecret(Secret secret){
		try{
			validateSecretInternal(secret);
			countSuccess("validateSecret");
		}catch(RuntimeException e){
			reportError("validateSecret", e);
			throw new SecretValidationException(e);
		}
	}

	@Override
	public final void validateName(String name){
		try{
			validateNameInternal(name);
			countSuccess("validateName");
		}catch(RuntimeException e){
			reportError("validateName", e);
			throw new SecretValidationException(e);
		}
	}

	private void countSuccess(String operation){
		count(List.of(
				"",
				"success",
				operation,
				operation + " success"));
	}

	private void reportError(String operation, RuntimeException exc){
		countError(operation, exc);
		logError(operation, exc);
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

	private void logError(String operation, RuntimeException exc){
		logger.warn(this.getClass().getSimpleName() + " failed operation=" + operation, exc);
	}

}
