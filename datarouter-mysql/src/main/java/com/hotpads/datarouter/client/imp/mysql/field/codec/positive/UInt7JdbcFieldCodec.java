package com.hotpads.datarouter.client.imp.mysql.field.codec.positive;

import java.sql.Types;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.mysql.field.codec.primitive.BaseByteJdbcFieldCodec;
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

	@Override
	protected Integer getJavaSqlType(){
		return Types.TINYINT;
	}

}