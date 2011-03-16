package com.hotpads.datarouter.test.client.txn;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;


@SuppressWarnings("serial")
@Entity()
@AccessType("field")
public class TxnBean extends BaseDatabean<TxnBeanKey,TxnBean>{
	
	@Id
	private TxnBeanKey key;
	
	/***************************** columns ******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_id = "id";
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList();
	}
	
	@Override
	public boolean isFieldAware(){
		return true;
	}
	

	/***************************** constructor **************************************/
		
	TxnBean(){
		this.key = new TxnBeanKey(null);
	}
	
	public TxnBean(String id) {
		this.key = new TxnBeanKey(id);
	}
	
	
	/***************************** method ************************************/
	
	@Override
	public Class<TxnBeanKey> getKeyClass() {
		return TxnBeanKey.class;
	};
	
	@Override
	public TxnBeanKey getKey() {
		return key;
	}

	
	/***************************** get/set **************************************/


	public void setKey(TxnBeanKey key){
		this.key = key;
	}




	public String getId(){
		return key.getId();
	}


	public void setId(String id){
		key.setId(id);
	}
	
	
}
