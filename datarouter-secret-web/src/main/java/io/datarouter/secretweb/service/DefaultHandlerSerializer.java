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
package io.datarouter.secretweb.service;

import java.lang.reflect.Type;

import io.datarouter.json.JsonSerializer;
import io.datarouter.secret.service.SecretJsonSerializer;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class DefaultHandlerSerializer implements SecretJsonSerializer{

	@Inject
	@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER)
	private JsonSerializer jsonSerializer;

	@Override
	public <T> String serialize(T toSerialize){
		return jsonSerializer.serialize(toSerialize);
	}

	@Override
	public <T> T deserialize(String toDeserialize, Type classOfT){
		return jsonSerializer.deserialize(toDeserialize, classOfT);
	}

}
