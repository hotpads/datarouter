package com.hotpads.datarouter.storage.field.imp.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;
import com.hotpads.util.core.exception.NotImplementedException;

public class UInt63ArrayField extends BaseListField<Long,List<Long>>{

	public UInt63ArrayField(String name, List<Long> value){
		super(name, value);
	}

	public UInt63ArrayField(String prefix, String name, List<Long> value){
		super(prefix, name, value);
	}
	
	@Override
	public void fromString(String s){
		throw new NotImplementedException();
	}
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.LONGBLOB, null , true);
	}
	
	@Override
	public int compareTo(Field<List<Long>> other){
		return ListTool.compare(this.value, other.getValue());
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, this.value==null?null:LongByteTool.getUInt63ByteArray(this.value));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public List<Long> parseJdbcValueButDoNotSet(Object obj){
		throw new NotImplementedException("code needs testing");
//		if(obj==null){ return null; }
//		byte[] bytes = (byte[])obj;
//		return new LongArray(LongByteTool.fromUInt63Bytes(bytes));
	}
	
	@Override
	public List<Long> fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte[] bytes = rs.getBytes(columnName);
			return new LongArray(LongByteTool.fromUInt63ByteArray(bytes));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public String getSqlEscaped(){
		throw new NotImplementedException("and probably never will be");
	};
	

	@Override
	public byte[] getBytes(){
		return value==null?null:LongByteTool.getUInt63ByteArray(value);
	}
	
	@Override
	public List<Long> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = ArrayTool.length(bytes) - byteOffset;
		return new LongArray(LongByteTool.fromUInt63ByteArray(bytes, byteOffset, numBytes));
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		return bytes==null?0:(IntegerByteTool.fromUInt31Bytes(bytes, byteOffset) + 4);
	}
	
	@Override
	public byte[] getBytesWithSeparator(){
		if(value==null){ return IntegerByteTool.getUInt31Bytes(0); }
		//prepend the length (in bytes) as a positive integer (not bitwise comparable =( )
		//TODO replace with varint
		byte[] dataBytes = LongByteTool.getUInt63ByteArray(value);
		byte[] allBytes = new byte[4+dataBytes.length];
		System.arraycopy(IntegerByteTool.getUInt31Bytes(dataBytes.length), 0, allBytes, 0, 4);
		System.arraycopy(dataBytes, 0, allBytes, 4, dataBytes.length);
		return allBytes;
	}
	
	@Override
	public List<Long> fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = numBytesWithSeparator(bytes, byteOffset) - 4;
		return new LongArray(LongByteTool.fromUInt63ByteArray(bytes, byteOffset + 4, numBytes));
	}
	
	
	public static class UInt63ArrayFieldTests{
		@Test public void testByteAware(){
			LongArray a1 = new LongArray();
			a1.add(Long.MAX_VALUE);
			a1.add((long)Integer.MAX_VALUE);
			a1.add((long)Short.MAX_VALUE);
			a1.add((long)Byte.MAX_VALUE);
			a1.add((long)5);
			a1.add((long)0);
			UInt63ArrayField field = new UInt63ArrayField("", a1);
			byte[] bytesNoPrefix = field.getBytes();
			Assert.assertEquals(a1.size()*8, ArrayTool.length(bytesNoPrefix));
			List<Long> a2 = new UInt63ArrayField("", null).fromBytesButDoNotSet(bytesNoPrefix, 0);
			Assert.assertTrue(CollectionTool.equalsAllElementsInIteratorOrder(a1, a2));
			
			byte[] bytesWithPrefix = field.getBytesWithSeparator();
			Assert.assertEquals(a1.size()*8, bytesWithPrefix[3]);
			Assert.assertEquals(a1.size()*8 + 4, field.numBytesWithSeparator(bytesWithPrefix, 0));

			List<Long> a3 = new UInt63ArrayField("", null).fromBytesWithSeparatorButDoNotSet(bytesWithPrefix, 0);
			Assert.assertTrue(CollectionTool.equalsAllElementsInIteratorOrder(a1, a3));
			
		}
	}
}
