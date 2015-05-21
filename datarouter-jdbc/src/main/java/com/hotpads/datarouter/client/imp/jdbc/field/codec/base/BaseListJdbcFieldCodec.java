package com.hotpads.datarouter.client.imp.jdbc.field.codec.base;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;

public abstract class BaseListJdbcFieldCodec<T extends Comparable<T>,L extends List<T>,F extends Field<L>> 
extends BaseJdbcFieldCodec<L,F>{

	public BaseListJdbcFieldCodec(F field){
		super(field);
	}

	@Override
	public String getSqlEscaped(){
		if(field.getValue() == null){
			return "null";
		}
		return field.getValue().toString();
	}
}
