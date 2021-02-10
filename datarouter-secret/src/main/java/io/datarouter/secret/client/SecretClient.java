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

import io.datarouter.secret.exception.SecretClientException;
import io.datarouter.secret.exception.SecretValidationException;
import io.datarouter.secret.service.CachedSecretFactory;
import io.datarouter.secret.service.CachedSecretFactory.CachedSecret;
import io.datarouter.secret.service.SecretService;

//TODO maybe split into read only and read/write versions?
//TODO would an interface even make sense for the latter? consider java access control for one specific class instead.
/**
 * This is an interface that enables simple CRUD methods for {@link Secret} storage without any namespacing logic.
 *
 * The recommended way to use this interface is to implement use {@link CachedSecretFactory} to obtain a
 * {@link CachedSecret} for reading. If more than just reading is required, {@link SecretService} should be used.
 * This interface should be used directly only as a last resort or for migrations across namespaces.
 */
public interface SecretClient{

	public static enum SecretClientOpStatus{
		SUCCESS,
		VALIDATION_ERROR,
		OP_ERROR
	}

	public static class SecretClientOpResult<T>{

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

		public Boolean isSuccess(){
			return this.status == SecretClientOpStatus.SUCCESS;
		}

	}

	/**
	 * write the specified {@link Secret} to the secret storage for the first time
	 */
	SecretClientOpResult<Void> create(Secret secret);

	/**
	 * create a {@link Secret} as specified, then write it to the secret storage for the first time
	 */
	default SecretClientOpResult<Void> create(String name, String value){
		return create(new Secret(name, value));
	}

	/**
	 * read the {@link Secret} with the given name and return a {@link Secret}
	 */
	SecretClientOpResult<Secret> read(String name);

	/**
	 * returns the full {@link Secret} names that start with exclusive prefix
	 */
	SecretClientOpResult<List<String>> listNames(Optional<String> exclusivePrefix);

	/**
	 * update the current value of the {@link Secret} in the secret storage
	 */
	SecretClientOpResult<Void> update(Secret secret);

	/**
	 * create a {@link Secret} as specified, then update the current value of it in the secret storage
	 */
	default SecretClientOpResult<Void> update(String name, String value){
		return update(new Secret(name, value));
	}

	/**
	 * delete the named {@link Secret} from the secret storage
	 */
	SecretClientOpResult<Void> delete(String name);

	/**
	 * validate the provided name according to the rules of the secret storage
	 */
	Void validateName(String name);

	/**
	 * validate the provided {@link Secret} according to the rules of the secret storage
	 */
	Void validateSecret(Secret secret);

}
