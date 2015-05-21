package com.hotpads.datarouter.client.imp.jdbc.field.codec.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BaseListJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.array.BooleanArrayField;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.util.core.bytes.BooleanByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class BooleanArrayJdbcFieldCodec
extends BaseListJdbcFieldCodec<Boolean,List<Boolean>,Field<List<Boolean>>>{
	
	public BooleanArrayJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public BooleanArrayJdbcFieldCodec(BooleanArrayField field){
		super(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.LONGBLOB, Integer.MAX_VALUE, field.getNullable(), 
				false);
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
			byte[] bytes = rs.getBytes(field.getColumnName());
			if(DrArrayTool.isEmpty(bytes)){ return new ArrayList<>(); }
			return BooleanByteTool.fromBooleanByteArray(bytes, 0);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, field.getValue() == null ? null : BooleanByteTool.getBooleanByteArray(field
					.getValue()));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
