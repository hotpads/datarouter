package com.hotpads.datarouter.client.imp.mysql.field.codec.enums;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.mysql.field.codec.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.storage.field.imp.enums.VarIntEnumField;
import com.hotpads.util.core.enums.IntegerEnum;

public class VarIntEnumJdbcFieldCodec<E extends IntegerEnum<E>>
extends BaseJdbcFieldCodec<E,VarIntEnumField<E>>{

	private IntegerEnumJdbcFieldCodec<E> integerEnumJdbcFieldCodec;

	public VarIntEnumJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public VarIntEnumJdbcFieldCodec(VarIntEnumField<E> field){
		super(field);
		this.integerEnumJdbcFieldCodec = new IntegerEnumJdbcFieldCodec<>(VarIntEnumField.toIntegerEnumField(field));
	}

	/*********************** SqlEncodedField ***********************/

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return integerEnumJdbcFieldCodec.getSqlColumnDefinition(allowNullable);
	}

	@Override
	public String getSqlEscaped(){
		return integerEnumJdbcFieldCodec.getSqlEscaped();
	}

	@Override
	public E parseJdbcValueButDoNotSet(Object obj){
		return integerEnumJdbcFieldCodec.parseJdbcValueButDoNotSet(obj);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		integerEnumJdbcFieldCodec.setPreparedStatementValue(ps, parameterIndex);
	}

	@Override
	public E fromJdbcResultSetButDoNotSet(ResultSet rs){
		return integerEnumJdbcFieldCodec.fromJdbcResultSetButDoNotSet(rs);
	}

}
