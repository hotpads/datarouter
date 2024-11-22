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

import com.google.gson.JsonSyntaxException;

import io.datarouter.secret.client.Secret;
import io.datarouter.secret.exception.SecretClientException;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.service.SecretJsonSerializer;
import io.datarouter.util.Require;

/**
 * Adapts a Secret to a TypedSecret by deserializing its value. Deserialization can only be skipped for Strings.
 * @param <T> type of TypedSecret input
 */
public class DeserializingAdapter<T> implements SecretOpAdapter<Secret,TypedSecret<T>>{

	private final SecretJsonSerializer jsonSerializer;
	private final Class<T> classOfT;
	private final Boolean shouldSkipSerialization;

	public DeserializingAdapter(SecretJsonSerializer jsonSerializer, Class<T> classOfT, SecretOpConfig config){
		this.jsonSerializer = jsonSerializer;
		this.classOfT = Require.notNull(classOfT);
		this.shouldSkipSerialization = config.shouldSkipSerialization;
		Require.isTrue(config.shouldSkipSerialization && classOfT == String.class || jsonSerializer != null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypedSecret<T> adapt(Secret input){
		if(classOfT == String.class && shouldSkipSerialization){
			return (TypedSecret<T>)new TypedSecret<>(input.getName(), input.getValue());
		}
		try{
			return new TypedSecret<>(input.getName(), jsonSerializer.deserialize(input.getValue(), classOfT));
		}catch(JsonSyntaxException e){
			//hide the exception to avoid logging secret value
			throw new SecretClientException("failed to deserialize secret name=" + input.getName());
		}
	}

}
