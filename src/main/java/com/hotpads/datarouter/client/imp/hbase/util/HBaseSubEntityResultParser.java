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
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.java.ReflectionTool;

public class HBaseSubEntityResultParser<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = Logger.getLogger(HBaseSubEntityResultParser.class);

	private EntityFieldInfo<EK,E> entityFieldInfo;
	private EntityPartitioner<EK> partitioner;
	private DatabeanFieldInfo<PK,D,F> fieldInfo;
	
	
	public HBaseSubEntityResultParser(EntityFieldInfo<EK,E> entityFieldInfo, DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.entityFieldInfo = entityFieldInfo;
		this.partitioner = entityFieldInfo.getEntityPartitioner();
		this.fieldInfo = fieldInfo;
	}
	
	
	/***************** parse simple row bytes ************************/
	
	public EK getEkFromRowBytes(byte[] rowBytes){
		EK ek = ReflectionTool.create(entityFieldInfo.getEntityKeyClass());
		int byteOffset = partitioner.getNumPrefixBytes();
		for(Field<?> field : fieldInfo.getEkFields()){
			if(byteOffset==rowBytes.length){ break; }//ran out of bytes.  leave remaining fields blank
			Object value = field.fromBytesWithSeparatorButDoNotSet(rowBytes, byteOffset);
			field.setUsingReflection(ek, value);
			byteOffset += field.numBytesWithSeparator(rowBytes, byteOffset);
		}
		return ek;
	}


	/****************** parse multiple hbase rows ********************/

	public List<PK> getPrimaryKeysWithMatchingQualifierPrefix(Result[] rows){
		List<PK> results = ListTool.createArrayList();
		for(Result row : rows){
			if(row.isEmpty()){ continue; }
			NavigableSet<PK> pksFromSingleGet = getPrimaryKeysWithMatchingQualifierPrefix(row);
			results.addAll(CollectionTool.nullSafe(pksFromSingleGet));
		}
		return results;
	}

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
		for(KeyValue kv : IterableTool.nullSafe(row.list())){//row.list() can return null
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
		for(KeyValue kv : IterableTool.nullSafe(row.list())){//row.list() can return null
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
		//be sure to get the entity key fields from DatabeanFieldInfo in case the PK overrode the EK field names
		parseEkFieldsFromBytesToPk(kv, pk);
		//post-EK
		int fieldNameOffset = parsePostEkFieldsFromBytesToPk(kv, pk);
		//fieldName
		String fieldName = StringByteTool.fromUtf8BytesOffset(kv.getQualifier(), fieldNameOffset);
		return Pair.create(pk, fieldName);
	}
	
	private boolean matchesNodePrefix(KeyValue kv){
		byte[] qualifier = kv.getQualifier();
		return Bytes.startsWith(qualifier, fieldInfo.getEntityColumnPrefixBytes());
	}
	
	//parse the hbase row bytes after the partition offset
	private int parseEkFieldsFromBytesToPk(KeyValue kv, PK targetPk){
		int offset = partitioner.getNumPrefixBytes();
		byte[] fromBytes = kv.getRow();
		return parseFieldsFromBytesToPk(fieldInfo.getEkPkFields(), fromBytes, offset, targetPk);
	}
	
	//parse the hbase qualifier bytes
	private int parsePostEkFieldsFromBytesToPk(KeyValue kv, PK targetPk){
		byte[] entityColumnPrefixBytes = fieldInfo.getEntityColumnPrefixBytes();
		int offset = entityColumnPrefixBytes.length;
		byte[] fromBytes = kv.getQualifier();
		return parseFieldsFromBytesToPk(fieldInfo.getPostEkPkKeyFields(), fromBytes, offset, targetPk);
	}
	
	private int parseFieldsFromBytesToPk(List<Field<?>> fields, byte[] fromBytes, int offset, PK targetPk){
		int byteOffset = offset;
		for(Field<?> field : fields){
			if(byteOffset==fromBytes.length){ break; }//ran out of bytes.  leave remaining fields blank
			Object value = field.fromBytesWithSeparatorButDoNotSet(fromBytes, byteOffset);
			field.setUsingReflection(targetPk, value);
			byteOffset += field.numBytesWithSeparator(fromBytes, byteOffset);
		}
		return byteOffset;
	}
}
