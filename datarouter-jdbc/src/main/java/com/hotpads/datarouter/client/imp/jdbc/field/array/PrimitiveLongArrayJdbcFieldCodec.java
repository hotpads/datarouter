package com.hotpads.datarouter.client.imp.jdbc.field.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.storage.field.imp.array.PrimitiveLongArrayField;
import com.hotpads.util.core.exception.NotImplementedException;

public class PrimitiveLongArrayJdbcFieldCodec
extends BaseJdbcFieldCodec<long[],PrimitiveLongArrayField>{
	
	public PrimitiveLongArrayJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}
	
	public PrimitiveLongArrayJdbcFieldCodec(PrimitiveLongArrayField field){
		super(field);
	}

//	@Override
//	public Class<PrimitiveLongArrayField> getFieldType(){
//		return PrimitiveLongArrayField.class;
//	}
	
	

	@Override
	public SqlColumn getSqlColumnDefinition(){
		throw new NotImplementedException();
	}

	@Override
	public String getSqlEscaped(){
		throw new NotImplementedException();
	}

	@Override
	public long[] parseJdbcValueButDoNotSet(Object col){
		throw new NotImplementedException();
	}

	@Override
	public long[] fromJdbcResultSetButDoNotSet(ResultSet rs){
		throw new NotImplementedException();
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		throw new NotImplementedException();
	}

}
