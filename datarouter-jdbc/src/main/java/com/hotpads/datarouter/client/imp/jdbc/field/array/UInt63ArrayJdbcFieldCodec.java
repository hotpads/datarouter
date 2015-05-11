package com.hotpads.datarouter.client.imp.jdbc.field.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BaseListJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.array.UInt63ArrayField;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;
import com.hotpads.util.core.exception.NotImplementedException;

public class UInt63ArrayJdbcFieldCodec
extends BaseListJdbcFieldCodec<Long,List<Long>,UInt63ArrayField>{
	
	public UInt63ArrayJdbcFieldCodec(UInt63ArrayField field){
		super(field);
	}

	@Override
	public Class<UInt63ArrayField> getFieldType(){
		return UInt63ArrayField.class;
	}

	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.LONGBLOB, Integer.MAX_VALUE, field.getNullable(), 
				false);
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setBytes(parameterIndex, field.getValue()==null?null:LongByteTool.getUInt63ByteArray(field.getValue()));
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
			byte[] bytes = rs.getBytes(field.getColumnName());
			return new LongArray(LongByteTool.fromUInt63ByteArray(bytes));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public String getSqlEscaped(){
		throw new NotImplementedException("and probably never will be");
	}
}
