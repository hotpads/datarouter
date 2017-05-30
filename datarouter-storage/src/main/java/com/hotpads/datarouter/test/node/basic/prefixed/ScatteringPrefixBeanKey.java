package com.hotpads.datarouter.test.node.basic.prefixed;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63FieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class ScatteringPrefixBeanKey extends BasePrimaryKey<ScatteringPrefixBeanKey>{

	private String foo;
	private Long id;

	ScatteringPrefixBeanKey(){
	}


	public ScatteringPrefixBeanKey(String foo, Long id){
		this.foo = foo;
		this.id = id;
	}

	private static class FieldKeys{
		private static final StringFieldKey foo = new StringFieldKey("foo");
		private static final UInt63FieldKey id = new UInt63FieldKey("id");
	}


	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.foo, foo),
				new UInt63Field(FieldKeys.id, id));
	}

	/***************************** get/set *******************************/

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public String getFoo(){
		return foo;
	}

	public void setFoo(String foo){
		this.foo = foo;
	}

}