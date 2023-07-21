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
package io.datarouter.gson;

import com.google.gson.Gson;

import io.datarouter.gson.typeadapterfactory.OptionalContainerClassTypeAdapterFactory;
import io.datarouter.gson.typeadapterfactory.OptionalTypeAdapterFactory;
import jakarta.inject.Singleton;

@Singleton
public class OptionalContainerSerializer extends GsonJsonSerializer{

	private static final Gson READ_GSON = GsonTool.GSON.newBuilder()
			.registerTypeAdapterFactory(new OptionalContainerClassTypeAdapterFactory())
			.registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
			.serializeNulls()
			.create();

	public OptionalContainerSerializer(){
		super(READ_GSON);
	}

}
