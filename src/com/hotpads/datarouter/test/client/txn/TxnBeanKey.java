package com.hotpads.datarouter.test.client.txn;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

/********************************* indexes ***********************************/

@SuppressWarnings("serial")
@Embeddable
public class TxnBeanKey extends BasePrimaryKey<TxnBeanKey>{
	
	protected String id;
	
	TxnBeanKey(){
	}
	
	public TxnBeanKey(String id) {
		this.id = id;
	}
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(TxnBean.KEY_NAME, TxnBean.COL_id, this.id));
	}

	public String getId(){
		return id;
	}

	public void setId(String id){
		this.id = id;
	}

	
	
	
}