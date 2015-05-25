package com.hotpads.datarouter.client.imp.jdbc.field.codec.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.codec.binary.Hex;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayField;
import com.hotpads.util.core.exception.NotImplementedException;

public class ByteArrayJdbcFieldCodec
extends BaseJdbcFieldCodec<byte[],ByteArrayField>{
	
	public ByteArrayJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public ByteArrayJdbcFieldCodec(ByteArrayField field){
		super(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(){
		if(field.getSize() <= MySqlColumnType.MAX_LENGTH_VARBINARY){
			return new SqlColumn(field.getKey().getColumnName(), MySqlColumnType.VARBINARY, field.getSize(),
					field.getKey().isNullable(), false);
		}else if(field.getSize() <= MySqlColumnType.MAX_LENGTH_LONGBLOB){ 
			return new SqlColumn(field.getKey().getColumnName(), MySqlColumnType.LONGBLOB, Integer.MAX_VALUE, field
					.getKey().isNullable(), false); }
		throw new IllegalArgumentException("Unknown size:" + field.getSize());
	}
	
	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{			
			ps.setBytes(parameterIndex, field.getValue()==null?null:field.getValue());			
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public byte[] parseJdbcValueButDoNotSet(Object obj){		
		throw new NotImplementedException("code needs testing");
//		if(obj==null){ return null; }
//		byte[] bytes = (byte[])obj;
//		return new LongArray(LongByteTool.fromUInt63Bytes(bytes));
	}
	
	@Override
	public byte[] fromJdbcResultSetButDoNotSet(ResultSet rs){	
		try{
			return rs.getBytes(field.getKey().getColumnName());
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
	
	@Override
	public String getSqlEscaped(){	
		//This method is called by the 'alreadyExists' condition inside jdbcPutOp.java, when ByteArrayField belongs to a PK.
		// Adding Ox prefix is equivalent to wrapping the string with quotes, both works. ["'"+Hex.encodeHexString(value)+"'"]		
		return "0x"+Hex.encodeHexString(field.getValue());		
	}
}
