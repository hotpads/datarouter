package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

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

public class HBaseSubEntityResultParser<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static Logger logger = Logger.getLogger(HBaseSubEntityResultParser.class);

	private DatabeanFieldInfo<PK,D,F> fieldInfo;
	
	
	public HBaseSubEntityResultParser(DatabeanFieldInfo<PK,D,F> fieldInfo){
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
		NavigableSet<PK> pks = SetTool.createTreeSet();//unfortunately, we expect a bunch of duplicate PK's, so throw them in a set
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
		Map<PK,D> databeanByKey = MapTool.createTreeMap();
		for(KeyValue kv : row.list()){
			if(!matchesNodePrefix(kv)){ continue; }
			Pair<PK,String> pkAndFieldName = parsePrimaryKeyAndFieldName(kv);
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
	
	private boolean matchesNodePrefix(KeyValue kv){
		byte[] qualifier = kv.getQualifier();
		return Bytes.startsWith(qualifier, fieldInfo.getEntityColumnPrefixBytes());
	}
	
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
	
	private int parseFieldsFromBytesToPk(List<Field<?>> fields, byte[] fromBytes, PK targetPk){
		int byteOffset = 0;
		for(Field<?> field : fields){
			Object value = field.fromBytesWithSeparatorButDoNotSet(fromBytes, byteOffset);
			field.setUsingReflection(targetPk, value);
			byteOffset += field.numBytesWithSeparator(fromBytes, byteOffset);
			if(byteOffset==fromBytes.length){ break; }//ran out of bytes.  leave remaining fields blank
		}
		return byteOffset;
	}
}
