package com.hotpads.datarouter.util;

import com.hotpads.datarouter.serialize.fielder.PrimaryKeyFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.java.ReflectionTool;

public class PrimaryKeyPercentCodec{
	public static <PK extends PrimaryKey<PK>> String encode(PK pk){
		return PercentFieldCodec.encode(pk.getFields());
	}

	public static <PK extends PrimaryKey<PK>> PK decode(Class<PK> pkClass, PrimaryKeyFielder<PK> fielder,
			String pkEncoded){
		if(pkEncoded == null){ return null; }
		PK pk = ReflectionTool.create(pkClass);
		String[] tokens = PercentFieldCodec.deCode(pkEncoded);
		int i = 0;
		for(Field<?> field : fielder.getFields(pk)){
			if(i > tokens.length - 1){
				break;
			}
			field.fromString(tokens[i]);
			field.setUsingReflection(pk, field.getValue());
			field.setValue(null);// to be safe until Field logic is cleaned up
			++i;
		}
		return pk;
	}
}
