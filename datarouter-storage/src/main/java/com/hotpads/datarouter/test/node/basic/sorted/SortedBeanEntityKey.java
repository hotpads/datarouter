package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.Arrays;
import java.util.List;


import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityKey;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityPartitioner;
import com.hotpads.datarouter.storage.key.entity.base.NoOpEntityPartitioner;
import com.hotpads.datarouter.util.core.DrHashMethods;

public class SortedBeanEntityKey
extends BaseEntityKey<SortedBeanEntityKey>{

	/************* fields *************************/
	private String a;
	private String b;

	public static class FieldKeys{
		public static final StringFieldKey a = new StringFieldKey("a").withSize(MySqlColumnType.MAX_KEY_LENGTH_UTF8MB4);
		public static final StringFieldKey b = new StringFieldKey("b").withSize(MySqlColumnType.MAX_KEY_LENGTH_UTF8MB4);
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.a, a),
				new StringField(FieldKeys.b, b));
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
			long hash = DrHashMethods.longDjbHash(hashInput) % getNumPartitions();
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
