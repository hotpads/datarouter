package com.hotpads.clustersetting;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Objects;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.server.databean.WebAppInstance;

public class ClusterSettingKey extends BasePrimaryKey<ClusterSettingKey>{

	public static final int DEFAULT_MEDIUM_STRING_LENGTH = MySqlColumnType.LENGTH_50;
	public static final int LEN_SERVER_TYPE = MySqlColumnType.LENGTH_50;

	/********************** fields ****************************************/

	private String name;
	private ClusterSettingScope scope;
	private String serverType;
	private String serverName;
	private String application;

	public static class FieldKeys{
		public static final StringFieldKey name = new StringFieldKey("name");
		public static final StringEnumFieldKey<ClusterSettingScope> scope = new StringEnumFieldKey<>("scope",
				ClusterSettingScope.class);
		public static final StringFieldKey serverType = new StringFieldKey("serverType").withSize(LEN_SERVER_TYPE);
		public static final StringFieldKey serverName = new StringFieldKey("serverName")
				.withSize(DEFAULT_MEDIUM_STRING_LENGTH);
		public static final StringFieldKey application = new StringFieldKey("application")
				.withSize(DEFAULT_MEDIUM_STRING_LENGTH);
	}

	@Override
	public List<Field<?>> getFields() {
		return Arrays.asList(
				new StringField(FieldKeys.name, name),
				new StringEnumField<>(FieldKeys.scope, scope),
				new StringField(FieldKeys.serverType, serverType),
				new StringField(FieldKeys.serverName, serverName),
				new StringField(FieldKeys.application, application));
	}

	/*************************** constructors *******************************/

	ClusterSettingKey(){//required no-arg
	}

	public ClusterSettingKey(String name, ClusterSettingScope scope, String serverType, String serverName,
			String application){
		this.name = name;
		this.scope = scope;
		this.serverType = serverType;
		this.serverName = serverName;
		this.application = application;
	}

	/****************************** methods ********************************/

	public boolean appliesToWebAppInstance(WebAppInstance app){
		if(ClusterSettingScope.defaultScope == scope){
			return true;
		}else if(ClusterSettingScope.cluster == scope){
			return true;
		}else if(ClusterSettingScope.serverType == scope){
			return Objects.equal(serverType, app.getServerType());
		}else if(ClusterSettingScope.serverName == scope){
			return Objects.equal(serverName, app.getKey().getServerName());
		}else if(ClusterSettingScope.application == scope){
			return Objects.equal(application, app.getKey().getWebAppName());
		}
		throw new RuntimeException("unknown key.scope");
	}

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

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	/********************* Tests *******************************/

	public static class ClusterSettingTests{
		@Test
		public void testAppliesToWebAppInstance(){
			final String application = "myApplication";
			final String serverName = "myServerName";
			final String serverType = "myServerType";
			WebAppInstance app = new WebAppInstance(application, serverName, serverType);

			ClusterSettingKey keyDefault = new ClusterSettingKey("a", ClusterSettingScope.defaultScope, null, null,
					null);
			Assert.assertTrue(keyDefault.appliesToWebAppInstance(app));

			ClusterSettingKey keyCluster = new ClusterSettingKey("b", ClusterSettingScope.cluster, null, null, null);
			Assert.assertTrue(keyCluster.appliesToWebAppInstance(app));

			ClusterSettingKey keyServerType = new ClusterSettingKey("c", ClusterSettingScope.serverType, serverType,
					null, null);
			Assert.assertTrue(keyServerType.appliesToWebAppInstance(app));

			ClusterSettingKey keyServerName = new ClusterSettingKey("d", ClusterSettingScope.serverName, null,
					serverName, null);
			Assert.assertTrue(keyServerName.appliesToWebAppInstance(app));

			ClusterSettingKey keyApplication = new ClusterSettingKey("e", ClusterSettingScope.application, null, null,
					application);
			Assert.assertTrue(keyApplication.appliesToWebAppInstance(app));

			ClusterSettingKey keyApplicationFalse = new ClusterSettingKey("eFalse", ClusterSettingScope.application,
					null, null, "not-" + application);
			Assert.assertFalse(keyApplicationFalse.appliesToWebAppInstance(app));
		}
	}

}
