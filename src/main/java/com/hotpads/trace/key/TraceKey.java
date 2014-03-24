/**
 * 
 */
package com.hotpads.trace.key;

import java.util.List;
import java.util.Random;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;

@SuppressWarnings("serial")
@Embeddable
public class TraceKey extends BaseEntityPrimaryKey<TraceEntityKey,TraceKey>{
	private Long id;
	private static Random random = new Random();

	public static final String
		COL_id = "id";
	
	public TraceKey(){//remember no-arg is required
		long r = Math.abs(random.nextLong());
		if(Long.MIN_VALUE==r){ r = 0; }
		this.id = r;
	}
	
	public TraceKey(Long id){
		this.id = id;
	}

	
	@Override
	public TraceEntityKey getEntityKey(){
		return new TraceEntityKey(id);
	}
	
	@Override
	public List<Field<?>> getEntityKeyFields(){
		return FieldTool.createList(
				new LongField(COL_id, id));//override the standard traceId col name
	}
	
	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return FieldTool.createList();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}