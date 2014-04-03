package com.hotpads.datarouter.test.node.basic.backup;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.content.ContentHolder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;


@SuppressWarnings("serial")
@Entity()
@AccessType("field")
public class BackupBean extends BaseDatabean<BackupBeanKey,BackupBean> implements ContentHolder<BackupBeanKey, BackupBean>{
	
	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	@Id
	private BackupBeanKey key;
	
	private String f1;
	private Long f2;
	private String f3;
	private Double f4;
	
	/***************************** columns ******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_f1 = "f1",
		COL_f2 = "f2",
		COL_f3 = "f3",
		COL_f4 = "f4";
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
				new StringField(COL_f1, f1, DEFAULT_STRING_LENGTH),
				new LongField(COL_f2, f2),
				new StringField(COL_f3, f3, DEFAULT_STRING_LENGTH),
				new DumbDoubleField(COL_f4, f4));
	}
	
	public static class BackupBeanFielder extends BaseDatabeanFielder<BackupBeanKey,BackupBean>{
		public BackupBeanFielder(){}
		@Override
		public Class<BackupBeanKey> getKeyFielderClass(){
			return BackupBeanKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(BackupBean d){
			return d.getNonKeyFields();
		}
	}
	

	/***************************** constructor **************************************/
		
	BackupBean() {
		this.key = new BackupBeanKey();
	}
	
	public BackupBean(String a, String b, Integer c, String d,
			String f1, Long f2, String f3, Double f4){
		this.key = new BackupBeanKey(a, b, c, d);
		this.f1 = f1;
		this.f2 = f2;
		this.f3 = f3;
		this.f4 = f4;
		
	}
	
	
	/************************** databean *******************************************/

	@Override
	public Class<BackupBeanKey> getKeyClass() {
		return BackupBeanKey.class;
	};
	
	@Override
	public BackupBeanKey getKey() {
		return key;
	}
	
	/** ContentHolder ********************************************************/
	@Override
	public List<Field<?>> getMetaFields() {
		return FieldTool.createList();
	}

	@Override
	public List<Field<?>> getContentFields() {
		return FieldTool.createList(
				new StringField(COL_f1, f1, DEFAULT_STRING_LENGTH),
				new LongField(COL_f2, f2),
				new StringField(COL_f3, f3, DEFAULT_STRING_LENGTH),
				new DumbDoubleField(COL_f4, f4));
	}

	@Override
	public boolean equalsContent(ContentHolder<?, ?> other) {
		// TODO Auto-generated method stub
		return false;
	}

	
	/***************************** index *************************************/
	
	public static class SortedBeanByDCBLookup extends BaseLookup<BackupBeanKey>{
		String d;
		Integer c;
		String b;
		public SortedBeanByDCBLookup(String d, Integer c, String b){
			this.d = d;
			this.c = c;
			this.b = b;
		}
		public List<Field<?>> getFields(){
			return FieldTool.createList(
					new StringField(BackupBean.KEY_NAME, BackupBeanKey.COL_d, d, DEFAULT_STRING_LENGTH),
					new UInt31Field(BackupBean.KEY_NAME, BackupBeanKey.COL_c, c),
					new StringField(BackupBean.KEY_NAME, BackupBeanKey.COL_b, b, DEFAULT_STRING_LENGTH));
		}
	}

	
	/***************************** get/set **************************************/

	public String getF1(){
		return f1;
	}

	public void setF1(String f1){
		this.f1 = f1;
	}

	public Long getF2(){
		return f2;
	}

	public void setF2(Long f2){
		this.f2 = f2;
	}

	public String getF3(){
		return f3;
	}

	public void setF3(String f3){
		this.f3 = f3;
	}

	public Double getF4(){
		return f4;
	}

	public void setF4(Double f4){
		this.f4 = f4;
	}

	public void setKey(BackupBeanKey key){
		this.key = key;
	}

	public String getA(){
		return key.getA();
	}

	public String getB(){
		return key.getB();
	}

	public Integer getC(){
		return key.getC();
	}

	public String getD(){
		return key.getD();
	}

	public void setA(String a){
		key.setA(a);
	}

	public void setB(String b){
		key.setB(b);
	}

	public void setC(Integer c){
		key.setC(c);
	}

	public void setD(String d){
		key.setD(d);
	}
	
	
}
