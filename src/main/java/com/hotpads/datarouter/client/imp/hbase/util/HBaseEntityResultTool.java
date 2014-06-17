package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

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
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.java.ReflectionTool;

public class HBaseEntityResultTool<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private DatabeanFieldInfo<PK,D,F> fieldInfo;
	
	
	public HBaseEntityResultTool(DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.fieldInfo = fieldInfo;
	}


	/****************** parse multiple hbase rows ********************/

	public List<D> getDatabeansWithMatchingQualifierPrefix(Result[] rows){
		List<D> results = ListTool.createArrayList();
		for(Result row : rows){
			if(row.isEmpty()){ continue; }
			List<D> databeansFromSingleGet = getDatabeansWithMatchingQualifierPrefix(row);
			results.addAll(CollectionTool.nullSafe(databeansFromSingleGet));
		}
		return results;
	}

	
	/****************** parse single hbase row ********************/
	
	public NavigableSet<PK> getPrimaryKeysWithMatchingQualifierPrefix(Result row){
		if(row==null){ return SetTool.createTreeSet(); }
		byte[] entityColumnPrefixBytes = fieldInfo.getEntityColumnPrefixBytes();//the table name with separator byte
		NavigableSet<PK> pks = SetTool.createTreeSet();
		for(KeyValue kv : row.list()){
			byte[] qualifier = kv.getQualifier();
			if(!Bytes.startsWith(qualifier, entityColumnPrefixBytes)){ continue; }
			int numNonColumnPrefixBytes = qualifier.length - entityColumnPrefixBytes.length;
			byte[] postEkPkPlusColumnBytes = ByteTool.copyOfRange(qualifier, entityColumnPrefixBytes.length, 
					numNonColumnPrefixBytes);
			byte[] pkPlusFieldNameBytes = ByteTool.concatenate(row.getRow(), postEkPkPlusColumnBytes);
			Pair<PK,String> pkAndFieldName = getPrimaryKeyAndFieldName(pkPlusFieldNameBytes);
			PK pk = pkAndFieldName.getLeft();
			pks.add(pk);
		}
		return pks;
	}
		
	public List<D> getDatabeansWithMatchingQualifierPrefix(Result row){
		if(row==null){ return ListTool.createLinkedList(); }
		byte[] entityColumnPrefixBytes = fieldInfo.getEntityColumnPrefixBytes();//the table name with separator byte
		Map<PK,D> databeanByKey = MapTool.createTreeMap();
		for(KeyValue kv : row.list()){
			byte[] qualifier = kv.getQualifier();
			if(!Bytes.startsWith(qualifier, entityColumnPrefixBytes)){ continue; }
			int numNonColumnPrefixBytes = qualifier.length - entityColumnPrefixBytes.length;
			byte[] postEkPkPlusColumnBytes = ByteTool.copyOfRange(qualifier, entityColumnPrefixBytes.length, 
					numNonColumnPrefixBytes);
			byte[] pkPlusFieldNameBytes = ByteTool.concatenate(row.getRow(), postEkPkPlusColumnBytes);
			Pair<PK,String> pkAndFieldName = getPrimaryKeyAndFieldName(pkPlusFieldNameBytes);
			PK pk = pkAndFieldName.getLeft();
			
			//get or create the databean, and set the pk which we already parsed
			D databean = databeanByKey.get(pk);
			if(databean==null){
				databean = ReflectionTool.create(fieldInfo.getDatabeanClass());
				ReflectionTool.set(fieldInfo.getKeyJavaField(), databean, pk);
				databeanByKey.put(pk, databean);
			}
			
			//set the databean field value for this hbase cell
			Field<?> field = fieldInfo.getNonKeyFieldByColumnName().get(pkAndFieldName.getRight());//skip key fields which may have been accidenally inserted
			if(field==null){ continue; }//skip dummy fields and fields that may have existed in the past
			Object value = field.fromBytesButDoNotSet(kv.getValue(), 0);
			field.setUsingReflection(databean, value);
		}
		return ListTool.createArrayList(databeanByKey.values());
	}
	

	
	/****************** private ********************/

	
	private Pair<PK,String> getPrimaryKeyAndFieldName(byte[] pkPlusFieldNameBytes){
		if(ArrayTool.isEmpty(pkPlusFieldNameBytes)){ throw new IllegalArgumentException("pkPlusFieldNameBytes is empty"); }
		PK primaryKey = ReflectionTool.create(fieldInfo.getPrimaryKeyClass());
		
		//copied from above
		int byteOffset = 0;
		for(Field<?> field : fieldInfo.getPrimaryKeyFields()){
			if(byteOffset==pkPlusFieldNameBytes.length){ break; }//ran out of bytes.  leave remaining fields blank
			int numBytesWithSeparator = field.numBytesWithSeparator(pkPlusFieldNameBytes, byteOffset);
			Object value = field.fromBytesWithSeparatorButDoNotSet(pkPlusFieldNameBytes, byteOffset);
			field.setUsingReflection(primaryKey, value);
			byteOffset += numBytesWithSeparator;
		}
		String fieldName = StringByteTool.fromUtf8BytesOffset(pkPlusFieldNameBytes, byteOffset);
		return Pair.create(primaryKey, fieldName);
	}
}
