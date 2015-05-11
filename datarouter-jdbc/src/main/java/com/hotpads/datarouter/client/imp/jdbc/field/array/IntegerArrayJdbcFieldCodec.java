package com.hotpads.datarouter.client.imp.jdbc.field.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BaseListJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.array.IntegerArrayField;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class IntegerArrayJdbcFieldCodec
extends BaseListJdbcFieldCodec<Integer,List<Integer>,IntegerArrayField>{
	
	public IntegerArrayJdbcFieldCodec(IntegerArrayField field){
		super(field);
	}

	@Override
	public Class<IntegerArrayField> getFieldType(){
		return IntegerArrayField.class;
	}



	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.LONGBLOB, Integer.MAX_VALUE , field.getNullable(), 
				false);
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
			byte[] bytes = rs.getBytes(field.getColumnName());
			if(DrArrayTool.isEmpty(bytes)){ return new ArrayList<>(); }
			return IntegerByteTool.fromIntegerByteArray(bytes, 0);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, field.getValue()==null?null:IntegerByteTool.getIntegerByteArray(field.getValue()));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

}
