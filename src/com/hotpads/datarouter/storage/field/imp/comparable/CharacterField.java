package com.hotpads.datarouter.storage.field.imp.comparable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class CharacterField extends BasePrimitiveField<Character>{

	public CharacterField(String name, Character value){
		super(name, value);
	}

	public CharacterField(String prefix, String name, Character value){
		super(prefix, name, value);
	}
	
	@Override
	public void fromString(String s){
		this.value = StringTool.isEmpty(s)?null:s.charAt(0);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.CHAR, 1, nullable);
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
			String value = rs.getString(columnName);
			return rs.wasNull()?null:value.charAt(0);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public byte[] getBytes(){
		return value==null?null:StringByteTool.getUtf8Bytes(value.toString());
	}
	
	@Override
	public byte[] getBytesWithSeparator(){
		byte[] dataBytes = getBytes();
		if(ArrayTool.isEmpty(dataBytes)){ return new byte[]{StringField.SEPARATOR}; }
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
