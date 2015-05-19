package com.hotpads.datarouter.client.imp.jdbc.field.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.exception.NotImplementedException;

public class PrimitiveIntegerArrayJdbcFieldCodec
extends BaseJdbcFieldCodec<int[],Field<int[]>>{
	
	public PrimitiveIntegerArrayJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public PrimitiveIntegerArrayJdbcFieldCodec(Field<int[]> field){
		super(field);
	}
	

	@Override
	public SqlColumn getSqlColumnDefinition(){
		throw new NotImplementedException();
	}

	@Override
	public String getSqlEscaped(){
		throw new NotImplementedException();
	}

	@Override
	public int[] parseJdbcValueButDoNotSet(Object col){
		throw new NotImplementedException();
	}

	@Override
	public int[] fromJdbcResultSetButDoNotSet(ResultSet rs){
		throw new NotImplementedException();
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		throw new NotImplementedException();
	}

}
