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
package io.datarouter.auth.service;

import javax.inject.Singleton;

import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;

@Singleton
public class DefaultDatarouterAccountKeys implements DefaultDatarouterAccountKeysSupplier{

	private final String defaultApiKey;
	private final String defaultSecretKey;

	public DefaultDatarouterAccountKeys(String defaultApiKey, String defaultSecretKey){
		Require.isTrue(StringTool.notEmpty(defaultApiKey), "defaultApiKey cannot be empty");
		Require.isTrue(StringTool.notEmpty(defaultSecretKey), "defaultSecretKey cannot be empty");
		this.defaultApiKey = defaultApiKey;
		this.defaultSecretKey = defaultSecretKey;
	}

	@Override
	public String getDefaultApiKey(){
		return defaultApiKey;
	}

	@Override
	public String getDefaultSecretKey(){
		return defaultSecretKey;
	}

}
