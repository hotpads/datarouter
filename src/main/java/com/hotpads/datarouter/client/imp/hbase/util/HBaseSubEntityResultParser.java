package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.java.ReflectionTool;

public class HBaseSubEntityResultParser<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static Logger logger = Logger.getLogger(HBaseSubEntityResultParser.class);

	private EntityFieldInfo<EK,E> entityFieldInfo;
	private DatabeanFieldInfo<PK,D,F> fieldInfo;
	
	
	public HBaseSubEntityResultParser(EntityFieldInfo<EK,E> entityFieldInfo, DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.entityFieldInfo = entityFieldInfo;
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
		if(row==null){ return new TreeSet<>(); }
		NavigableSet<PK> pks = new TreeSet<>();//unfortunately, we expect a bunch of duplicate PK's, so throw them in a set
		for(KeyValue kv : row.list()){
			if(!matchesNodePrefix(kv)){ continue; }
			Pair<PK,String> pkAndFieldName = parsePrimaryKeyAndFieldName(kv);
			PK pk = pkAndFieldName.getLeft();
			pks.add(pk);
		}
		return pks;
	}
		
	public List<D> getDatabeansWithMatchingQualifierPrefix(Result row){
		if(row==null){ return ListTool.createLinkedList(); }
		Map<PK,D> databeanByKey = new TreeMap<>();
		for(KeyValue kv : row.list()){
			if(!matchesNodePrefix(kv)){ continue; }
			addKeyValueToResultsUnchecked(databeanByKey, kv);
		}
		return ListTool.createArrayList(databeanByKey.values());
	}	
	
	
	public void addKeyValueToResultsUnchecked(
			Map<? extends EntityPrimaryKey<?,?>,? extends Databean<?,?>> uncheckedDatabeanByPk, 
			KeyValue kv){
		@SuppressWarnings("unchecked") Map<PK,D> databeanByPk = (Map<PK,D>)uncheckedDatabeanByPk;
		Pair<PK,String> pkAndFieldName = parsePrimaryKeyAndFieldName(kv);
		Field<?> field = fieldInfo.getNonKeyFieldByColumnName().get(pkAndFieldName.getRight());//skip key fields which may have been accidenally inserted
		if(field==null){ return; }//skip dummy fields and fields that may have existed in the past
		PK pk = pkAndFieldName.getLeft();
		D databean = databeanByPk.get(pk);
		if(databean==null){
			databean = ReflectionTool.create(fieldInfo.getDatabeanClass());
			ReflectionTool.set(fieldInfo.getKeyJavaField(), databean, pk);
			databeanByPk.put(pk, databean);
		}
		
		//set the databean field value for this hbase cell
		Object value = field.fromBytesButDoNotSet(kv.getValue(), 0);
		field.setUsingReflection(databean, value);
	}

	
	/****************** private ********************/
	
	private Pair<PK,String> parsePrimaryKeyAndFieldName(KeyValue kv){
		PK pk = ReflectionTool.create(fieldInfo.getPrimaryKeyClass());
		//EK
		byte[] rowBytes = kv.getRow();
		parseFieldsFromBytesToPk(fieldInfo.getEntityKeyFields(), rowBytes, pk);
		//post-EK
		byte[] qualifier = kv.getQualifier();
		byte[] postPrefixQualifierBytes = ByteTool.copyOfRangeFromOffset(qualifier, 
				fieldInfo.getEntityColumnPrefixBytes().length);
		//fieldName
		int fieldNameOffset = parseFieldsFromBytesToPk(fieldInfo.getPostEkPkKeyFields(), 
				postPrefixQualifierBytes, pk);
		String fieldName = StringByteTool.fromUtf8BytesOffset(postPrefixQualifierBytes, fieldNameOffset);
		return Pair.create(pk, fieldName);
	}
	
	private boolean matchesNodePrefix(KeyValue kv){
		byte[] qualifier = kv.getQualifier();
		return Bytes.startsWith(qualifier, fieldInfo.getEntityColumnPrefixBytes());
	}
	
	private int parseFieldsFromBytesToPk(List<Field<?>> fields, byte[] fromBytes, PK targetPk){
		int byteOffset = entityFieldInfo.getEntityPartitioner().getNumPrefixBytes();
		for(Field<?> field : fields){
			Object value = field.fromBytesWithSeparatorButDoNotSet(fromBytes, byteOffset);
			field.setUsingReflection(targetPk, value);
			byteOffset += field.numBytesWithSeparator(fromBytes, byteOffset);
			if(byteOffset==fromBytes.length){ break; }//ran out of bytes.  leave remaining fields blank
		}
		return byteOffset;
	}
}
