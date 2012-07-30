package com.hotpads.datarouter.test.node.basic.backup;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

/********************************* indexes ***********************************/

@SuppressWarnings("serial")
@Embeddable
public class BackupBeanKey extends BasePrimaryKey<BackupBeanKey>{
	
	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	protected String a;
	protected String b;
	protected Integer c;
	protected String d;
	
	BackupBeanKey(){
	}
	
	
	public BackupBeanKey(String a, String b, Integer c, String d){
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
				new StringField(COL_a, a, DEFAULT_STRING_LENGTH),
				new StringField(COL_b, b, DEFAULT_STRING_LENGTH),
				new UInt31Field(COL_c, c),
				new StringField(COL_d, d, DEFAULT_STRING_LENGTH));
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