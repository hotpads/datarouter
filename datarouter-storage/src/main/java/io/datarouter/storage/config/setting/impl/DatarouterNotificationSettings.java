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
package io.datarouter.storage.config.setting.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;

@Singleton
public class DatarouterNotificationSettings extends SettingNode{

	public final Setting<String> apiEndPoint;

	public final Setting<Boolean> forceHideStackTrace;
	public final Setting<String> errorNotificationRecipient;
    public final Setting<Integer> deleteNotificationRequestBatchCount;
	public final Setting<Boolean> writeToNotificationRequest2;
	public final Setting<Boolean> readFromNotificationRequest2;
	public final Setting<Boolean> useMaxPerDayTimingStrategy;
	public final Setting<Boolean> writeToNotificationRequest3;
	public final Setting<Boolean> readFromNotificationRequest3;
	public final Setting<String> exceptionRecorderDomainName;

	@Inject
	public DatarouterNotificationSettings(SettingFinder finder, DatarouterProperties datarouterProperties){
		super(finder, "datarouter.notification.");

		apiEndPoint = registerString("apiEndPoint", "https://localhost:8443/job/api/notification");

		forceHideStackTrace = registerBoolean("forceHideStackTrace", false);
		errorNotificationRecipient = registerString("errorNotificationRecipient", datarouterProperties
				.getAdministratorEmail());
		deleteNotificationRequestBatchCount = registerInteger("deleteNotificationRequestBatchCount", 100);
		writeToNotificationRequest2 = registerBoolean("writeToNotificationRequest2", false);
		readFromNotificationRequest2 = registerBoolean("readFromNotificationRequest2", false);
		useMaxPerDayTimingStrategy = registerBoolean("useMaxPerDayTimingStrategy", false);
		writeToNotificationRequest3 = registerBoolean("writeToNotificationRequest3", false);
		readFromNotificationRequest3 = registerBoolean("readFromNotificationRequest3", false);
		exceptionRecorderDomainName = registerString("exceptionRecorderDomainName", "localhost:8443");
	}

}
