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
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.cached.MemoizedComputation;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.config.DatarouterWebPaths;

@Singleton
public class DatarouterSamlSettingRoot extends SettingRoot{

	public final CachedSetting<String> entityId;
	public final CachedSetting<String> assertionConsumerServicePath;
	public final CachedSetting<String> idpHomeUrl;
	public final CachedSetting<String> idpSamlUrl;
	//receives automatically created users' permission requests
	public final CachedSetting<String> permissionRequestEmail;
	public final CachedSetting<Boolean> ignoreServiceProviderRegistrationFailures;
	public final CachedSetting<Boolean> shouldUseSaml;
	public final CachedSetting<String> attributesToRoleGroupIds;
	public final CachedSetting<String> encodedIdpPublicKey;
	public final CachedSetting<String> encodedIdpX509Certificate;

	private MemoizedComputation<String, Credential> idpPublicKey;
	private MemoizedComputation<String, Credential> idpX509CertificatePublicKey;
	private final Boolean isLive;

	@Inject
	public DatarouterSamlSettingRoot(SettingFinder finder, DatarouterProperties datarouterProperties,
			DatarouterWebPaths paths){
		super(finder, "datarouterSaml.");
		entityId = registerString("entityId", "https://" + datarouterProperties.getServerName());
		assertionConsumerServicePath = registerString("assertionConsumerServletPath", paths.consumer.toSlashedString());
		idpHomeUrl = registerString("idpHomeUrl", "");
		idpSamlUrl = registerString("idpSamlUrl", "");
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
		shouldUseSaml.expire();
		encodedIdpPublicKey.expire();
		encodedIdpX509Certificate.expire();
		attributesToRoleGroupIds.expire();
	}

	public Boolean getShouldProcess(){
		return isLive && shouldUseSaml.get();
	}

	public Map<String, String> getAttributeToRoleGroupIdMap(){
		Map<String, String> parsed = new HashMap<>();
		for(String pair : attributesToRoleGroupIds.get().split(",")){
			int index = pair.indexOf("=");
			if(index > 0 && index < pair.length() - 1){//skip strings with no "=" OR with no strings surrounding "="
				parsed.put(pair.substring(0, index), pair.substring(++index));
			}
		}
		return parsed;
	}

	public Credential getSignatureCredential(){
		String settingValue = encodedIdpX509Certificate.get();
		if(StringTool.isEmptyOrWhitespace(settingValue)){
			settingValue = null;
		}
		Credential credential = idpX509CertificatePublicKey.getOutput(settingValue);
		if(credential == null){
			settingValue = encodedIdpPublicKey.get();
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
