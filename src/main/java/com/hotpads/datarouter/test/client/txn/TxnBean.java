package com.hotpads.datarouter.test.client.txn;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;


@Entity()
@AccessType("field")
public class TxnBean extends BaseDatabean<TxnBeanKey,TxnBean>{
	
	@Id
	private TxnBeanKey key;
	
	public static class TxnBeanFielder extends BaseDatabeanFielder<TxnBeanKey,TxnBean>{
		public TxnBeanFielder(){}
		@Override
		public Class<TxnBeanKey> getKeyFielderClass(){
			return TxnBeanKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(TxnBean d){
			return FieldTool.createList();
		}
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
