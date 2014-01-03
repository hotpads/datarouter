package com.hotpads.datarouter.storage.field.imp.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class IntegerArrayField extends BaseListField<Integer, List<Integer>>{

	public IntegerArrayField(String name, List<Integer> value){
		super(name, value);
	}
	
	public IntegerArrayField(String prefix, String name, List<Integer> value){
		super(prefix, name, value);
	}
	
	
	/*********************** StringEncodedField ***********************/
	
	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		//TODO to CSV format?
		throw new NotImplementedException();
	}
	
	@Override
	public List<Integer> parseStringEncodedValueButDoNotSet(String s){
		throw new NotImplementedException();
	}
	

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		return IntegerByteTool.getIntegerByteArray(value);
	}

	@Override
	public List<Integer> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		return IntegerByteTool.fromIntegerByteArray(bytes, byteOffset);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		// TODO Auto-generated method stub
		return 0;
	}
	

	/*********************** SqlEncodedField ***********************/

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.LONGBLOB, Integer.MAX_VALUE , nullable, false);
	}

	@Override
	public List<Integer> parseJdbcValueButDoNotSet(Object col){
		throw new NotImplementedException("code needs testing");
//		if(obj==null){ return null; }
//		byte[] bytes = (byte[])obj;
//		return IntegerByteTool.fromComparableByteArray(bytes));
	}

	@Override
	public List<Integer> fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte[] bytes = rs.getBytes(columnName);
			if(ArrayTool.isEmpty(bytes)){ return ListTool.create(); }
			return IntegerByteTool.fromIntegerByteArray(bytes, 0);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, this.value==null?null:IntegerByteTool.getIntegerByteArray(this.value));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	
	/********************* main ************************/
	
	public static void main(String[] args){
		IntegerArrayField testField = new IntegerArrayField("stuff", ListTool.create(new Integer(-51), null, new Integer(2)));
		for(Integer num : testField.value){
			System.out.println(num);
		}
		byte[] bytes = testField.getBytes();
		List<Integer> integers = testField.fromBytesButDoNotSet(bytes, 0);
		for(Integer value : integers){
			System.out.println(value);
		}
	}
}
