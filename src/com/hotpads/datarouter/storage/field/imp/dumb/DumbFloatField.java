package com.hotpads.datarouter.storage.field.imp.dumb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.bytes.FloatByteTool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.bytes.DoubleByteTool;
/*
 * "dumb" because doesn't necessarily sort correctly in serialized form.  should prob copy
 * whatever they do in Orderly: https://github.com/zettaset/orderly
 */
public class DumbFloatField extends BasePrimitiveField<Float>{

	public DumbFloatField(String name, Float value){
		super(name, value);
	}

	public DumbFloatField(String prefix, String name, Float value){
		super(prefix, name, value);
	}
	
	@Override
	public void fromString(String s){
		this.value = s==null?null:Float.valueOf(s);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(name, MySqlColumnType.FLOAT, null, true);
	}
	
	@Override
	public Float parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Float)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.FLOAT);
			}else{
				ps.setFloat(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Float fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			float value = rs.getFloat(columnName);
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] getBytes(){
		return value==null?null:FloatByteTool.getBytes(value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}
	
	@Override
	public Float fromBytesButDoNotSet(byte[] bytes, int offset){
		return FloatByteTool.fromBytes(bytes, offset);
	}
}
