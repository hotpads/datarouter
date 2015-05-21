package com.hotpads.datarouter.client.imp.jdbc.field.codec.base;

import com.hotpads.datarouter.storage.field.Field;

public abstract class BasePrimitiveJdbcFieldCodec<T extends Comparable<T>,F extends Field<T>> 
extends BaseJdbcFieldCodec<T,F>{

	public BasePrimitiveJdbcFieldCodec(F field){
		super(field);
	}

	@Override
	public String getSqlEscaped(){
		if(field.getValue()==null){
			return "null";
		}
		return field.getValue().toString();
	}
}
