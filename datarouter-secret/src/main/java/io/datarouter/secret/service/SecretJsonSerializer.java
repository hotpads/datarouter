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
package io.datarouter.secret.service;

import java.lang.reflect.Type;

import io.datarouter.gson.GsonTool;
import jakarta.inject.Singleton;

public interface SecretJsonSerializer{

	<T> String serialize(T toSerialize);
	<T> T deserialize(String toDeserialize, Type classOfT);

	@Singleton
	public static class GsonToolJsonSerializer implements SecretJsonSerializer{

		@Override
		public <T> String serialize(T toSerialize){
			return GsonTool.withUnregisteredEnums().toJson(toSerialize);
		}

		@Override
		public <T> T deserialize(String toDeserialize, Type classOfT){
			return GsonTool.withUnregisteredEnums().fromJson(toDeserialize, classOfT);
		}

	}

}
