package com.hotpads.datarouter.storage.field.imp.comparable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.bytes.IntegerByteTool;

public class IntegerField extends BasePrimitiveField<Integer>{

	public IntegerField(String name, Integer value){
		super(name, value);
	}

	public IntegerField(String prefix, String name, Integer value){
		super(prefix, name, value);
	}
	
	@Override
	public void fromString(String s){
		this.value = s==null?null:Integer.valueOf(s);
	}
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.INT, 11, true);
	}

	@Override
	public Integer parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Integer)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.INTEGER);
			}else{
				ps.setInt(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Integer fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			int value = rs.getInt(columnName);
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] getBytes(){
		return value==null?null:IntegerByteTool.getComparableBytes(value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}
	
	@Override
	public Integer fromBytesButDoNotSet(byte[] bytes, int offset){
		return IntegerByteTool.fromComparableBytes(bytes, offset);
	}
	
	
}
