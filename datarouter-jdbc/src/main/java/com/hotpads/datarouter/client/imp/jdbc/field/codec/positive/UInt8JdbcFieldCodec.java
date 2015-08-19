package com.hotpads.datarouter.client.imp.jdbc.field.codec.positive;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.primitive.BaseByteJdbcFieldCodec;
import com.hotpads.datarouter.storage.field.imp.positive.UInt8Field;

public class UInt8JdbcFieldCodec extends BaseByteJdbcFieldCodec<UInt8Field>{

	public UInt8JdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public UInt8JdbcFieldCodec(UInt8Field field){
		super(field);
	}

	@Override
	protected Integer getMaxColumnLength(){
		return 5;
	}

	@Override
	protected MySqlColumnType getMysqlColumnType(){
		return MySqlColumnType.SMALLINT;
	}

}
