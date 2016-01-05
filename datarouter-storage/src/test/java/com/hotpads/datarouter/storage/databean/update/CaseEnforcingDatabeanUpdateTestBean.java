package com.hotpads.datarouter.storage.databean.update;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.content.ContentHolder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleField;


@Entity()
@Access(AccessType.FIELD)
public class CaseEnforcingDatabeanUpdateTestBean
extends BaseDatabean<CaseEnforcingDatabeanUpdateTestBeanKey,CaseEnforcingDatabeanUpdateTestBean>
implements ContentHolder<CaseEnforcingDatabeanUpdateTestBeanKey, CaseEnforcingDatabeanUpdateTestBean>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;

	@Id
	private CaseEnforcingDatabeanUpdateTestBeanKey key;

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


	public static class BackupBeanFielder
	extends BaseDatabeanFielder<CaseEnforcingDatabeanUpdateTestBeanKey,CaseEnforcingDatabeanUpdateTestBean>{
		public BackupBeanFielder(){
			super(CaseEnforcingDatabeanUpdateTestBeanKey.class);
		}
		@Override
		public List<Field<?>> getNonKeyFields(CaseEnforcingDatabeanUpdateTestBean d){
			return Arrays.asList(
					new StringField(COL_f1, d.f1, DEFAULT_STRING_LENGTH),
					new LongField(COL_f2, d.f2),
					new StringField(COL_f3, d.f3, DEFAULT_STRING_LENGTH),
					new DumbDoubleField(COL_f4, d.f4));
		}
	}


	/***************************** constructor **************************************/

	CaseEnforcingDatabeanUpdateTestBean() {
		this.key = new CaseEnforcingDatabeanUpdateTestBeanKey();
	}

	public CaseEnforcingDatabeanUpdateTestBean(String a, String b, Integer c, String d, String f1, Long f2, String f3,
			Double f4){
		this.key = new CaseEnforcingDatabeanUpdateTestBeanKey(a, b, c, d);
		this.f1 = f1;
		this.f2 = f2;
		this.f3 = f3;
		this.f4 = f4;
	}


	/************************** databean *******************************************/

	@Override
	public Class<CaseEnforcingDatabeanUpdateTestBeanKey> getKeyClass() {
		return CaseEnforcingDatabeanUpdateTestBeanKey.class;
	}

	@Override
	public CaseEnforcingDatabeanUpdateTestBeanKey getKey() {
		return key;
	}

	/** ContentHolder ********************************************************/
	@Override
	public List<Field<?>> getMetaFields() {
		return Collections.emptyList();
	}

	@Override
	public List<Field<?>> getContentFields() {
		return Arrays.asList(
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

}
