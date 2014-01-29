/**
 * 
 */
package com.hotpads.datarouter.config;

import java.util.List;
import java.util.Random;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.util.core.number.RandomTool;

@SuppressWarnings("serial")
@Embeddable
public class ConfigKey extends BasePrimaryKey<ConfigKey>{
	private Long id;
	private static Random random = new Random();

	public static final String
		COL_id = "id";
	
	public ConfigKey(){//remember no-arg is required
		this.id = RandomTool.nextPositiveLong(random);
	}
	
	public ConfigKey(Long id){
		this.id = id;
	}
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new LongField(COL_id, id));
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}