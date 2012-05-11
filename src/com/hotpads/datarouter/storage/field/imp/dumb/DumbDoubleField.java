package com.hotpads.datarouter.storage.field.imp.dumb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.bytes.DoubleByteTool;
/*
 * "dumb" because doesn't necessarily sort correctly in serialized form.  should prob copy
 * whatever they do in Orderly: https://github.com/zettaset/orderly
 */
public class DumbDoubleField extends BasePrimitiveField<Double>{

	public DumbDoubleField(String name, Double value){
		super(name, value);
	}

	public DumbDoubleField(String prefix, String name, Double value){
		super(prefix, name, value);
	}
	
	@Override
	public void fromString(String s){
		this.value = s==null?null:Double.valueOf(s);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(name, MySqlColumnType.DOUBLE, null, true);
	}
	
	@Override
	public Double parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Double)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.DOUBLE);
			}else{
				ps.setDouble(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Double fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			double value = rs.getDouble(columnName);
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] getBytes(){
		return value==null?null:DoubleByteTool.getBytes(value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}
	
	@Override
	public Double fromBytesButDoNotSet(byte[] bytes, int offset){
		return DoubleByteTool.fromBytes(bytes, offset);
	}
	

}
