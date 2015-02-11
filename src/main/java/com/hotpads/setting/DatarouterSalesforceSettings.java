package com.hotpads.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.setting.cached.imp.StringCachedSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class DatarouterSalesforceSettings extends SettingNode{

	private StringCachedSetting clientId;
	private StringCachedSetting clientSecret;
	private StringCachedSetting username;
	private StringCachedSetting password;
	private StringCachedSetting loginEndpoint;

	@Inject
	public DatarouterSalesforceSettings(ClusterSettingFinder finder){
		super(finder, "datarouter.salesforce.", "datarouter.");
		register();
	}

	private void register(){
		clientId = registerString("clientId", "");
		clientSecret = registerString("clientSecret", "");
		username = registerString("username", "");
		password = registerString("password", "");
		loginEndpoint = registerString("loginEndpoint", "https://test.salesforce.com/services/oauth2/token");
	}

	public StringCachedSetting getClientId(){
		return clientId;
	}

	public StringCachedSetting getClientSecret(){
		return clientSecret;
	}

	public StringCachedSetting getUsername(){
		return username;
	}

	public StringCachedSetting getPassword(){
		return password;
	}

	public StringCachedSetting getLoginEndpoint(){
		return loginEndpoint;
	}

}
