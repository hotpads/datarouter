package com.hotpads.datarouter.storage.field.imp.enums;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.java.ReflectionTool;

public class IntegerEnumField<E extends IntegerEnum<E>> extends BaseField<E>{
	
	protected E sampleValue;

	public IntegerEnumField(Class<E> enumClass, String name, E value){
		this(enumClass, null, name, value);
	}

	public IntegerEnumField(Class<E> enumClass, String prefix, String name, E value){
		super(prefix, name, value);
		this.sampleValue = ReflectionTool.create(enumClass);
	}
	
	@Override
	public E parseStringValueButDoNotSet(String s){
		if(StringTool.isEmpty(s)){ return null; }
		return sampleValue.fromPersistentInteger(Integer.valueOf(s));
	}
	
	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.INT, 11, nullable, false);
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
		return value==null?null:IntegerByteTool.getComparableBytes(
				value.getPersistentInteger());
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}
	
	@Override
	public E fromBytesButDoNotSet(byte[] bytes, int offset){
		return sampleValue.fromPersistentInteger(
				IntegerByteTool.fromComparableBytes(bytes, offset));
	}
	
	
}
