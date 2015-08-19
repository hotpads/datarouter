package com.hotpads.datarouter.client.imp.jdbc.field.codec.positive;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.primitive.BaseByteJdbcFieldCodec;
import com.hotpads.datarouter.storage.field.imp.positive.UInt7Field;

public class UInt7JdbcFieldCodec extends BaseByteJdbcFieldCodec<UInt7Field>{

	public UInt7JdbcFieldCodec(){//no-arg for reflection
		this(null);
	}

	public UInt7JdbcFieldCodec(UInt7Field field){
		super(field);
	}

	@Override
	protected Integer getMaxColumnLength(){
		return 3;
	}

	@Override
	protected MySqlColumnType getMysqlColumnType(){
		return MySqlColumnType.TINYINT;
	}

}
