package com.hotpads.datarouter.storage.field.imp.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.bind.DatatypeConverter;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class ByteArrayField extends BaseField<byte[]>{

	public ByteArrayField(String name, byte[] value){
		super(name, value);
	}

	public ByteArrayField(String prefix, String name, byte[] value){
		super(prefix, name, value);
	}
	
	
	/*********************** StringEncodedField ***********************/
	
	@Override
	public String getStringEncodedValue(){
		if(value == null){ return null; }
		return DatatypeConverter.printBase64Binary(value);
	}
	
	@Override
	public byte[] parseStringEncodedValueButDoNotSet(String s){
		return DatatypeConverter.parseBase64Binary(s);
	}
	

	/*********************** ByteEncodedField ***********************/
	
	@Override
	public byte[] getBytes(){
		return value==null?null:ByteTool.flipToAndFromComparableByteArray(this.value);
	}
	
	@Override
	public boolean isFixedLength(){
		return false;
	}
	
	@Override
	public byte[] getBytesWithSeparator(){
		if(this.value==null){ return null; }
		//prepend the length as a positive integer (not bitwise comparable =( )
		//TODO replace with varint
		byte[] dataBytes = ByteTool.flipToAndFromComparableByteArray(value);//TODO write directly to the allBytes array
		byte[] allBytes = new byte[4+ArrayTool.length(dataBytes)];
		System.arraycopy(IntegerByteTool.getUInt31Bytes(0), 0, allBytes, 4, 4);
		System.arraycopy(dataBytes, 0, allBytes, 4, ArrayTool.length(dataBytes));
		return allBytes;
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return IntegerByteTool.fromUInt31Bytes(bytes, offset);//should we be adding 4 here?
	}
	
	@Override
	public byte[] fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		int numBytes = numBytesWithSeparator(bytes, offset) - 4;
		return ByteTool.flipToAndFromComparableByteArray(bytes, offset + 4, numBytes);
	}
	
	@Override
	public byte[] fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		int length = bytes.length - byteOffset;
		return ByteTool.flipToAndFromComparableByteArray(bytes, byteOffset, length);
	}
	

	/*********************** SqlEncodedField ***********************/

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.LONGBLOB, Integer.MAX_VALUE , nullable, false);
	}
	
	@Override
	public String getValueString(){
		return ArrayTool.toCsvString(value);
	}
	
	@Override
	public int compareTo(Field<byte[]> other){
		return ByteTool.bitwiseCompare(this.value, other.getValue());
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, this.value==null?null:this.value);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] parseJdbcValueButDoNotSet(Object obj){
		throw new NotImplementedException("code needs testing");
//		if(obj==null){ return null; }
//		byte[] bytes = (byte[])obj;
//		return new LongArray(LongByteTool.fromUInt63Bytes(bytes));
	}
	
	@Override
	public byte[] fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getBytes(columnName);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public String getSqlEscaped(){
		throw new NotImplementedException();
	}

}
