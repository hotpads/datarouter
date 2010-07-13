package com.hotpads.datarouter.storage.field.imp.comparable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class CharacterField extends PrimitiveField<Character>{

	public CharacterField(String name, Character value){
		super(name, value);
	}

	public CharacterField(String prefix, String name, Character value){
		super(prefix, name, value);
	}

	@Override
	public Character parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Character)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.VARCHAR);
			}else{
				ps.setString(parameterIndex, this.value==null?null:this.value+"");
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Character fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			String s = rs.getString(this.name);
			return s==null?null:s.charAt(0);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public byte[] getBytes(){
		return value==null?null:StringByteTool.getUtf8Bytes(this.value.toString());
	}
	
	@Override
	public byte[] getBytesWithSeparator(){
		byte[] dataBytes = getBytes();
		if(ArrayTool.isEmpty(dataBytes)){ return new byte[]{StringField.SEPARATOR}; }
		byte[] allBytes = new byte[dataBytes.length+1];
		System.arraycopy(dataBytes, 0, allBytes, 0, dataBytes.length);
		allBytes[allBytes.length-1] = 0;//Ascii "null" will compare first in lexicographical bytes comparison
		return allBytes;
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		for(int i=offset; i < bytes.length; ++i){
			if(bytes[i]==StringField.SEPARATOR){
				return i - offset + 1;//plus 1 for the separator
			}
		}
		throw new IllegalArgumentException("separator not found");
	}
	
	@Override
	public Character fromBytesButDoNotSet(byte[] bytes, int offset){
		int length = numBytesWithSeparator(bytes, offset) - 1;
		return new String(bytes, offset + 1, length, StringByteTool.CHARSET_UTF8).charAt(0);
	}

}
