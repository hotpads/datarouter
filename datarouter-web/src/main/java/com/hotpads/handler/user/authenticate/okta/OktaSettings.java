package com.hotpads.handler.user.authenticate.okta;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingRoot;

@Singleton
public class OktaSettings extends SettingRoot{

	public final Setting<Boolean> isOktaAllowed;
	public final Setting<Boolean> isOktaRequired;
	public final Setting<String> orgUrl;
	public final Setting<String> apiKey;

	private final Boolean isLive;

	@Inject
	public OktaSettings(SettingFinder finder, DatarouterProperties datarouterProperties){
		super(finder, "datarouterOkta.", "");

		isOktaAllowed = registerBoolean("isOktaAllowed", false);
		isOktaRequired = registerBoolean("isOktaRequired", false);
		//for testing (see scottd)
		//orgUrl = registerString("orgUrl", "https://dev-907596-admin.oktapreview.com");
		//apiKey = registerString("apiKey", "004dX4J_CeoyztfSFIc_2HTHQmqzdGwWyXgv-AHurl");
		orgUrl = registerString("orgUrl", "https://zillow.okta.com");
		apiKey = registerString("apiKey", "");

		isLive = !ServerType.DEV.equals(datarouterProperties.getServerType().getPersistentString());
	}

	public Boolean getShouldProcess(){
		return isLive && (isOktaAllowed.getValue() || isOktaRequired.getValue());
	}
}