package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;

/********************************* indexes ***********************************/

@SuppressWarnings("serial")
@Embeddable
public class SortedBeanKey extends BaseEntityPrimaryKey<SortedBeanEntityKey,SortedBeanKey>{
	
	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	protected String a;
	protected String b;
	protected Integer c;
	protected String d;
	
	public static final String
		COL_a = "a",
		COL_b = "b",
		COL_c = "c",
		COL_d = "d";
	
	
	/************************ entity ***********************************/
	
	@Override
	public SortedBeanEntityKey getEntityKey(){
		return new SortedBeanEntityKey(a, b);
	}

	@Override
	public String getEntityKeyName() {
		return null;
	}

	@Override
	public SortedBeanKey prefixFromEntityKey(SortedBeanEntityKey ek){
		return new SortedBeanKey(ek.getA(), ek.getB(), null, null);
	}
	
	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return FieldTool.createList(
				new UInt31Field(COL_c, c),
				new StringField(COL_d, d, DEFAULT_STRING_LENGTH));
	}
	
	
	/************************ construct ********************************/

	SortedBeanKey(){
	}
	
	public SortedBeanKey(String a, String b, Integer c, String d){
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
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