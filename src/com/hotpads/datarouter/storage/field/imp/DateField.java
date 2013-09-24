package com.hotpads.datarouter.storage.field.imp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.bytes.LongByteTool;

public class DateField extends BasePrimitiveField<Date>{

	public DateField(String name, Date value){
		super(name, value);
	}

	public DateField(String prefix, String name, Date value){
		super(prefix, name, value);
	}
	
	@Override
	public void fromString(String s){
		this.value = s==null?null:DateTool.parseUserInputDate(s,null);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.DATETIME, 19, nullable);
	}
	
	@Override
	public Date parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Date)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.DATE);
			}else{
				ps.setTimestamp(parameterIndex, new Timestamp(this.value.getTime())); //sql timestamp is MySQL's datetime
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Date fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			Timestamp timeStamp = rs.getTimestamp(columnName);
			if(rs.wasNull()){
				return null;
			}
			Date timeStampDate = new Date(timeStamp.getTime());
			return timeStampDate;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public byte[] getBytes(){
		if(value==null){ return null; }
		return LongByteTool.getUInt63Bytes(value.getTime());
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}
	
	@Override
	public Date fromBytesButDoNotSet(byte[] bytes, int offset){
		return new Date(LongByteTool.fromUInt63Bytes(bytes, offset));
	}
	
	@Override
	public String getSqlEscaped(){
		return "'" + getSqlDateString(value)+ "'";
	}
	
	public static String getSqlDateString(Date date){
		return new Timestamp(date.getTime()).toString();
	}
	
	/** tests ****************************************************************/
	public static class Tests {
		@Test public void testGetSqlEscaped() throws Exception{
			//mysql date format is yyyy-MM-dd HH:mm:ss http://dev.mysql.com/doc/refman/5.1/en/datetime.html
			//jdbc timestamp escape format" yyyy-MM-dd HH:mm:ss.n where n is nanoseconds (not representable with Date)
			// sql insert with a string including the nanosecond value works in mysql 
			String dateString = "2002-11-05 13:14:01";
			Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
			DateField testField = new DateField("test",date);
			Assert.assertEquals("'"+dateString+".0'", testField.getSqlEscaped());
		}
	}
}
