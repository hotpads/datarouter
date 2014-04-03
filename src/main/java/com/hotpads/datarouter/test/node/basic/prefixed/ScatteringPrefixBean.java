package com.hotpads.datarouter.test.node.basic.prefixed;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.SignedByteField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt8Field;
import com.hotpads.datarouter.storage.prefix.BaseScatteringPrefix;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.util.core.ListTool;


@SuppressWarnings("serial")
@Entity()
@AccessType("field")
public class ScatteringPrefixBean extends BaseDatabean<ScatteringPrefixBeanKey,ScatteringPrefixBean>{
	
	@Id
	private ScatteringPrefixBeanKey key;
	
	private String f1;
	
	/***************************** columns ******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_f1 = "f1";
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
				new StringField(COL_f1, f1, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}
	
	public static class ScatteringPrefixBeanFielder extends BaseDatabeanFielder<ScatteringPrefixBeanKey,ScatteringPrefixBean>{	
		
		public static class ScatteringPrefixBeanScatterer extends BaseScatteringPrefix{			
			public static final Integer 
				NUM_SHARDS = 8,
				NUM_PREFIX_BYTES = 1;
			
			public static class F{
				public static final String
					prefix = "prefix";
			}
			@Override
			public List<Field<?>> getScatteringPrefixFields(FieldSet<?> primaryKey) {
				Long id = ((ScatteringPrefixBeanKey)primaryKey).getId();
				int mod = id==null ? 0 : (int)(id % NUM_SHARDS);
				return FieldTool.createList(
						new UInt8Field("prefix", mod));
			}
			@Override
			public List<List<Field<?>>> getAllPossibleScatteringPrefixes() {
				List<List<Field<?>>> all = ListTool.createArrayList(NUM_SHARDS);
				for(int i=0; i < NUM_SHARDS; ++i){
					//DOH, probably should have used an UnsignedByteField
					all.add(FieldTool.createList(new SignedByteField(F.prefix, (byte)i)));
				}
				return all;
			}
			@Override
			public Integer getNumPrefixBytes() {
				return NUM_PREFIX_BYTES;
			}
		}

		public ScatteringPrefixBeanFielder(){
		}
		
		@Override
		public Class<? extends ScatteringPrefix> getScatteringPrefixClass() {
			return ScatteringPrefixBeanScatterer.class;
		}
		@Override
		public Class<ScatteringPrefixBeanKey> getKeyFielderClass(){
			return ScatteringPrefixBeanKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(ScatteringPrefixBean d){
			return d.getNonKeyFields();
		}
	}
	

	/***************************** constructor **************************************/
		
	ScatteringPrefixBean() {
		this.key = new ScatteringPrefixBeanKey();
	}
	
	public ScatteringPrefixBean(String a, Long id, String f1, Integer f2){
		this.key = new ScatteringPrefixBeanKey(a, id);
		int prefix = (int)(id % ScatteringPrefixBeanFielder.ScatteringPrefixBeanScatterer.NUM_SHARDS);
		this.f1 = prefix+"_"+id.toString();
		
	}
	
	
	/************************** databean *******************************************/

	@Override
	public Class<ScatteringPrefixBeanKey> getKeyClass() {
		return ScatteringPrefixBeanKey.class;
	};
	
	@Override
	public ScatteringPrefixBeanKey getKey() {
		return key;
	}
	

	
	/***************************** get/set **************************************/

	public String getF1(){
		return f1;
	}


	public void setF1(String f1){
		this.f1 = f1;
	}


	public void setKey(ScatteringPrefixBeanKey key) {
		this.key = key;
	}


	
}
