package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;

public class HBaseResultTool{

	/****************** parse multiple results ********************/
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	List<PK> getPrimaryKeys(List<Result> rows, DatabeanFieldInfo<PK,D,F> fieldInfo){
		List<PK> results = DrListTool.createArrayListWithSize(rows);
		for(Result row : rows){
			if(row==null || row.isEmpty()){ continue; }
			PK result = getPrimaryKey(row.getRow(), fieldInfo);
			results.add(result);
		}
		return results;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	List<D> getDatabeans(List<Result> rows, DatabeanFieldInfo<PK,D,F> fieldInfo){
		List<D> results = DrListTool.createArrayListWithSize(rows);
		for(Result row : rows){
			if(row==null || row.isEmpty()){ continue; }
			D result = getDatabean(row, fieldInfo);
			results.add(result);
		}
		return results;
	}

	
	/****************** parse single result ********************/

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>  
	PK getPrimaryKey(byte[] keyBytes, DatabeanFieldInfo<PK,D,F> fieldInfo){
		return getPrimaryKeyUnchecked(keyBytes, fieldInfo);
	}
	
	//TODO use FieldSetTool.fromConcatenatedValueBytes
	public static <PK extends PrimaryKey<PK>> 
	PK getPrimaryKeyUnchecked(byte[] rowBytes, DatabeanFieldInfo<?,?,?> fieldInfo){
		@SuppressWarnings("unchecked")
		PK primaryKey = (PK)ReflectionTool.create(fieldInfo.getPrimaryKeyClass());
		if(DrArrayTool.isEmpty(rowBytes)){ return primaryKey; }
		
		byte[] keyBytesWithoutScatteringPrefix = getKeyBytesWithoutScatteringPrefix(fieldInfo, rowBytes);
		//copied from above
		int byteOffset = 0;
		for(Field<?> field : fieldInfo.getPrimaryKeyFields()){
			if(byteOffset==keyBytesWithoutScatteringPrefix.length){ break; }//ran out of bytes.  leave remaining fields blank
			int numBytesWithSeparator = field.numBytesWithSeparator(keyBytesWithoutScatteringPrefix, byteOffset);
			Object value = field.fromBytesWithSeparatorButDoNotSet(keyBytesWithoutScatteringPrefix, byteOffset);
			field.setUsingReflection(primaryKey, value);
			byteOffset+=numBytesWithSeparator;
		}
		
		return primaryKey;
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	D getDatabean(Result row, DatabeanFieldInfo<PK,D,F> fieldInfo){
		D databean = ReflectionTool.create(fieldInfo.getDatabeanClass());
		byte[] keyBytes = getKeyBytesWithoutScatteringPrefix(fieldInfo, row.getRow());
		HBaseRow hBaseRow = new HBaseRow(keyBytes, row.getMap());//so we can see a better toString value
		setPrimaryKeyFields(databean.getKey(), keyBytes, fieldInfo.getPrimaryKeyFields());
		//TODO use row.raw() to avoid building all these TreeMaps
		for(Map.Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> family : hBaseRow.map.entrySet()){
			for(Map.Entry<byte[], NavigableMap<Long, byte[]>> column : family.getValue().entrySet()){
				byte[] latestValue = column.getValue().lastEntry().getValue();
				String fieldName = StringByteTool.fromUtf8Bytes(column.getKey());
				Field<?> field = fieldInfo.getNonKeyFieldByColumnName().get(fieldName);//skip key fields which may have been accidenally inserted
				if(field==null){ continue; }//skip dummy fields and fields that may have existed in the past
				//someListener.handleUnmappedColumn(.....
				if(DrArrayTool.isEmpty(latestValue)){ continue; }
				Object value = field.fromBytesButDoNotSet(latestValue, 0);
				field.setUsingReflection(databean, value);
			}
		}
		return databean;
	}
	
	
	
	/*********************** helper *********************************/

	//anticipates that you will pass the PK's fields with no prefixes
	private static void setPrimaryKeyFields(FieldSet<?> primaryKey, byte[] bytes, List<Field<?>> primaryKeyFields){
		int byteOffset = 0;
		for(Field<?> field : primaryKeyFields){
			//this should not be zero, but could be if bad data leaked in.  try setting missing fields to null
			int numBytesWithSeparator = field.numBytesWithSeparator(bytes, byteOffset);
			Object value = null;
			if(numBytesWithSeparator > 0){
				value = field.fromBytesWithSeparatorButDoNotSet(bytes, byteOffset);
			}
			field.setUsingReflection(primaryKey, value);
			byteOffset+=numBytesWithSeparator;
		}
	}
	
	private static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	byte[] getKeyBytesWithoutScatteringPrefix(DatabeanFieldInfo<PK,D,F> fieldInfo, byte[] keyBytesWithScatteringPrefix){
		int numScatteringPrefixBytes = fieldInfo.getSampleScatteringPrefix().getNumPrefixBytes();
		if(numScatteringPrefixBytes == 0){
			return keyBytesWithScatteringPrefix;
		}
		byte[] keyBytesWithoutScatteringPrefix= Arrays.copyOfRange(keyBytesWithScatteringPrefix, 
				numScatteringPrefixBytes, keyBytesWithScatteringPrefix.length);
		return keyBytesWithoutScatteringPrefix;
	}

}
