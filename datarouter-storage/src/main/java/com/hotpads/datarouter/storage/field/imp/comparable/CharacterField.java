package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class CharacterField extends BasePrimitiveField<Character>{

	public CharacterField(String name, Character value){
		super(name, value);
	}

	public CharacterField(String prefix, String name, Character value){
		super(prefix, name, value);
	}
	
	
	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.toString();
	}
	
	@Override
	public Character parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s)){ return null; }
		return s.charAt(0);
	}
	

	/*********************** ByteEncodedField ***********************/
	
	@Override
	public byte[] getBytes(){
		return value==null?null:StringByteTool.getUtf8Bytes(value.toString());
	}
	
	@Override
	public byte[] getBytesWithSeparator(){
		byte[] dataBytes = getBytes();
		if(DrArrayTool.isEmpty(dataBytes)){ return new byte[]{StringField.SEPARATOR}; }
		byte[] allBytes = new byte[dataBytes.length+1];
		System.arraycopy(dataBytes, 0, allBytes, 0, dataBytes.length);
		allBytes[allBytes.length-1] = StringField.SEPARATOR;
		return allBytes;
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		//TODO this should be reviewed for correctness
		for(int i=offset; i < bytes.length; ++i){
			if(bytes[i]==StringField.SEPARATOR){
				return i - offset + 1;//plus 1 for the separator
			}
		}
		throw new IllegalArgumentException("separator not found");
	}
	
	@Override
	public Character fromBytesButDoNotSet(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return new String(bytes, offset, length, StringByteTool.CHARSET_UTF8).charAt(0);
	}

}
