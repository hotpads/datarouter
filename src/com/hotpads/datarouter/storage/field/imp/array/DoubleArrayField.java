package com.hotpads.datarouter.storage.field.imp.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.DoubleByteTool;
import com.hotpads.util.core.exception.NotImplementedException;


public class DoubleArrayField extends BaseListField<Double,List<Double>>{

	public DoubleArrayField(String name, List<Double> value){
		super(name, value);
	}
	
	public DoubleArrayField(String prefix, String name, List<Double> value){
		super(prefix, name, value);
	}

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		byte[] bytes = new byte[value.size() * 8];
		for(int i = 0; i < value.size(); i++){
			System.arraycopy(DoubleByteTool.getBytes(value.get(i)), 0, bytes, i * 8, 8);
		}
		return bytes;
	}

	@Override
	public List<Double> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		List<Double> doubles = ListTool.create();
		int numBytes = bytes.length - byteOffset;
		int numDoubles = (bytes.length - byteOffset)/8;
		byte[] arrayToCopy = new byte[8];
		for(int i = 0; i < numDoubles; i++){
			System.arraycopy(bytes, i * 8, arrayToCopy, 0, 8);
			doubles.add(DoubleByteTool.fromBytes(arrayToCopy, byteOffset));
		}
		return doubles;
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void fromString(String s){
		throw new NotImplementedException();
	}

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.LONGBLOB, 2147483647 , true);
	}

	@Override
	public List<Double> parseJdbcValueButDoNotSet(Object col){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Double> fromJdbcResultSetButDoNotSet(ResultSet rs){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args){
		new DoubleArrayField("stuff", ListTool.create(new Double(5.00001), new Double(203920.555))).getBytes();
	}
}
