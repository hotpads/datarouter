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

import io.datarouter.httpclient.security.UrlConstants;
import io.datarouter.secret.service.CachedSecretFactory;
import io.datarouter.secret.service.CachedSecretFactory.CachedSecret;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.cached.CachedSetting;

/**
 * It's recommended to use DatarouterEmailSettingsProvider to avoid a circular dependency
 */
@Singleton
public class DatarouterEmailSettings extends SettingNode{

	static final String DEFAULT_SMTP_HOST = "127.0.0.1";
	static final Integer DEFAULT_SMTP_PORT = 25;

	public final CachedSetting<String> smtpHost;
	public final CachedSetting<Integer> smtpPort;
	public final CachedSetting<String> smtpUsername;
	public final CachedSetting<String> smtpPasswordName;
	private final CachedSecret<String> smtpPassword;
	public final CachedSetting<Boolean> useRemoteSmtp;
	public final CachedSetting<Boolean> sendDatarouterEmails;
	public final CachedSetting<String> emailLinkHostPort;
	public final CachedSetting<Boolean> includeLogo;
	public final CachedSetting<String> logoImgSrc;

	@Inject
	public DatarouterEmailSettings(SettingFinder finder, CachedSecretFactory cachedSecretFactory){
		super(finder, "datarouterWeb.email.");

		sendDatarouterEmails = registerBoolean("sendDatarouterEmails", true);
		useRemoteSmtp = registerBoolean("useRemoteSmtp", false);

		// if below smtp setting values are overridden, the overrides are only used if useRemoteSmtp is true
		// see getDatarouterEmailHostDetails() method
		smtpHost = registerString("smtpHost", DEFAULT_SMTP_HOST);
		smtpPort = registerInteger("smtpPort", DEFAULT_SMTP_PORT);
		smtpUsername = registerString("smtpUsername", "");
		smtpPasswordName = registerString("smtpPasswordName", "datarouter/email/smtpPassword");
		smtpPassword = cachedSecretFactory.cacheSharedSecretString(smtpPasswordName, "");

		emailLinkHostPort = registerString("emailLinkHostPort", UrlConstants.LOCAL_DEV_SERVER_HTTPS);
		includeLogo = registerBoolean("includeLogo", true);
		logoImgSrc = registerString("logoImgSrc", "");//specify "" for default
	}

	public Boolean useRemoteSmtp(){
		return useRemoteSmtp.get();
	}

	public String getSmtpHost(){
		return smtpHost.get();
	}

	public int getSmtpPort(){
		return smtpPort.get();
	}

	public String getSmtpUsername(){
		return smtpUsername.get();
	}

	public String getSmtpPassword(){
		return smtpPassword.get();
	}

	public Boolean sendDatarouterEmails(){
		return sendDatarouterEmails.get();
	}

	public String emailLinkHostPort(){
		return emailLinkHostPort.get();
	}

	public Boolean includeLogo(){
		return includeLogo.get();
	}

	public String logoImgSrc(){
		return logoImgSrc.get();
	}

	public DatarouterEmailHostDetails getDatarouterEmailHostDetails(){
		if(useRemoteSmtp()){
			return new DatarouterEmailHostDetails(
					getSmtpHost(),
					getSmtpPort(),
					getSmtpUsername(),
					getSmtpPassword());
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
