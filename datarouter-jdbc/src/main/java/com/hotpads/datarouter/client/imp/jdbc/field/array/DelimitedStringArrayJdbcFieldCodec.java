package com.hotpads.datarouter.client.imp.jdbc.field.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BaseListJdbcFieldCodec;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;

public class DelimitedStringArrayJdbcFieldCodec
extends BaseListJdbcFieldCodec<String,List<String>,DelimitedStringArrayField>{
	
	public DelimitedStringArrayJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public DelimitedStringArrayJdbcFieldCodec(DelimitedStringArrayField field){
		super(field);
	}

	@Override
	public Class<DelimitedStringArrayField> getFieldType(){
		return DelimitedStringArrayField.class;
	}



	@Override
	public SqlColumn getSqlColumnDefinition(){
		return new SqlColumn(field.getColumnName(), MySqlColumnType.LONGBLOB, Integer.MAX_VALUE , field.getNullable(), 
				false);
	}

	@Override
	public List<String> parseJdbcValueButDoNotSet(Object col){
		if(col==null){ return null; }
		String dbValue = (String)col;
		return DelimitedStringArrayField.decode(dbValue, field.getSeparator());
	}

	@Override
	public List<String> fromJdbcResultSetButDoNotSet(ResultSet rs){
		try{
			String dbValue = rs.getString(field.getColumnName());
			return DelimitedStringArrayField.decode(dbValue, field.getSeparator());
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			ps.setString(parameterIndex, DelimitedStringArrayField.encode(field.getValue(), field.getSeparator()));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}
}
