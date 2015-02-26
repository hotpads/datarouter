package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityKey;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityPartitioner;
import com.hotpads.datarouter.storage.key.entity.base.NoOpEntityPartitioner;
import com.hotpads.datarouter.util.core.HashMethods;

@SuppressWarnings("serial")
public class SortedBeanEntityKey 
extends BaseEntityKey<SortedBeanEntityKey>{

	/************* fields *************************/

	protected String a;
	protected String b;

	
	public static class F{
		public static final String
			a = "a",
			b = "b";
	}
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(F.a, a, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField(F.b, b, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}
	
	public static class SortedBeanEntityPartitioner
	extends NoOpEntityPartitioner<SortedBeanEntityKey>{
	}


	public static class SortedBeanEntityPartitioner4 extends BaseEntityPartitioner<SortedBeanEntityKey>{
		@Override
		public int getNumPartitions(){
			return 4;
		}
		@Override
		public int getPartition(SortedBeanEntityKey ek){
			String hashInput = ek.a + ek.b;
			long hash = HashMethods.longDJBHash(hashInput) % getNumPartitions();
			return (int)(hash % getNumPartitions());
		}
	}

	
	/****************** construct *******************/
	
	private SortedBeanEntityKey(){//for reflection
	}
	
	public SortedBeanEntityKey(String a, String b){
		this.a = a;
		this.b = b;
	}

	
	/********************** get/set ***************************/

	public String getA(){
		return a;
	}

	public String getB(){
		return b;
	}
	

}
