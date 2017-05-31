package com.hotpads.datarouter.client.imp.mysql.field.codec.enums;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.mysql.field.StringJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.mysql.field.codec.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.util.core.enums.StringEnum;

public class StringEnumJdbcFieldCodec<E extends StringEnum<E>>
extends BaseJdbcFieldCodec<E,StringEnumField<E>>{

	private StringJdbcFieldCodec stringJdbcFieldCodec;

	public StringEnumJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public StringEnumJdbcFieldCodec(StringEnumField<E> field){
		super(field);
		stringJdbcFieldCodec = new StringJdbcFieldCodec(StringEnumField.toStringField(field));
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return stringJdbcFieldCodec.getSqlColumnDefinition(allowNullable);
	}

	@Override
	public String getSqlEscaped(){
		return stringJdbcFieldCodec.getSqlEscaped();
	}

	@Override
	public E parseJdbcValueButDoNotSet(Object obj){
		String persistentString = stringJdbcFieldCodec.parseJdbcValueButDoNotSet(obj);
		return StringEnum.fromPersistentStringSafe(field.getSampleValue(), persistentString);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		stringJdbcFieldCodec.setPreparedStatementValue(ps, parameterIndex);
	}

	@Override
	public E fromJdbcResultSetButDoNotSet(ResultSet rs){
		String string = stringJdbcFieldCodec.fromJdbcResultSetButDoNotSet(rs);
		return StringEnum.fromPersistentStringSafe(field.getSampleValue(), string);
	}

}
