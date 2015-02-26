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
import com.hotpads.util.core.bytes.DoubleByteTool;
import com.hotpads.util.core.exception.NotImplementedException;


public class DoubleArrayField extends BaseListField<Double,List<Double>>{

	public DoubleArrayField(String name, List<Double> value){
		super(name, value);
	}
	
	public DoubleArrayField(String prefix, String name, List<Double> value){
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
	public List<Double> parseStringEncodedValueButDoNotSet(String s){
		throw new NotImplementedException();
	}
	

	/*********************** ByteEncodedField ***********************/
	
	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		return DoubleByteTool.getDoubleByteArray(value);
	}

	@Override
	public List<Double> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		return DoubleByteTool.fromDoubleByteArray(bytes, byteOffset);
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
	public List<Double> parseJdbcValueButDoNotSet(Object col){
		throw new NotImplementedException("code needs testing");
//		if(obj==null){ return null; }
//		byte[] bytes = (byte[])obj;
//		return DoubleByteTool.fromDoubleBytes(bytes);
	}
	
	@Override
	public List<Double> fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte[] bytes = rs.getBytes(columnName);
			if(DrArrayTool.isEmpty(bytes)){ return DrListTool.create(); }
			return DoubleByteTool.fromDoubleByteArray(bytes, 0);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, this.value==null?null:DoubleByteTool.getDoubleByteArray(this.value));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	public static void main(String[] args){
		DoubleArrayField testField = new DoubleArrayField("stuff", DrListTool.create(new Double(-5.00001), new Double(203920.555),  null));
		for(Double num : testField.value){
			System.out.println(num);
		}
		byte[] bytes = testField.getBytes();
		List<Double> doubles = testField.fromBytesButDoNotSet(bytes, 0);
		for(Double doub : doubles){
			System.out.println(doub);
		}
	}
}
