package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.java.ReflectionTool;

public class HBaseEntityResultTool{

	/****************** multiple databeans in one hbase row ********************/
		
	public static <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>> 
	List<D> getDatabeans(Result row, DatabeanFieldInfo<PK,D,F> fieldInfo){
		List<D> results = ListTool.createArrayList();
		if(row==null){ return results; }
		byte[] entityColumnPrefixBytes = fieldInfo.getEntityColumnPrefixBytes();
		List<KeyValue> kvs = ListTool.createArrayList();
		for(KeyValue kv : row.list()){
			byte[] qualifier = kv.getQualifier();
			if(!Bytes.startsWith(qualifier, entityColumnPrefixBytes)){ continue; }
			int numNonColumnPrefixBytes = qualifier.length - entityColumnPrefixBytes.length;
			byte[] pkPlusColumnBytes = ByteTool.copyOfRange(qualifier, entityColumnPrefixBytes.length, 
					numNonColumnPrefixBytes);
			byte[] pkPlusFieldNameBytes = ByteTool.concatenate(row.getRow(), pkPlusColumnBytes);
			Pair<PK,Integer> pkAndLength = getPrimaryKeyUncheckedAndLength(pkPlusFieldNameBytes, fieldInfo);
			int fieldNameOffset = pkAndLength.getRight();
			String fieldName = StringByteTool.fromUtf8BytesOffset(pkPlusFieldNameBytes, fieldNameOffset);
			D result = getDatabean(row, fieldInfo);
			results.add(result);
		}
		return results;
	}
	
	
	/**************** single databean ***************************/
	
	public static <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	D getDatabean(PK pk, Map<String,KeyValue> kvByFieldName, DatabeanFieldInfo<PK,D,F> fieldInfo){
		D databean = ReflectionTool.create(fieldInfo.getDatabeanClass());
		
	}

	
	/****************** parse single result ********************/

	
	public static <EK extends EntityKey<EK>,PK extends EntityPrimaryKey<EK,PK>> 
	Pair<PK,Integer> getPrimaryKeyUncheckedAndLength(byte[] pkPlusFieldNameBytes, DatabeanFieldInfo<PK,?,?> fieldInfo){
		@SuppressWarnings("unchecked")
		PK primaryKey = (PK)ReflectionTool.create(fieldInfo.getPrimaryKeyClass());
		if(ArrayTool.isEmpty(pkPlusFieldNameBytes)){ return Pair.create(primaryKey, 0); }
		
		//copied from above
		int byteOffset = 0;
		for(Field<?> field : fieldInfo.getPrimaryKeyFields()){
			if(byteOffset==pkPlusFieldNameBytes.length){ break; }//ran out of bytes.  leave remaining fields blank
			int numBytesWithSeparator = field.numBytesWithSeparator(pkPlusFieldNameBytes, byteOffset);
			Object value = field.fromBytesWithSeparatorButDoNotSet(pkPlusFieldNameBytes, byteOffset);
			field.setUsingReflection(primaryKey, value);
			byteOffset+=numBytesWithSeparator;
		}
		
		return Pair.create(primaryKey, byteOffset);
	}
}
