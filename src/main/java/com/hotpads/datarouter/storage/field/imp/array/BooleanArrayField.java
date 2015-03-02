package com.hotpads.datarouter.storage.field.imp.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.BooleanByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class BooleanArrayField extends BaseListField<Boolean, List<Boolean>>{

	public BooleanArrayField(String name, List<Boolean> value){
		super(name, value);
	}
	
	public BooleanArrayField(String prefix, String name, List<Boolean> value){
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
	public List<Boolean> parseStringEncodedValueButDoNotSet(String s){
		throw new NotImplementedException();
	}
	

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		return BooleanByteTool.getBooleanByteArray(value);
	}

	@Override
	public List<Boolean> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		return BooleanByteTool.fromBooleanByteArray(bytes, byteOffset);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		// TODO Auto-generated method stub
		return 0;
	}
	

	/*********************** SqlEncodedField ***********************/

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.LONGBLOB, Integer.MAX_VALUE, nullable, false);
	}

	@Override
	public List<Boolean> parseJdbcValueButDoNotSet(Object col){
		throw new NotImplementedException("code needs testing");
//		if(obj==null){ return null; }
//		byte[] bytes = (byte[])obj;
//		return BooleanByteTool.fromBooleanByteArray(bytes));
	}

	@Override
	public List<Boolean> fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte[] bytes = rs.getBytes(columnName);
			if(DrArrayTool.isEmpty(bytes)){ return DrListTool.create(); }
			return BooleanByteTool.fromBooleanByteArray(bytes, 0);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, this.value==null?null:BooleanByteTool.getBooleanByteArray(this.value));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	
	/************************** main ***********************/
	
	public static void main(String[] args){
		BooleanArrayField testField = new BooleanArrayField("stuff", DrListTool.create(new Boolean(true), null, new Boolean(false)));
		for(Boolean num : testField.value){
			System.out.println(num);
		}
		byte[] bytes = testField.getBytes();
		List<Boolean> bools = testField.fromBytesButDoNotSet(bytes, 0);
		for(Boolean bool : bools){
			System.out.println(bool);
		}
	}
}
