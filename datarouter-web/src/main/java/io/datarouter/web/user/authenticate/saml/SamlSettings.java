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
package io.datarouter.web.user.authenticate.saml;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.opensaml.security.credential.Credential;

import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.profile.ConfigProfile;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.cached.MemoizedComputation;
import io.datarouter.util.string.StringTool;

@Singleton
public class SamlSettings extends SettingRoot{

	public static final String DEFAULT_ASSERTION_CONSUMER_SERVICE_PATH = "/consumer";

	public final Setting<String> entityId;
	public final Setting<String> assertionConsumerServicePath;
	public final CachedSetting<String> idpHomeUrl;
	public final CachedSetting<String> idpSamlUrl;
	public final CachedSetting<Boolean> shouldAllowRoleGroups;
	public final Setting<String> permissionRequestEmail;//receives automatically created users' permission requests
	public final Setting<Boolean> ignoreServiceProviderRegistrationFailures;

	private final CachedSetting<Boolean> shouldUseSaml;
	private final CachedSetting<String> attributesToRoleGroupIds;
	private final CachedSetting<String> encodedIdpPublicKey;
	private final CachedSetting<String> encodedIdpX509Certificate;
	private MemoizedComputation<String, Credential> idpPublicKey;
	private MemoizedComputation<String, Credential> idpX509CertificatePublicKey;
	private final Boolean isLive;

	@Inject
	public SamlSettings(SettingFinder finder, DatarouterProperties datarouterProperties){
		super(finder, "datarouterSaml.");

		entityId = registerString("entityId", "https://" + datarouterProperties.getServerName());
		assertionConsumerServicePath = registerString("assertionConsumerServletPath",
				DEFAULT_ASSERTION_CONSUMER_SERVICE_PATH);
		idpHomeUrl = registerString("idpHomeUrl", "");
		idpSamlUrl = registerString("idpSamlUrl", "");
		shouldAllowRoleGroups = registerBoolean("shouldAllowRoleGroups", true);
		permissionRequestEmail = registerString("permissionRequestEmail", datarouterProperties.getAdministratorEmail());
		ignoreServiceProviderRegistrationFailures = registerBoolean("ignoreServiceProviderRegistrationFailures", true);

		shouldUseSaml = registerBoolean("shouldUseSaml", false);
		attributesToRoleGroupIds = registerString("attributesToRoleGroupIds", "");
		encodedIdpPublicKey = registerString("encodedIdpPublicKey", "");
		encodedIdpX509Certificate = registerString("encodedIdpX509Certificate", "");
		idpPublicKey = new MemoizedComputation<>(SamlTool::getCredentialFromEncodedRsaPublicKey);
		idpX509CertificatePublicKey = new MemoizedComputation<>(SamlTool
				::getCredentialFromEncodedX509Certificate);

		isLive = !ConfigProfile.DEVELOPMENT.get().getPersistentString().equals(datarouterProperties.getConfigProfile());
	}

	public void expireAutoConfigSettings(){
		idpHomeUrl.expire();
		idpSamlUrl.expire();
		shouldAllowRoleGroups.expire();
		shouldUseSaml.expire();
		encodedIdpPublicKey.expire();
		encodedIdpX509Certificate.expire();
		attributesToRoleGroupIds.expire();
	}

	public String getShouldUseSamlName(){
		return shouldUseSaml.getName();
	}

	public String getEncodedIdpPublicKeyName(){
		return encodedIdpPublicKey.getName();
	}

	public String getEncodedIdpX509CertificateName(){
		return encodedIdpX509Certificate.getName();
	}

	public Boolean getIsLive(){
		return isLive;
	}

	public Boolean getShouldProcess(){
		return isLive && shouldUseSaml.getValue();
	}

	public Map<String, String> getAttributeToRoleGroupIdMap(){
		Map<String, String> parsed = new HashMap<>();
		for(String pair : attributesToRoleGroupIds.getValue().split(",")){
			int index = pair.indexOf("=");
			if(index > 0 && index < pair.length() - 1){//skip strings with no "=" OR with no strings surrounding "="
				parsed.put(pair.substring(0, index), pair.substring(++index));
			}
		}
		return parsed;
	}

	public String getAttributesToRoleGroupIdsName(){
		return attributesToRoleGroupIds.getName();
	}

	public String getAttributesToRoleGroupIdsString(){
		return attributesToRoleGroupIds.getValue();
	}

	public Credential getSignatureCredential(){
		String settingValue = encodedIdpX509Certificate.getValue();
		if(StringTool.isEmptyOrWhitespace(settingValue)){
			settingValue = null;
		}
		Credential credential = idpX509CertificatePublicKey.getOutput(settingValue);
		if(credential == null){
			settingValue = encodedIdpPublicKey.getValue();
			if(StringTool.isEmptyOrWhitespace(settingValue)){
				settingValue = null;
			}
			credential = idpPublicKey.getOutput(settingValue);
		}
		if(credential == null){
			throw new NullPointerException();
		}
		return credential;
	}

}
