package com.hotpads.datarouter.client.imp.jdbc.field.base;

import com.hotpads.datarouter.storage.field.Field;

public abstract class BaseListJdbcFieldCodec<T extends Comparable<T>,F extends Field<T>> 
extends BaseJdbcFieldCodec<T,F>{

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
