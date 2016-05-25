package com.hotpads.datarouter.util;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.serialize.fielder.PrimaryKeyFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.util.core.java.ReflectionTool;

public class PrimaryKeyPercentCodec{

	public static <PK extends PrimaryKey<PK>> String encode(PK pk){
		return PercentFieldCodec.encode(pk.getFields());
	}

	public static <PK extends PrimaryKey<PK>> PK decode(Class<PK> pkClass, String pkEncoded){
		if(pkEncoded == null){
			return null;
		}
		PK pk = ReflectionTool.create(pkClass);
		String[] tokens = PercentFieldCodec.decode(pkEncoded);
		int index = 0;
		for(Field<?> field : pk.getFields(pk)){
			if(index > tokens.length - 1){
				break;
			}
			field.fromString(tokens[index]);
			field.setUsingReflection(pk, field.getValue());
			field.setValue(null);// to be safe until Field logic is cleaned up
			++index;
		}
		return pk;
	}

	public static <PK extends PrimaryKey<PK>> PK parseKeyFromKeyFieldMap(Class<PK> pkClass,
			PrimaryKeyFielder<PK> fielder, Map<String,String> keyFieldMap){
		if(keyFieldMap == null || keyFieldMap.isEmpty()){
			return null;
		}
		PK pk = ReflectionTool.create(pkClass);
		for(Field<?> field : fielder.getFields(pk)){
			field.fromString(keyFieldMap.get(field.getPrefixedName()));
			field.setUsingReflection(pk, field.getValue());
			field.setValue(null);
		}
		return pk;
	}


	public static class PrimaryKeyPercentCodecTests{
		@Test
		public void testSimpleNumericPk(){
			Long id = 355L;
			TraceKey pk = new TraceKey(id);
			String encoded = encode(pk);
			TraceKey decoded = decode(TraceKey.class, encoded);
			Assert.assertEquals(decoded.getId(), id);
		}
	}
}
