package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleField;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleFieldKey;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;


@SuppressWarnings("serial")
@Entity()
@Access(AccessType.FIELD)
public class SortedBean extends BaseDatabean<SortedBeanKey,SortedBean>{

	private static final String KEY_NAME = "key";

	@Id
	private SortedBeanKey key;

	private String f1;
	private Long f2;
	private String f3;
	private Double f4;

	/***************************** columns ******************************/

	public static class SortedBeanFielder extends BaseDatabeanFielder<SortedBeanKey,SortedBean>{
		public static final StringFieldKey f1 = new StringFieldKey("f1");
		public static final LongFieldKey f2 = new LongFieldKey("f2");
		public static final StringFieldKey f3 = new StringFieldKey("f3");
		public static final DumbDoubleFieldKey f4 = new DumbDoubleFieldKey("f4");

		public SortedBeanFielder(){
		}
		@Override
		public Class<SortedBeanKey> getKeyFielderClass(){
			return SortedBeanKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(SortedBean d){
			return FieldTool.createList(
					new StringField(f1, d.f1),
					new LongField(f2, d.f2),
					new StringField(f3, d.f3),
					new DumbDoubleField(f4, d.f4));
		}
	}


	/***************************** constructor **************************************/

	SortedBean() {
		this.key = new SortedBeanKey();
	}

	public SortedBean(String a, String b, Integer c, String d,
			String f1, Long f2, String f3, Double f4){
		this(new SortedBeanKey(a, b, c, d), f1, f2, f3, f4);
	}

	public SortedBean(SortedBeanKey key, String f1, Long f2, String f3, Double f4){
		this.key = key;
		this.f1 = f1;
		this.f2 = f2;
		this.f3 = f3;
		this.f4 = f4;
	}


	/************************** databean *******************************************/

	@Override
	public Class<SortedBeanKey> getKeyClass() {
		return SortedBeanKey.class;
	};

	@Override
	public SortedBeanKey getKey() {
		return key;
	}


	/***************************** index *************************************/

	public static class SortedBeanByDCBLookup extends BaseLookup<SortedBeanKey>{
		String d;
		Integer c;
		String b;
		public SortedBeanByDCBLookup(String d, Integer c, String b){
			this.d = d;
			this.c = c;
			this.b = b;
		}
		@Override
		public List<Field<?>> getFields(){
			return FieldTool.createList(
					new StringField(SortedBean.KEY_NAME, SortedBeanKey.FieldKeys.d, d),
					new UInt31Field(SortedBean.KEY_NAME, SortedBeanKey.FieldKeys.c, c),
					new StringField(SortedBean.KEY_NAME, SortedBeanEntityKey.FieldKeys.b, b));
		}
	}


	/***************************** get/set **************************************/

	public String getA(){
		return key.getA();
	}

	public String getB(){
		return key.getB();
	}

}
