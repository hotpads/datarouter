package com.hotpads.datarouter.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.serialize.fielder.PrimaryKeyFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.stream.StreamTool;

public class PrimaryKeyPercentCodec{

	/*-------------- encode --------------------*/

	public static <PK extends PrimaryKey<PK>> String encode(PK pk){
		return PercentFieldCodec.encode(pk.getFields());
	}

	public static <PK extends PrimaryKey<PK>> String encodeMulti(Iterable<PK> pks, String delimiter){
		//TODO validate delimiter
		return StreamTool.stream(pks)
				.map(PrimaryKeyPercentCodec::encode)
				.collect(Collectors.joining(delimiter));
	}

	/*-------------- decode --------------------*/

	public static <PK extends PrimaryKey<PK>> PK decode(Class<PK> pkClass, String encodedPk){
		if(encodedPk == null){
			return null;
		}
		PK pk = ReflectionTool.create(pkClass);
		String[] tokens = PercentFieldCodec.decode(encodedPk);
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

	public static <PK extends PrimaryKey<PK>> List<PK> decodeMulti(Class<PK> pkClass, String delimiter,
			String encodedPks){
		String[] eachEncodedPk = encodedPks.split(delimiter);
		return Arrays.stream(eachEncodedPk)
				.map(encodedPk -> decode(pkClass, encodedPk))
				.collect(Collectors.toList());
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


	/*-------------- tests --------------------*/

	public static class PrimaryKeyPercentCodecTests{
		@Test
		public void testSimpleNumericPk(){
			Long id = 355L;
			TraceKey pk = new TraceKey(id);
			String encoded = encode(pk);
			TraceKey decoded = decode(TraceKey.class, encoded);
			Assert.assertEquals(decoded.getId(), id);
		}
		@Test
		public void testMultiNumericPk(){
			final String delimiter = ",";
			List<Long> ids = Arrays.asList(23L, 52L, 103L);
			List<TraceKey> pks = StreamTool.map(ids, TraceKey::new);
			String encoded = encodeMulti(pks, delimiter);
			List<TraceKey> decodedPks = decodeMulti(TraceKey.class, delimiter, encoded);
			List<Long> decodedIds = StreamTool.map(decodedPks, TraceKey::getId);
			Assert.assertTrue(DrListTool.equals(ids, decodedIds));
		}
	}
}
