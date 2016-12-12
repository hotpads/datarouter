package com.hotpads.clustersetting;

import com.hotpads.datarouter.setting.ServerType;

public class ClusterSettingScopeValue implements Comparable<ClusterSettingScopeValue>{
	private final ClusterSettingScope scope;
	private final String value;
	private final String displayString;

	private static final String EMPTY_STRING = "";

	public ClusterSettingScopeValue(ClusterSettingKey setting){
		this.scope = setting.getScope();
		switch(this.scope){
		case defaultScope:
			this.value = "";
			this.displayString = "Default Scope";
			break;
		case cluster:
			this.value = "";
			this.displayString = "Cluster Scope";
			break;
		case serverType:
			this.value = setting.getServerType();
			this.displayString = "Server Type: " + setting.getServerType();
			break;
		case serverName:
			this.value = setting.getServerName();
			this.displayString = "Server Name: " + setting.getServerName();
			break;
		case application:
			this.value = setting.getApplication();
			this.displayString = "Web App Name: " + setting.getApplication();
			break;
		default:
			throw new RuntimeException("Failed to construct ClusterSettingScopeValue from ClusterSettingKey: "
					+ setting.toString());
		}
	}

	@Override
	public int compareTo(ClusterSettingScopeValue other){
		int scopeCompare = scope.compareTo(other.scope);
		return scopeCompare != 0 ? scopeCompare : value.compareTo(other.value);
	}

	public String getPersistentString(){
		return scope.getPersistentString() + '_' + value;
	}

	public static ClusterSettingScopeValue parse(String persistentString){
		if(persistentString == null){
			return null;
		}
		String[] parts = persistentString.split("_", 2);
		if(parts.length == 0){
			return null;
		}
		ClusterSettingScope scope = ClusterSettingScope.fromPersistentStringStatic(parts[0]);
		switch(scope){
		case defaultScope:
		case cluster:
			return new ClusterSettingScopeValue(new ClusterSettingKey(null, scope, null, null, null));
		case serverType:
			return new ClusterSettingScopeValue(new ClusterSettingKey(null, scope, parts[1], null, null));
		case serverName:
			return new ClusterSettingScopeValue(new ClusterSettingKey(null, scope, null, parts[1], null));
		case application:
			return new ClusterSettingScopeValue(new ClusterSettingKey(null, scope, null, null, parts[1]));
		default:
			throw new RuntimeException("Failed to parse ClusterSettingScopeValue from string: " + persistentString);
		}
	}

	public ClusterSettingKey toClusterSettingKey(String settingName){
		switch(scope){
		case defaultScope:
			return new ClusterSettingKey(settingName, scope, ServerType.UNKNOWN, EMPTY_STRING, EMPTY_STRING);
		case cluster:
			return new ClusterSettingKey(settingName, scope, ServerType.ALL, EMPTY_STRING, EMPTY_STRING);
		case serverType:
			return new ClusterSettingKey(settingName, scope, value, EMPTY_STRING, EMPTY_STRING);
		case serverName:
			return new ClusterSettingKey(settingName, scope, ServerType.UNKNOWN, value, EMPTY_STRING);
		case application:
			return new ClusterSettingKey(settingName, scope, ServerType.UNKNOWN, EMPTY_STRING, value);
		default:
			return null;
		}
	}

	public String getDisplayString(){
		return displayString;
	}
}
