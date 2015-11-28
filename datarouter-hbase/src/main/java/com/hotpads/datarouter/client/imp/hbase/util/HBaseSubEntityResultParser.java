package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
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
		List<PK> results = new ArrayList<>();
		for(Result row : rows){
			if(row.isEmpty()){ continue; }
			NavigableSet<PK> pksFromSingleGet = getPrimaryKeysWithMatchingQualifierPrefix(row);
			results.addAll(DrCollectionTool.nullSafe(pksFromSingleGet));
		}
		return results;
	}

	public List<D> getDatabeansWithMatchingQualifierPrefix(Result[] rows){
		List<D> results = new ArrayList<>();
		for(Result row : rows){
			if(row.isEmpty()){ continue; }
			List<D> databeansFromSingleGet = getDatabeansWithMatchingQualifierPrefix(row, null);
			results.addAll(DrCollectionTool.nullSafe(databeansFromSingleGet));
		}
		return results;
	}


	/****************** parse single hbase row ********************/
	public NavigableSet<PK> getPrimaryKeysWithMatchingQualifierPrefix(Result row){
		return getPrimaryKeysWithMatchingQualifierPrefix(row, null);
	}
	public NavigableSet<PK> getPrimaryKeysWithMatchingQualifierPrefix(Result row, Integer limit){
		if(row == null) {
			return new TreeSet<>();
		}
		//unfortunately, we expect a bunch of duplicate PK's, so throw them in a set
		//TODO stop using a Set
		NavigableSet<PK> pks = new TreeSet<>();
		for(KeyValue kv : DrIterableTool.nullSafe(row.list())){//row.list() can return null
			if(!matchesNodePrefix(kv)) {
				continue;
			}
			Pair<PK,String> pkAndFieldName = parsePrimaryKeyAndFieldName(kv);
			PK pk = pkAndFieldName.getLeft();
			pks.add(pk);
			if(limit != null && pks.size() >= limit){
				break;
			}
		}
		return pks;
	}

	public List<D> getDatabeansWithMatchingQualifierPrefix(Result row, Integer limit){
		if(row == null) {
			return Collections.emptyList();
		}
		return getDatabeansForKvsWithMatchingQualifierPrefix(row.list(), limit);
	}

	public List<D> getDatabeansForKvsWithMatchingQualifierPrefix(List<KeyValue> kvs, Integer limit){
		if(DrCollectionTool.isEmpty(kvs)) {
			return Collections.emptyList();
		}
		List<D> databeans = new ArrayList<>();
		D databean = null;
		for(KeyValue kv : kvs){//row.list() can return null
			if(!matchesNodePrefix(kv)) {
				continue;
			}
			Pair<PK,String> pkAndFieldName = parsePrimaryKeyAndFieldName(kv);
			if(databean == null || DrObjectTool.notEquals(databean.getKey(), pkAndFieldName.getLeft())){
				//we're about to start a new databean
				if(limit != null && databeans.size() == limit){
					break;
				}
				databean = fieldInfo.getDatabeanSupplier().get();
				ReflectionTool.set(fieldInfo.getKeyJavaField(), databean, pkAndFieldName.getLeft());
				databeans.add(databean);
			}
			setDatabeanField(databean, pkAndFieldName.getRight(), kv.getValue());
		}
		return databeans;
	}

	public void setDatabeanField(D databean, String fieldName, byte[] bytesValue){
		Field<?> field = null;
		if(HBaseSubEntityNode.DUMMY.equals(fieldName)){
			return;
		}
		field = fieldInfo.getNonKeyFieldByColumnName().get(fieldName);
		if(field == null){//field doesn't exist in the databean anymore.  skip it
			return;
		}
		//set the databean field value for this hbase cell
		Object value = field.fromBytesButDoNotSet(bytesValue, 0);
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
