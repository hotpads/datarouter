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
import java.util.function.Supplier;

import io.datarouter.secret.service.CachedSecretFactory;
import io.datarouter.secret.service.CachedSecretFactory.CachedSecret;
import io.datarouter.secret.service.SecretService;

/**
 * This is an interface that enables simple CRUD methods for {@link Secret} storage without any namespacing logic.
 *
 * The recommended way to use this interface is to implement use {@link CachedSecretFactory} to obtain a
 * {@link CachedSecret} for reading. If more than just reading is required, {@link SecretService} should be used.
 * This interface should be used directly only as a last resort or for migrations across namespaces.
 */
public interface SecretClient{

	/**
	 * write the specified {@link Secret} to the secret storage for the first time
	 */
	void create(Secret secret);

	/**
	 * create a {@link Secret} as specified, then write it to the secret storage for the first time
	 */
	default void create(String name, String value){
		create(new Secret(name, value));
	}

	/**
	 * read the {@link Secret} with the given name and return a {@link Secret}
	 */
	Secret read(String name);

	/**
	 * returns the full {@link Secret} names that start with prefix
	 */
	List<String> listNames(Optional<String> prefix);

	/**
	 * update the current value of the {@link Secret} in the secret storage
	 */
	void update(Secret secret);

	/**
	 * create a {@link Secret} as specified, then update the current value of it in the secret storage
	 */
	default void update(String name, String value){
		update(new Secret(name, value));
	}

	/**
	 * delete the named {@link Secret} from the secret storage
	 */
	void delete(String name);

	/**
	 * validate the provided name according to the rules of the secret storage
	 */
	default void validateName(String name){
		Secret.validateName(name);
	}

	/**
	 * validate the provided {@link Secret} according to the rules of the secret storage
	 */
	default void validateSecret(Secret secret){
		Secret.validateSecret(secret);
		validateName(secret.getName());
	}

	interface SecretClientSupplier extends Supplier<SecretClient>{
	}

}
