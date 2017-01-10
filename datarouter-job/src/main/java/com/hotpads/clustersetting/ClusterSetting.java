package com.hotpads.clustersetting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.webappinstance.databean.WebAppInstance;

public class ClusterSetting extends BaseDatabean<ClusterSettingKey,ClusterSetting>{

	/******************* fields ************************/

	private ClusterSettingKey key;
	private String value;

	public static class FieldKeys{
		public static final StringFieldKey value = new StringFieldKey("value");
	}


	public static class ClusterSettingFielder extends BaseDatabeanFielder<ClusterSettingKey,ClusterSetting>{

		public ClusterSettingFielder(){
			super(ClusterSettingKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ClusterSetting databean){
			return Arrays.asList(
					new StringField(FieldKeys.value, databean.value));
		}
	}

	/************************* constructors ***************************/

	public ClusterSetting(){
		this.key = new ClusterSettingKey(null, null, null, null, null);
	}

	public ClusterSetting(ClusterSettingKey key, String value){
		this.key = key;
		this.value = value;
	}

	public ClusterSetting(String name, ClusterSettingScope scope, String serverType, String serverName,
			String application, String value){
		this.key = new ClusterSettingKey(name, scope, serverType, serverName, application);
		this.value = value;
	}

	/******************************* databean **************************/

	@Override
	public Class<ClusterSettingKey> getKeyClass(){
		return ClusterSettingKey.class;
	}

	@Override
	public ClusterSettingKey getKey(){
		return key;
	}

	/***************************** static **************************************/

	public static Optional<ClusterSetting> getMostSpecificSettingForWebAppInstance(List<ClusterSetting> settings,
			WebAppInstance webAppInstance){
		List<ClusterSetting> settingsForWebAppInstance = filterForWebAppInstance(settings, webAppInstance);
		return getMostSpecificSetting(settingsForWebAppInstance);
	}

	public static List<ClusterSetting> filterForWebAppInstance(List<ClusterSetting> settings,
			WebAppInstance webAppInstance){
		return settings.stream()
				.filter(setting -> setting.getKey().appliesToWebAppInstance(webAppInstance))
				.collect(Collectors.toList());
	}

	public static Optional<ClusterSetting> getMostSpecificSetting(List<ClusterSetting> settings){
		return settings.isEmpty() ? Optional.empty() : Optional.of(Collections.min(settings,
				new ClusterSettingScopeComparator()));
	}

	public static <T> T getTypedValueOrUseDefaultFrom(Optional<ClusterSetting> clusterSetting,
			Setting<T> settingForTypeAndDefault){
		return clusterSetting
				.map(setting -> setting.getTypedValue(settingForTypeAndDefault))
				.orElse(settingForTypeAndDefault.getDefaultValue());
	}

	/***************************** methods **************************************/

	public <T> T getTypedValue(Setting<T> parser){
		return parser.parseStringValue(value);
	}

	/****************************** Object methods ******************************/

	@Override
	public String toString(){
		return key.toString() + ":" + value;
	}

	/******************************* getters/setters *****************************/

	public String getValue(){
		return value;
	}

	public void setValue(String value){
		this.value = value;
	}

	public void setKey(ClusterSettingKey key){
		this.key = key;
	}

	public ClusterSettingScope getScope(){
		return key.getScope();
	}

	public void setScope(ClusterSettingScope scope){
		key.setScope(scope);
	}

	public String getServerType(){
		return key.getServerType();
	}

	public void setServerType(String serverType){
		key.setServerType(serverType);
	}

	public String getServerName(){
		return key.getServerName();
	}

	public void setServerName(String serverName){
		key.setServerName(serverName);
	}

	public String getApplication(){
		return key.getApplication();
	}

	public void setApplication(String application){
		key.setApplication(application);
	}

	public String getName(){
		return key.getName();
	}

	public void setName(String name){
		key.setName(name);
	}

}
