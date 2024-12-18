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
package io.datarouter.joblet.codec;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;

import io.datarouter.gson.DatarouterGsons;

public abstract class BaseGsonJobletCodec<P> implements JobletCodec<P>{

	private final Type paramsType;

	public BaseGsonJobletCodec(Class<P> paramsClass){
		this.paramsType = paramsClass;
	}

	public BaseGsonJobletCodec(TypeToken<P> paramsTypeToken){
		this.paramsType = paramsTypeToken.getType();
	}

	@SuppressWarnings("deprecation")
	@Override
	public String marshallData(P params){
		return DatarouterGsons.withUnregisteredEnums().toJson(params);
	}

	@SuppressWarnings("deprecation")
	@Override
	public P unmarshallData(String encodedParams){
		return DatarouterGsons.withUnregisteredEnums().fromJson(encodedParams, paramsType);
	}

}
