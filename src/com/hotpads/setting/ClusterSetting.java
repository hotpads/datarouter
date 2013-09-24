package com.hotpads.setting;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;

@SuppressWarnings("serial")
public class ClusterSetting
extends BaseDatabean<ClusterSettingKey,ClusterSetting>{

	protected ClusterSettingKey key;
	protected String value;

    
    /********************** columns ************************/

	public static class F{
	    public static final String
	    	value = "value";
	}
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
				new StringField(F.value, value, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}
	
	public static class ClusterSettingFielder extends BaseDatabeanFielder<ClusterSettingKey,ClusterSetting>{
		public ClusterSettingFielder(){}
		@Override
		public Class<ClusterSettingKey> getKeyFielderClass(){
			return ClusterSettingKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(ClusterSetting d){
			return d.getNonKeyFields();
		}
	}
	
    
    /************************* constructors ***************************/

	public ClusterSetting(){
		this.key = new ClusterSettingKey(null, null, null, null, null);
	}
	
	public ClusterSetting(ClusterSettingKey key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public ClusterSetting(String name, ClusterSettingScope scope, Class<ST> serverTypeClass, ServerType<ST> serverType,
			String instance, String application, String value){
		this.key = new ClusterSettingKey(name, scope, serverTypeClass, serverType, instance, application);
		this.value = value;
	}
    
    /******************************* databean **************************/
    
    @Override
    public Class<ClusterSettingKey> getKeyClass(){
    	return ClusterSettingKey.class;
    }
    
    @Override
	public ClusterSettingKey getKey() {
		return key;
	}
    
    @Override
    public boolean isFieldAware(){
    	return true;
    }
    
    /***************************** methods **************************************/

    public String getServerTypePersistentString(){
    	return key.getServerTypePersistentString();
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

	public String getInstance(){
		return key.getInstance();
	}

	public void setInstance(String instance){
		key.setInstance(instance);
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