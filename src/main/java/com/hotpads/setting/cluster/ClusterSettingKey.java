/**
 * 
 */
package com.hotpads.setting.cluster;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class ClusterSettingKey
extends BasePrimaryKey<ClusterSettingKey>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	public static final int LEN_SCOPE = MySqlColumnType.MAX_LENGTH_VARCHAR;
	public static final int LEN_SERVER_TYPE = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	/********************** fields ****************************************/

	private String name;
	//could probably calculate the scope at runtime, but an explicit field will make it easier to understand the 
	// ClusterSetting table in mysql
	private ClusterSettingScope scope;
	private String serverType;
	private String instance;
	private String application;
	
	public static class F{
		public static final String
			name = "name",
			scope = "scope",
			serverType = "serverType",
			instance = "instance",
			application = "application";
	}

	@Override
	public List<Field<?>> getFields() {
		return FieldTool.createList(
			new StringField(F.name, name, DEFAULT_STRING_LENGTH),
			new StringEnumField<ClusterSettingScope>(ClusterSettingScope.class, F.scope, scope, LEN_SCOPE),
			new StringField(F.serverType, serverType, LEN_SERVER_TYPE),
			new StringField(F.instance, instance, DEFAULT_STRING_LENGTH),
			new StringField(F.application, application, DEFAULT_STRING_LENGTH));
	}
	
	
	/*************************** constructors *******************************/
	
	ClusterSettingKey(){//required no-arg
	}
			
	public ClusterSettingKey(String name, ClusterSettingScope scope, String serverType,
			String instance, String application){
		this.name = name;
		this.scope = scope;
		this.serverType = serverType;
		this.instance = instance;
		this.application = application;
	}
	
	
	/****************************** methods ********************************/


	/***************************** get/set *******************************/

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

	public String getInstance(){
		return instance;
	}

	public void setInstance(String instance){
		this.instance = instance;
	}

	public String getApplication(){
		return application;
	}

	public void setApplication(String application){
		this.application = application;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}
	
}