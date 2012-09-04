package com.hotpads.datarouter.storage.field.imp.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.util.core.exception.NotImplementedException;

public class BooleanArrayField extends BaseListField<Boolean, List<Boolean>>{

	public BooleanArrayField(String name, List<Boolean> value){
		super(name, value);
	}
	
	public BooleanArrayField(String prefix, String name, List<Boolean> value){
		super(prefix, name, value);
	}

	@Override
	public byte[] getBytes(){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Boolean> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Boolean> parseJdbcValueButDoNotSet(Object col){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Boolean> fromJdbcResultSetButDoNotSet(ResultSet rs){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		// TODO Auto-generated method stub
		
	}

}
