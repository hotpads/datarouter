package com.hotpads.datarouter.storage.field.imp.comparable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.util.core.ByteTool;

//recognizes -128 to -1 using two's complement.  therefore max value is 127
public class SignedByteField extends BasePrimitiveField<Byte>{

	public SignedByteField(String name, Byte value){
		super(name, value);
	}

	public SignedByteField(String prefix, String name, Byte value){
		super(prefix, name, value);
	}
	
	@Override
	public void fromString(String s){
		this.value = s==null?null:Byte.valueOf(s);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(columnName, MySqlColumnType.TINYINT, 1 , true);
	}
	
	@Override
	public Byte parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Byte)obj;
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(value==null){
				ps.setNull(parameterIndex, Types.TINYINT);
			}else{
				ps.setByte(parameterIndex, this.value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public Byte fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			byte value = rs.getByte(columnName);
			return rs.wasNull()?null:value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	//recognizes -128 to -1 using two's complement.  therefore max value is 127
	@Override
	public byte[] getBytes(){
		return value==null?null:ByteTool.getComparableBytes(value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 1;
	}
	
	@Override
	public Byte fromBytesButDoNotSet(byte[] bytes, int offset){
		return ByteTool.getComparableByte(bytes[offset]);
	}


}