package com.hotpads.datarouter.storage.field.imp.enums;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.number.VarInt;

public class VarIntEnumField<E extends IntegerEnum<E>> extends BaseField<E>{
	
	protected E sampleValue;

	public VarIntEnumField(Class<E> enumClass, String name, E value){
		this(enumClass, null, name, value);
	}

	public VarIntEnumField(Class<E> enumClass, String prefix, String name, E value){
		super(prefix, name, value);
		this.sampleValue = ReflectionTool.create(enumClass);
	}
	
	@Override
	public void fromString(String s){
		this.value = s==null?null:sampleValue.fromPersistentInteger(Integer.valueOf(s));
	}
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.INT, 11 , true);
	}
	
	@Override
	public int compareTo(Field<E> other){
		return DataRouterEnumTool.compareIntegerEnums(value, other.getValue());
	}
	
	@Override
	public String getValueString(){
		if(value==null){ return ""; }//hmm - should this just return null?
		return value.getPersistentInteger()+"";
	}

	@Override
	public String getSqlEscaped(){
		if(value==null){ return "null"; }
		return value.getPersistentInteger().toString();
	}

	@Override
	public E parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:sampleValue.fromPersistentInteger((Integer)obj);
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.INTEGER);
			}else{
				ps.setInt(parameterIndex, value.getPersistentInteger());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public E fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			int rsValue = rs.getInt(columnName);
			return rs.wasNull()?null:sampleValue.fromPersistentInteger(rsValue);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] getBytes(){
		return value==null?null:new VarInt(value.getPersistentInteger()).getBytes();
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return new VarInt(bytes, offset).getNumBytes();
	}
	
	@Override
	public E fromBytesButDoNotSet(byte[] bytes, int offset){
		Integer i = new VarInt(bytes, offset).getValue();
		return i==null?null:sampleValue.fromPersistentInteger(i);
	}
	
	
}
