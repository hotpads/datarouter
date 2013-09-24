package com.hotpads.datarouter.storage.field.imp.geo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.map.SQuad;

public class SQuadStringField extends BasePrimitiveField<SQuad>{

	public SQuadStringField(String name, SQuad value){
		super(name, value);
	}

	public SQuadStringField(String prefix, String name, SQuad value){
		super(prefix, name, value);
	}
	
	@Override
	public int compareTo(Field<SQuad> other){
		if(other==null){ return -1; }
		return ComparableTool.nullFirstCompareTo(value, other.getValue());
	};
	
	@Override
	public void fromString(String s){
		if(s==null){ value = null; }
		value = new SQuad(s);
	}
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.VARCHAR, SQuad.MAX_LEVEL, nullable);
	}
	
	@Override
	public String getValueString(){
		return value==null ? null : value.getMicrosoftStyleString();
	}

	@Override
	public String getSqlEscaped(){
		return "'"+value.getMicrosoftStyleString()+"'";
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.VARCHAR);
			}else{
				ps.setString(parameterIndex, value.getMicrosoftStyleString());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public SQuad parseJdbcValueButDoNotSet(Object obj){
		return obj==null ? null : new SQuad((String)obj);
	}
	
	@Override
	public SQuad fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			String s = rs.getString(columnName);
			if(s==null){ return null; }
			return new SQuad(s);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	/*********************** override *******************************/
	
	public static final byte SEPARATOR = 0;
	
	@Override
	public boolean isFixedLength(){
		return false;
	}
	
	@Override
	public byte[] getBytes(){
		if(value==null){ return new byte[]{}; }
		byte[] bytes = StringByteTool.getUtf8Bytes(value.getMicrosoftStyleString());
		return bytes;
	}
	
	@Override
	public byte[] getBytesWithSeparator(){
		//TODO someday don't put the separator after the last field, but that would break all currently persisted keys
		byte[] dataBytes = getBytes();
		if(ArrayTool.containsUnsorted(dataBytes, SEPARATOR)){
			throw new IllegalArgumentException("String cannot contain separator byteVal="+SEPARATOR);
		}
		if(ArrayTool.isEmpty(dataBytes)){ return new byte[]{SEPARATOR}; }
		byte[] allBytes = new byte[dataBytes.length+1];
		System.arraycopy(dataBytes, 0, allBytes, 0, dataBytes.length);
		allBytes[allBytes.length-1] = SEPARATOR;//Ascii "null" will compare first in lexicographical bytes comparison
		return allBytes;
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		for(int i=offset; i < bytes.length; ++i){
			if(bytes[i]==SEPARATOR){
				return i - offset + 1;//plus 1 for the separator
			}
		}
		int numBytes = bytes.length - offset;
		return numBytes >= 0 ? numBytes : 0; //not sure where the separator went.  schema change or corruption?
//		throw new IllegalArgumentException("separator not found for bytes:"+new String(bytes));
	}
	
	@Override
	public SQuad fromBytesButDoNotSet(byte[] bytes, int offset){
		int length = bytes.length - offset;
		String string = StringByteTool.fromUtf8Bytes(bytes, offset, length);
		return new SQuad(string);
	}
	
	@Override
	public SQuad fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		int lengthIncludingSeparator = numBytesWithSeparator(bytes, offset);
		boolean lastByteIsSeparator = bytes[offset + lengthIncludingSeparator - 1] == SEPARATOR;
		int lengthWithoutSeparator = lengthIncludingSeparator;
		if(lastByteIsSeparator){
			--lengthWithoutSeparator;
		}
		if (lengthWithoutSeparator == -1)
			lengthWithoutSeparator = 0;
		String string = StringByteTool.fromUtf8Bytes(bytes, offset, lengthWithoutSeparator);
		return new SQuad(string);
	}

}
