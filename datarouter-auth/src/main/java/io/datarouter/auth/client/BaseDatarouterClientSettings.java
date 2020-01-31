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
package io.datarouter.auth.client;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.datarouter.auth.service.DefaultDatarouterAccountKeysSupplier;
import io.datarouter.httpclient.client.DatarouterHttpClientBuilder;
import io.datarouter.httpclient.client.DatarouterHttpClientSettings;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.httpclient.client.HttpRetryTool;
import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.duration.DatarouterDuration;

public abstract class BaseDatarouterClientSettings extends SettingNode implements DatarouterHttpClientSettings{

	public final CachedSetting<String> endpointDomain;
	public final CachedSetting<String> endpointPath;
	public final CachedSetting<String> apiKey;
	public final CachedSetting<String> privateKey;
	public final CachedSetting<DatarouterDuration> timeout;
	public final CachedSetting<Integer> numRetries;
	public final CachedSetting<Boolean> enableBreakers;

	public BaseDatarouterClientSettings(
			SettingFinder finder,
			DefaultDatarouterAccountKeysSupplier defaultDatarouterAccountKeys,
			String settingNodeName,
			DatarouterService service,
			DefaultSettingValue<String> endpointDomainDefaults){
		super(finder, settingNodeName);
		endpointDomain = registerStrings("endpointDomain", endpointDomainDefaults);
		endpointPath = registerString("endpointPath", service.getContextPath());
		apiKey = registerString("apiKey", defaultDatarouterAccountKeys.getDefaultApiKey());
		privateKey = registerString("privateKey", defaultDatarouterAccountKeys.getDefaultSecretKey());
		timeout = registerDurations("timeout", defaultTo(new DatarouterDuration(getTimeoutDefault().toMillis(),
				TimeUnit.MILLISECONDS)));
		numRetries = registerIntegers("numRetries", defaultTo(getNumRetriesDefault()));
		enableBreakers = registerBooleans("enableBreakers", getEnableBreakersDefault());
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
	public String getPrivateKey(){
		return privateKey.get();
	}

	@Override
	public final Duration getTimeout(){
		return timeout.get().toJavaDuration();
	}

	@Override
	public final Supplier<Integer> getNumRetries(){
		return numRetries;
	}

	@Override
	public final Supplier<Boolean> getEnableBreakers(){
		return enableBreakers;
	}

	protected Duration getTimeoutDefault(){
		return DatarouterHttpClientBuilder.DEFAULT_TIMEOUT;
	}

	protected Integer getNumRetriesDefault(){
		return HttpRetryTool.DEFAULT_RETRY_COUNT;
	}

	protected DefaultSettingValue<Boolean> getEnableBreakersDefault(){
		return defaultTo(false);
	}

}
