package com.hotpads.datarouter.client.imp.jdbc.field.codec.primitive;

import java.sql.Types;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.imp.comparable.SignedByteField;

public class SignedByteJdbcFieldCodec extends BaseByteJdbcFieldCodec<SignedByteField>{

	public SignedByteJdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public SignedByteJdbcFieldCodec(SignedByteField field){
		super(field);
	}

	@Override
	protected Integer getMaxColumnLength(){
		return 1;
	}

	@Override
	protected MySqlColumnType getMysqlColumnType(){
		return MySqlColumnType.TINYINT;
	}

	@Override
	protected Integer getJavaSqlType(){
		return Types.TINYINT;
	}
}
