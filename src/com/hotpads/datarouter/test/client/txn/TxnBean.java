package com.hotpads.datarouter.test.client.txn;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.UInt63Field;


@SuppressWarnings("serial")
@Entity()
@AccessType("field")
public class TxnBean extends BaseDatabean<TxnBeanKey>{
	
	@Id
	private TxnBeanKey key;
	
	private Long version;
	
	private String f0;
	
	/***************************** columns ******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_id = "id",
		COL_version = "version",
		COL_f0 = "f0";
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
				new UInt63Field(COL_version, this.version),
				new StringField(COL_f0, this.f0));
	}
	

	/***************************** constructor **************************************/
		
	TxnBean(){
	}
	
	public TxnBean(String f0) {
		this.key = new TxnBeanKey();
		this.version = 0L;
		this.f0 = f0;
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

	public String getF0(){
		return f0;
	}


	public void setF0(String f0){
		this.f0 = f0;
	}

	public void setKey(TxnBeanKey key){
		this.key = key;
	}


	public Long getVersion(){
		return version;
	}


	public void setVersion(Long version){
		this.version = version;
	}


	public Long getId(){
		return key.getId();
	}


	public void setId(Long id){
		key.setId(id);
	}
	
	
}
