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
package io.datarouter.web.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterEmailSettings extends SettingNode{

	private static final String DEFAULT_SMTP_HOST = "127.0.0.1";
	private static final Integer DEFAULT_SMTP_PORT = 25;

	public final CachedSetting<String> smtpHost;
	public final CachedSetting<Integer> smtpPort;
	public final CachedSetting<String> smtpUsername;
	public final CachedSetting<String> smtpPassword;
	public final CachedSetting<Boolean> useRemoteSmtp;
	public final CachedSetting<Boolean> sendDatarouterEmails;

	@Inject
	public DatarouterEmailSettings(SettingFinder finder){
		super(finder, "datarouterWeb.email.");

		sendDatarouterEmails = registerBoolean("sendDatarouterEmails", true);
		useRemoteSmtp = registerBoolean("useRemoteSmtp", false);

		// if below setting values are overridden, the overrides are only used if useRemoteSmtp is true
		// see getDatarouterEmailHostDetails() method
		smtpHost = registerString("smtpHost", DEFAULT_SMTP_HOST);
		smtpPort = registerInteger("smtpPort", DEFAULT_SMTP_PORT);
		smtpUsername = registerString("smtpUsername", "");
		smtpPassword = registerString("smtpPassword", "");
	}

	public DatarouterEmailHostDetails getDatarouterEmailHostDetails(){
		if(useRemoteSmtp.get()){
			return new DatarouterEmailHostDetails(smtpHost.get(), smtpPort.get(), smtpUsername.get(),
					smtpPassword.get());
		}
		return new DatarouterEmailHostDetails(DEFAULT_SMTP_HOST, DEFAULT_SMTP_PORT, "", "");
	}

	public static class DatarouterEmailHostDetails{

		public final String smtpHost;
		public final int smtpPort;
		public final String smtpUsername;
		public final String smtpPassword;

		public DatarouterEmailHostDetails(String smtpHost, int smtpPort, String smtpUsername, String smtpPassword){
			this.smtpHost = smtpHost;
			this.smtpPort = smtpPort;
			this.smtpUsername = smtpUsername;
			this.smtpPassword = smtpPassword;
		}

	}

}
