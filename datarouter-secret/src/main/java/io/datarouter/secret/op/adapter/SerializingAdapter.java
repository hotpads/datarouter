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
package io.datarouter.secret.op.adapter;

import io.datarouter.secret.client.Secret;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.service.SecretJsonSerializer;
import io.datarouter.util.Require;

/**
 * Adapts a TypedSecret to a Secret by serializing its value. Serialization can only be skipped for Strings.
 * @param <T> type of TypedSecret input
 */
public class SerializingAdapter<T> implements SecretOpAdapter<TypedSecret<T>,Secret>{

	private final SecretJsonSerializer jsonSerializer;
	private final Boolean shouldSkipSerialization;

	public SerializingAdapter(SecretJsonSerializer jsonSerializer, SecretOpConfig config){
		this.jsonSerializer = jsonSerializer;
		this.shouldSkipSerialization = config.shouldSkipSerialization;
		Require.isTrue(config.shouldSkipSerialization || jsonSerializer != null);
	}

	@Override
	public Secret adapt(TypedSecret<T> input){
		if(input.getValue().getClass() == String.class && shouldSkipSerialization){
			return new Secret(input.getName(), (String)input.getValue());
		}
		return new Secret(input.getName(), jsonSerializer.serialize(input.getValue()));
	}

}