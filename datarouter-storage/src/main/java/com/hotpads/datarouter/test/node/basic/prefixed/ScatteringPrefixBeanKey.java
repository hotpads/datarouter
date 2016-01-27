package com.hotpads.datarouter.test.node.basic.prefixed;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

/********************************* indexes ***********************************/

@SuppressWarnings("serial")
@Embeddable
public class ScatteringPrefixBeanKey extends BasePrimaryKey<ScatteringPrefixBeanKey>{
	
	protected String a;
	protected Long id;
	
	ScatteringPrefixBeanKey(){
		this.a = null;
		this.id = null;
	}
	
	
	public ScatteringPrefixBeanKey(String a, Long id){
		this.a = a;
		this.id = id;
	}
	
	public static final String
		COL_a = "a",
		COL_id = "id";


	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(COL_a, a, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new UInt63Field(COL_id, id));
	}


	
	/***************************** get/set *******************************/
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getA() {
		return a;
	}


	public void setA(String a) {
		this.a = a;
	}

	
}