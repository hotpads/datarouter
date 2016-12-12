package com.hotpads.clustersetting;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateFieldKey;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumFieldKey;


public class ClusterSettingLog extends BaseDatabean<ClusterSettingLogKey,ClusterSettingLog>{

	/******************* fields ************************/

	private ClusterSettingLogKey key;
	private ClusterSettingScope scope;
	private String serverType;
	private String serverName;
	private String application;
	private String value;
	private ClusterSettingLogAction action;
	private String changedBy;

	public static class FieldKeys{
		public static final LongDateFieldKey timestamp = new LongDateFieldKey("timestamp");
		public static final StringFieldKey changedBy = new StringFieldKey("changedBy");
		public static final StringEnumFieldKey<ClusterSettingLogAction> action = new StringEnumFieldKey<>("action",
				ClusterSettingLogAction.class);
	}

	public static class ClusterSettingLogFielder extends BaseDatabeanFielder<ClusterSettingLogKey,ClusterSettingLog>{

		public ClusterSettingLogFielder(){
			super(ClusterSettingLogKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ClusterSettingLog databean){
			return Arrays.asList(
					new StringEnumField<>(ClusterSettingKey.FieldKeys.scope, databean.scope),
					new StringField(ClusterSettingKey.FieldKeys.serverType, databean.serverType),
					new StringField(ClusterSettingKey.FieldKeys.serverName, databean.serverName),
					new StringField(ClusterSettingKey.FieldKeys.application, databean.application),
					new StringField(ClusterSetting.FieldKeys.value, databean.value),
					new StringEnumField<>(FieldKeys.action, databean.action),
					new StringField(FieldKeys.changedBy, databean.changedBy));

		}
	}

	/************************* constructors ***************************/

	public ClusterSettingLog(){
		this.key = new ClusterSettingLogKey(null,null);
	}

	public ClusterSettingLog(ClusterSetting clusterSetting, ClusterSettingLogAction action, String changedBy){
		this.key = new ClusterSettingLogKey(clusterSetting.getName(), new Date());
		this.scope = clusterSetting.getScope();
		this.serverType = clusterSetting.getServerType();
		this.serverName = clusterSetting.getServerName();
		this.application = clusterSetting.getApplication();
		this.value = clusterSetting.getValue();
		this.action = action;
		this.changedBy = changedBy;
	}

	/******************************* databean **************************/

	@Override
	public Class<ClusterSettingLogKey> getKeyClass(){
		return ClusterSettingLogKey.class;
	}

	@Override
	public ClusterSettingLogKey getKey(){
		return key;
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

	public void setKey(ClusterSettingLogKey key){
		this.key = key;
	}

	public ClusterSettingScope getScope(){
		return scope;
	}

	public void setScope(ClusterSettingScope scope){
		this.scope = scope;
	}

	public String getServerType(){
		return serverType;
	}

	public void setServerType(String serverType){
		this.serverType = serverType;
	}

	public String getServerName(){
		return serverName;
	}

	public void setServerName(String serverName){
		this.serverName = serverName;
	}

	public String getApplication(){
		return application;
	}

	public void setApplication(String application){
		this.application = application;
	}

	public ClusterSettingLogAction getAction(){
		return action;
	}

	public String getChangedBy(){
		return changedBy;
	}
}