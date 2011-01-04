package com.hotpads.datarouter.test.node.basic.manyfield;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

/********************************* indexes ***********************************/

@SuppressWarnings("serial")
@Embeddable
public class ManyFieldTypeBeanKey extends BasePrimaryKey<ManyFieldTypeBeanKey>{
	
	protected Long id;
	
	public ManyFieldTypeBeanKey(){//for hibernate
		this.id = UInt63Field.nextPositiveRandom();
	}
	
	public ManyFieldTypeBeanKey(Long id) {
		this.id = id;
	}
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new UInt63Field(ManyFieldTypeBean.KEY_NAME, ManyFieldTypeBean.COL_id, this.id));
	}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}
	
	
}