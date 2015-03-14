package com.hotpads.datarouter.connection.keepalive;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;

/*
 * Databean for auto-generating a table called KeepAlive that can be queried to 
 * make sure your database is available.  Just add it to one of the routers in your 
 * application.  
 * 
 * Currently used by the HBaseDynamicClientFactory
 * 
 */
@Entity()
@AccessType("field")
public class KeepAlive extends BaseDatabean<KeepAliveKey,KeepAlive>{
	
	public static final String TABLE_NAME = "KeepAlive";//if you want to modify, you will have to change other stuff as well
	
	@Id
	private KeepAliveKey key;
	
	/***************************** columns ******************************/
	
	public static class KeepAliveFielder extends BaseDatabeanFielder<KeepAliveKey,KeepAlive>{
		public KeepAliveFielder(){}
		@Override
		public Class<KeepAliveKey> getKeyFielderClass(){
			return KeepAliveKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(KeepAlive d){
			return FieldTool.createList();
		}
	}
	

	/***************************** constructor **************************************/
		
	KeepAlive(){
		this.key = new KeepAliveKey(null);
	}
	
	public KeepAlive(String id) {
		this.key = new KeepAliveKey(id);
	}
	
	
	/***************************** method ************************************/
	
	@Override
	public Class<KeepAliveKey> getKeyClass() {
		return KeepAliveKey.class;
	};
	
	@Override
	public KeepAliveKey getKey() {
		return key;
	}

	
	/***************************** get/set **************************************/


	public void setKey(KeepAliveKey key){
		this.key = key;
	}




	public String getId(){
		return key.getId();
	}


	public void setId(String id){
		key.setId(id);
	}
	
	
}
