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
package io.datarouter.storage.setting.cached.impl;

import java.time.Duration;

import io.datarouter.instrumentation.refreshable.BaseMemoizedRefreshableSupplier;
import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.storage.setting.type.StringSetting;
import io.datarouter.storage.setting.validator.StringSettingValidator;

public class StringCachedSetting extends CachedSetting<String> implements StringSetting{

	public StringCachedSetting(SettingFinder finder, String name, DefaultSettingValue<String> defaultValue){
		super(finder, name, defaultValue, new StringSettingValidator());
	}

	public static class RefreshableStringCachedSetting extends BaseMemoizedRefreshableSupplier<String>{

		private final CachedSetting<String> setting;

		public RefreshableStringCachedSetting(CachedSetting<String> setting){
			this(setting, Duration.ofSeconds(30L));
		}

		public RefreshableStringCachedSetting(CachedSetting<String> setting, Duration minimumTtl){
			this(setting, minimumTtl, minimumTtl);
		}

		public RefreshableStringCachedSetting(
				CachedSetting<String> setting,
				Duration minimumTtl,
				Duration attemptInterval){
			super(minimumTtl, attemptInterval);
			this.setting = setting;
			refresh();
		}

		@Override
		protected String readNewValue(){
			setting.expire();
			return setting.get();
		}

		@Override
		protected String getIdentifier(){
			return setting.getName();
		}

	}

}
