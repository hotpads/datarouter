package com.hotpads.datarouter.client.imp.hbase.util;

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
	
	public static class HBaseRow{
		byte[] key;
		NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map;

		public HBaseRow(byte[] key, NavigableMap<byte[],NavigableMap<byte[],NavigableMap<Long,byte[]>>> map){
			this.key = key;
			this.map = map;
		}
		
		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append(StringByteTool.fromUtf8Bytes(key));
			sb.append("={\n");
			for(Map.Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> family : map.entrySet()){
				for(Map.Entry<byte[], NavigableMap<Long, byte[]>> column : family.getValue().entrySet()){
					for(Map.Entry<Long,byte[]> cell : column.getValue().entrySet()){
						String familyName = StringByteTool.fromUtf8Bytes(family.getKey());
						String columnName = StringByteTool.fromUtf8Bytes(column.getKey());
						Long version = cell.getKey();
						int numBytes = ArrayTool.length(cell.getValue());
						String valueString = StringByteTool.fromUtf8Bytes(cell.getValue());
						sb.append("\t"+version+":"+familyName+":"+columnName+"("+numBytes+"b)="+valueString+"\n");
					}
				}
			}
			sb.append("}");
			return sb.toString();
		}
	}
	

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	D getDatabean(Result row, DatabeanFieldInfo<PK,D,F> fieldInfo){
		D databean = ReflectionTool.create(fieldInfo.getDatabeanClass());
		byte[] keyBytes = row.getRow();
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
	PK getPrimaryKeyUnchecked(byte[] keyBytes, DatabeanFieldInfo<PK,?,?> fieldInfo){
		PK primaryKey = ReflectionTool.create(fieldInfo.getPrimaryKeyClass());
		
		//copied from above
		int byteOffset = 0;
		for(Field<?> field : fieldInfo.getPrimaryKeyFields()){
			int numBytesWithSeparator = field.numBytesWithSeparator(keyBytes, byteOffset);
			Object value = field.fromBytesWithSeparatorButDoNotSet(keyBytes, byteOffset);
			field.setUsingReflection(primaryKey, value);
			byteOffset+=numBytesWithSeparator;
		}
		
		return primaryKey;
	}


}
