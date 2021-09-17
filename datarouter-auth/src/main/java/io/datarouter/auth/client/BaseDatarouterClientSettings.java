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
package io.datarouter.auth.client;

import java.net.URI;

import io.datarouter.auth.service.DefaultDatarouterAccountKeysSupplier;
import io.datarouter.httpclient.client.DatarouterHttpClientSettings;
import io.datarouter.instrumentation.refreshable.RefreshableSupplier;
import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.storage.setting.cached.impl.StringCachedSetting.RefreshableStringCachedSetting;

public abstract class BaseDatarouterClientSettings
extends BaseSimpleDatarouterClientSettings
implements DatarouterHttpClientSettings{

	public final CachedSetting<String> endpointDomain;
	public final CachedSetting<String> endpointPath;
	public final CachedSetting<String> apiKey;
	public final CachedSetting<String> privateKey;
	public final RefreshableSupplier<String> refreshableApiKey;
	public final RefreshableSupplier<String> refreshablePrivateKey;

	//This newer constructor doesn't need DatarouterService
	public BaseDatarouterClientSettings(
			SettingFinder finder,
			DefaultDatarouterAccountKeysSupplier defaultDatarouterAccountKeys,
			String settingNodeName,
			String contextPath,
			DefaultSettingValue<String> endpointDomainDefaults){
		super(finder, settingNodeName);
		endpointDomain = registerStrings("endpointDomain", endpointDomainDefaults);
		endpointPath = registerString("endpointPath", contextPath);
		apiKey = registerString("apiKey", defaultDatarouterAccountKeys.getDefaultApiKey());
		privateKey = registerString("privateKey", defaultDatarouterAccountKeys.getDefaultSecretKey());
		refreshableApiKey = new RefreshableStringCachedSetting(apiKey);
		refreshablePrivateKey = new RefreshableStringCachedSetting(privateKey);
	}

	@Override
	public URI getEndpointUrl(){
		return URI.create("https://" + endpointDomain.get() + endpointPath.get());
	}

	@Override
	public String getApiKey(){
		return apiKey.get();
	}

	@Override
	public RefreshableSupplier<String> getRefreshableApiKey(){
		return refreshableApiKey;
	}

	@Override
	public String getPrivateKey(){
		return privateKey.get();
	}

	@Override
	public RefreshableSupplier<String> getRefreshablePrivateKey(){
		return refreshablePrivateKey;
	}

}
