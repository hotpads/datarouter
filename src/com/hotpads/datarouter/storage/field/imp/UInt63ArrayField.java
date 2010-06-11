package com.hotpads.datarouter.storage.field.imp;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class UInt63ArrayField extends Field<List<Long>>{

	public UInt63ArrayField(String name, List<Long> value){
		super(name, value);
	}

	public UInt63ArrayField(String prefix, String name, List<Long> value){
		super(prefix, name, value);
	}
	
	@Override
	public int compareTo(Field<List<Long>> other){
		return ListTool.compare(this.value, other.getValue());
	}

	@Override
	public List<Long> parseJdbcValueButDoNotSet(Object obj){
		throw new NotImplementedException("code needs testing");
//		if(obj==null){ return null; }
//		byte[] bytes = (byte[])obj;
//		return new LongArray(LongByteTool.fromUInt63Bytes(bytes));
	}
	
	@Override
	public String getSqlEscaped(){
		throw new NotImplementedException("and probably never will be");
	};
	

	@Override
	public byte[] getBytes(){
		return LongByteTool.getUInt63Bytes(value);
	}

}
