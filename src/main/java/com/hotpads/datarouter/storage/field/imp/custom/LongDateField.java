package com.hotpads.datarouter.storage.field.imp.custom;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.LongByteTool;

public class LongDateField extends BasePrimitiveField<Date>{

	public LongDateField(String name, Date value){
		super(name, value);
	}

	public LongDateField(String prefix, String name, Date value){
		super(prefix, name, value);
	}
	
	
	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.getTime()+"";
	}
	
	@Override
	public Date parseStringEncodedValueButDoNotSet(String s){
		if(StringTool.isEmpty(s) || s.equals("null")){ return null; }
//		return DateTool.parseUserInputDate(s,null);
		return new Date(Long.valueOf(s));
	}
	

	/*********************** ByteEncodedField ***********************/
	
	@Override
	public byte[] getBytes(){
		return value==null?null:LongByteTool.getUInt63Bytes(value.getTime());
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}
	
	@Override
	public Date fromBytesButDoNotSet(byte[] bytes, int offset){
		return new Date(LongByteTool.fromUInt63Bytes(bytes, offset));
	}
	

	/*********************** SqlEncodedField ***********************/

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.BIGINT, 20 , nullable, false);
	}
	
	@Override
	public String getValueString(){
		if(value==null){ return ""; }
		return value.toString();
	}

	@Override
	public Date parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:new Date(((BigInteger)obj).longValue());
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.BIGINT);
			}else{
				ps.setLong(parameterIndex, this.value.getTime());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Date fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			long value = rs.getLong(columnName);
			return rs.wasNull()?null:new Date(value);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
//	@Override
//	public void setFieldUsingReflection(FieldSet fieldSet, String fieldName, Long value){
//		try{
//			java.lang.reflect.Field fld = fieldSet.getClass().getField(fieldName);
//			fld.setAccessible(true);
//			fld.setLong(fieldSet, value);
//		}catch(Exception e){
//			throw new DataAccessException(e.getClass().getSimpleName()+" on "+fieldSet.getClass().getSimpleName()+"."+fieldName);
//		}
//	}

	@Override
	public String getSqlEscaped(){
		if(value==null){ return "null"; }
		return value.getTime()+"";
	}

}