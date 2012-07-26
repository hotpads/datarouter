package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

/********************************* indexes ***********************************/

@SuppressWarnings("serial")
@Embeddable
public class SortedBeanKey extends BasePrimaryKey<SortedBeanKey>{
	
	protected String a;
	protected String b;
	protected Integer c;
	protected String d;
	
	SortedBeanKey(){
	}
	
	
	public SortedBeanKey(String a, String b, Integer c, String d){
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	
	public static final String
		COL_a = "a",
		COL_b = "b",
		COL_c = "c",
		COL_d = "d";


	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(COL_a, a,255),
				new StringField(COL_b, b,255),
				new UInt31Field(COL_c, c),
				new StringField(COL_d, d,255));
	}


	
	/***************************** get/set *******************************/
	
	public String getA(){
		return a;
	}

	public void setA(String a){
		this.a = a;
	}

	public String getB(){
		return b;
	}

	public void setB(String b){
		this.b = b;
	}

	public Integer getC(){
		return c;
	}

	public void setC(Integer c){
		this.c = c;
	}

	public String getD(){
		return d;
	}

	public void setD(String d){
		this.d = d;
	}

	
}