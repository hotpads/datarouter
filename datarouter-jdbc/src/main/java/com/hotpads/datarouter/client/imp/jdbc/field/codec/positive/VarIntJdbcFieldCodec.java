package com.hotpads.datarouter.client.imp.jdbc.field.codec.positive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BasePrimitiveJdbcFieldCodec;
import com.hotpads.datarouter.storage.field.imp.positive.VarIntField;

public class VarIntJdbcFieldCodec
extends BasePrimitiveJdbcFieldCodec<Integer,VarIntField>{

	private UInt31JdbcFieldCodec uint31JdbcFieldCodec;

	public VarIntJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public VarIntJdbcFieldCodec(VarIntField field){
		super(field);
		this.uint31JdbcFieldCodec = new UInt31JdbcFieldCodec(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(){
		return uint31JdbcFieldCodec.getSqlColumnDefinition();
	}

	@Override
	public Integer parseJdbcValueButDoNotSet(Object obj){
		return VarIntField.assertInRange(uint31JdbcFieldCodec.parseJdbcValueButDoNotSet(obj));
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		uint31JdbcFieldCodec.setPreparedStatementValue(ps, parameterIndex);
	}

	@Override
	public Integer fromJdbcResultSetButDoNotSet(ResultSet rs){
		Integer value = uint31JdbcFieldCodec.fromJdbcResultSetButDoNotSet(rs);
		return VarIntField.assertInRange(value);
	}
}
