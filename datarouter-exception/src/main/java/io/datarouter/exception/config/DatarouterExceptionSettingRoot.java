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
package io.datarouter.exception.config;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.DatarouterSettingTagType;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterExceptionSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> forceHideStackTrace;
	public final CachedSetting<String> exceptionRecorderDomainName;

	public final CachedSetting<Boolean> publishRecords;

	public final CachedSetting<Boolean> publishNonProdDataToSharedQueue;

	@Inject
	public DatarouterExceptionSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterException.");

		forceHideStackTrace = registerBoolean("forceHideStackTrace", false);
		exceptionRecorderDomainName = registerString("exceptionRecorderDomainName", "localhost:8443");

		publishRecords = registerBooleans("publishRecords", defaultTo(false)
				.withTag(DatarouterSettingTagType.EXCEPTION_PIPELINE, () -> true));

		publishNonProdDataToSharedQueue = registerBoolean("publishNonProdDataToSharedQueue", false);
	}

}
