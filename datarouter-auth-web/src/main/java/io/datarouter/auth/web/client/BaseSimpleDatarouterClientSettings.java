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
package io.datarouter.auth.web.client;

import java.time.Duration;
import java.util.function.Supplier;

import io.datarouter.httpclient.client.DatarouterHttpClientBuilder;
import io.datarouter.httpclient.client.HttpRetryTool;
import io.datarouter.httpclient.client.SimpleDatarouterHttpClientSettings;
import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.duration.DatarouterDuration;

public abstract class BaseSimpleDatarouterClientSettings
extends SettingNode
implements SimpleDatarouterHttpClientSettings{

	public final CachedSetting<DatarouterDuration> timeout;
	public final CachedSetting<Integer> numRetries;
	public final CachedSetting<Boolean> enableBreakers;
	public final Setting<Boolean> traceInQueryString;
	public final Setting<Boolean> debugLog;

	public BaseSimpleDatarouterClientSettings(
			SettingFinder finder,
			String settingNodeName){
		super(finder, settingNodeName);
		timeout = registerDurations("timeout", getTimeoutDefaultSettingValue());
		numRetries = registerIntegers("numRetries", getNumRetriesDefault());
		enableBreakers = registerBooleans("enableBreakers", getEnableBreakersDefault());
		traceInQueryString = registerBoolean("traceInQueryString", false);
		debugLog = registerBoolean("debugLog", false);
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

	@Override
	public Setting<Boolean> getTraceInQueryString(){
		return traceInQueryString;
	}

	@Override
	public Setting<Boolean> getDebugLog(){
		return debugLog;
	}

	protected Duration getTimeoutDefault(){
		return DatarouterHttpClientBuilder.DEFAULT_TIMEOUT;
	}

	protected DefaultSettingValue<Integer> getNumRetriesDefault(){
		return defaultTo(HttpRetryTool.DEFAULT_RETRY_COUNT);
	}

	protected DefaultSettingValue<Boolean> getEnableBreakersDefault(){
		return defaultTo(false);
	}

	protected DefaultSettingValue<DatarouterDuration> getTimeoutDefaultSettingValue(){
		return defaultTo(new DatarouterDuration(getTimeoutDefault()));
	}

}
