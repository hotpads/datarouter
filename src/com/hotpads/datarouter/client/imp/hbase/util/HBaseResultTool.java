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
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;

public class HBaseResultTool{
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	D getDatabean(Result row, DatabeanFieldInfo<PK,D,F> fieldInfo){
		D databean = ReflectionTool.create(fieldInfo.getDatabeanClass());
		byte[] keyBytes = getKeyBytesWithoutScatteringPrefix(fieldInfo, row.getRow());
		int numScatteringPrefixBytes = fieldInfo.getSampleScatteringPrefix().getNumPrefixBytes();
		if(numScatteringPrefixBytes > 0){
			keyBytes = Arrays.copyOfRange(keyBytes, numScatteringPrefixBytes, keyBytes.length);
		}
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
				if(ArrayTool.isEmpty(latestValue)){ continue; }
				Object value = field.fromBytesButDoNotSet(latestValue, 0);
				field.setUsingReflection(databean, value);
			}
		}
		return databean;
	}
	
	
	//anticipates that you will pass the PK's fields with no prefixes
	public static void setPrimaryKeyFields(FieldSet<?> primaryKey, byte[] bytes, List<Field<?>> primaryKeyFields){
		int byteOffset = 0;
		for(Field<?> field : primaryKeyFields){
			int numBytesWithSeparator = field.numBytesWithSeparator(bytes, byteOffset);
			Object value = field.fromBytesWithSeparatorButDoNotSet(bytes, byteOffset);
			field.setUsingReflection(primaryKey, value);
			byteOffset+=numBytesWithSeparator;
		}
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>  
	PK getPrimaryKey(byte[] keyBytes, DatabeanFieldInfo<PK,D,F> fieldInfo){
		return getPrimaryKeyUnchecked(keyBytes, fieldInfo);
	}
	
	public static <PK extends PrimaryKey<PK>> 
	PK getPrimaryKeyUnchecked(byte[] rowBytes, DatabeanFieldInfo<?,?,?> fieldInfo){
		byte[] keyBytesWithoutScatteringPrefix = getKeyBytesWithoutScatteringPrefix(fieldInfo, rowBytes);
		@SuppressWarnings("unchecked")
		PK primaryKey = (PK)ReflectionTool.create(fieldInfo.getPrimaryKeyClass());
		
		//copied from above
		int byteOffset = 0;
		for(Field<?> field : fieldInfo.getPrimaryKeyFields()){
			int numBytesWithSeparator = field.numBytesWithSeparator(keyBytesWithoutScatteringPrefix, byteOffset);
			Object value = field.fromBytesWithSeparatorButDoNotSet(keyBytesWithoutScatteringPrefix, byteOffset);
			field.setUsingReflection(primaryKey, value);
			byteOffset+=numBytesWithSeparator;
		}
		
		return primaryKey;
	}
	
	
	/*********************** helper *********************************/

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
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
