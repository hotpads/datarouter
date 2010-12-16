package com.hotpads.datarouter.test.node.basic.backup;

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
public class BackupBeanKey extends BasePrimaryKey<BackupBeanKey>{
	
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
				new StringField(BackupBean.KEY_NAME, COL_a, a),
				new StringField(BackupBean.KEY_NAME, COL_b, b),
				new UInt31Field(BackupBean.KEY_NAME, COL_c, c),
				new StringField(BackupBean.KEY_NAME, COL_d, d));
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