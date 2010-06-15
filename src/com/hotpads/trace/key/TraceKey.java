/**
 * 
 */
package com.hotpads.trace.key;

import java.util.List;
import java.util.Random;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.imp.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.trace.Trace;
import com.hotpads.util.core.ListTool;

@SuppressWarnings("serial")
@Embeddable
public class TraceKey extends BasePrimaryKey<TraceKey>{
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
	public List<BaseField<?>> getFields(){
		List<BaseField<?>> fields = ListTool.create();
		fields.add(new LongField(Trace.KEY_key, COL_id, id));
		return fields;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}