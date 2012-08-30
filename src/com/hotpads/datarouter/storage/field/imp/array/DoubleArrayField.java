package com.hotpads.datarouter.storage.field.imp.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.DoubleByteTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;
import com.hotpads.util.core.exception.NotImplementedException;


public class DoubleArrayField extends BaseListField<Double,List<Double>>{

	public DoubleArrayField(String name, List<Double> value){
		super(name, value);
		// TODO Auto-generated constructor stub
	}
	
	public DoubleArrayField(String prefix, String name, List<Double> value){
		super(prefix, name, value);
	}

	@Override
	public byte[] getBytes(){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Double> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		List<Double> doubles = ListTool.create();
		for(byte b : bytes){
			doubles.add(DoubleByteTool.fromBytes(bytes, byteOffset));
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
	
}
