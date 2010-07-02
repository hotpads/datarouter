package com.hotpads.datarouter.test.client.txn;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

/********************************* indexes ***********************************/

@SuppressWarnings("serial")
@Embeddable
public class TxnBeanKey extends BasePrimaryKey<TxnBeanKey>{
	
	protected Long id;
	
	public TxnBeanKey(){//for hibernate
		this.id = UInt63Field.nextRandom();
	}
	
	public TxnBeanKey(Long id) {
		this.id = id;
	}
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new UInt63Field(TxnBean.KEY_NAME, TxnBean.COL_id, this.id));
	}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}
	
	
}