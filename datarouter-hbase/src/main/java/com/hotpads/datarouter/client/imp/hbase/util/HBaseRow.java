package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.Map;
import java.util.NavigableMap;

import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class HBaseRow{
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
					int numBytes = DrArrayTool.length(cell.getValue());
					String valueString = StringByteTool.fromUtf8Bytes(cell.getValue());
					sb.append("\t"+version+":"+familyName+":"+columnName+"("+numBytes+"b)="+valueString+"\n");
				}
			}
		}
		sb.append("}");
		return sb.toString();
	}
}