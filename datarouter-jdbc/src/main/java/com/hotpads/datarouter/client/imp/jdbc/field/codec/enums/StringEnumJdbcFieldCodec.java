package com.hotpads.datarouter.client.imp.jdbc.field.codec.enums;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.field.StringJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.base.BaseJdbcFieldCodec;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;

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
	public SqlColumn getSqlColumnDefinition(){
		return stringJdbcFieldCodec.getSqlColumnDefinition();
	}

	@Override
	public String getSqlEscaped(){
		return stringJdbcFieldCodec.getSqlEscaped();
	}

	@Override
	public E parseJdbcValueButDoNotSet(Object obj){
		return field.getSampleValue().fromPersistentString(stringJdbcFieldCodec.parseJdbcValueButDoNotSet(obj));
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		stringJdbcFieldCodec.setPreparedStatementValue(ps, parameterIndex);
	}

	@Override
	public E fromJdbcResultSetButDoNotSet(ResultSet rs){
		String string = stringJdbcFieldCodec.fromJdbcResultSetButDoNotSet(rs);
		return string == null ? null : field.getSampleValue().fromPersistentString(string);
	}

}
