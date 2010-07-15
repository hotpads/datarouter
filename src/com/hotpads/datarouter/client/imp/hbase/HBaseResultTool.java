package com.hotpads.datarouter.client.imp.hbase;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
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
	

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	D getDatabean(Result row, Class<D> databeanClass, List<Field<?>> primaryKeyFields, Map<String,Field<?>> fieldByName){
		D databean = ReflectionTool.create(databeanClass);
		byte[] keyBytes = row.getRow();
		HBaseRow hBaseRow = new HBaseRow(keyBytes, row.getMap());//so we can see a better toString value
		setPrimaryKeyFields(databean, keyBytes, primaryKeyFields);
		//TODO use row.raw() to avoid building all these TreeMaps
		for(Map.Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> family : hBaseRow.map.entrySet()){
			for(Map.Entry<byte[], NavigableMap<Long, byte[]>> column : family.getValue().entrySet()){
				byte[] latestValue = column.getValue().lastEntry().getValue();
				//TODO peristent field names vs abbreviated vs java field names
				String fieldName = StringByteTool.fromUtf8Bytes(column.getKey());
				Field<?> field = fieldByName.get(fieldName);
				if(field==null){ continue; }
				//someListener.handleUnmappedColumn(.....
				if(ArrayTool.isEmpty(latestValue)){ continue; }
				Object value = field.fromBytesButDoNotSet(latestValue, 0);
				field.setUsingReflection(databean, value, false);
			}
		}
		return databean;
	}
	
	//TODO avoid using the whole databean if the key has its own fields
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	void setPrimaryKeyFields(D databean, byte[] bytes, List<Field<?>> primaryKeyFields){
		int byteOffset = 0;
		for(Field<?> field : primaryKeyFields){
			int numBytesWithSeparator = field.numBytesWithSeparator(bytes, byteOffset);
			Object value = field.fromBytesWithSeparatorButDoNotSet(bytes, byteOffset);
			field.setUsingReflection(databean, value, false);
			byteOffset+=numBytesWithSeparator;
		}
	}

	public static <PK extends PrimaryKey<PK>> 
	PK getPrimaryKey(byte[] keyBytes, Class<PK> primaryKeyClass, List<Field<?>> primaryKeyFields){
		PK primaryKey = ReflectionTool.create(primaryKeyClass);
		
		//copied from above
		int byteOffset = 0;
		for(Field<?> field : primaryKeyFields){
			int numBytesWithSeparator = field.numBytesWithSeparator(keyBytes, byteOffset);
			Object value = field.fromBytesWithSeparatorButDoNotSet(keyBytes, byteOffset);
			field.setUsingReflection(primaryKey, value, true);
			byteOffset+=numBytesWithSeparator;
		}
		
		return primaryKey;
	}


}
