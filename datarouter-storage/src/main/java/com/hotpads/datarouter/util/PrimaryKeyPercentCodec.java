package com.hotpads.datarouter.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.stream.StreamTool;

public class PrimaryKeyPercentCodec{

	/*-------------- encode --------------------*/

	public static String encode(PrimaryKey<?> pk){
		return PercentFieldCodec.encodeFields(pk.getFields());
	}

	public static <PK extends PrimaryKey<PK>> String encodeMulti(Iterable<PK> pks, char delimiter){
		Preconditions.checkArgument(PercentFieldCodec.isValidExternalSeparator(delimiter), "invalid delimiter:"
				+ delimiter);
		return StreamTool.stream(pks)
				.map(PrimaryKeyPercentCodec::encode)
				.collect(Collectors.joining(Character.toString(delimiter)));
	}

	/*-------------- decode --------------------*/

	public static <PK extends PrimaryKey<PK>> PK decode(Class<PK> pkClass, String encodedPk){
		if(encodedPk == null){
			return null;
		}
		PK pk = ReflectionTool.create(pkClass);
		List<String> tokens = PercentFieldCodec.decode(encodedPk);
		int index = 0;
		for(Field<?> field : pk.getFields(pk)){
			if(index > tokens.size() - 1){
				break;
			}
			field.fromString(tokens.get(index));
			field.setUsingReflection(pk, field.getValue());
			field.setValue(null);// to be safe until Field logic is cleaned up
			++index;
		}
		return pk;
	}

	public static <PK extends PrimaryKey<PK>> List<PK> decodeMulti(Class<PK> pkClass, char delimiter,
			String encodedPks){
		List<String> eachEncodedPk = DrStringTool.splitOnCharNoRegex(encodedPks, delimiter);
		return eachEncodedPk.stream()
				.map(encodedPk -> decode(pkClass, encodedPk))
				.collect(Collectors.toList());
	}


	/*-------------- tests --------------------*/

	public static class PrimaryKeyPercentCodecTests{

		private static SortedBeanKey
				SBK_0 = new SortedBeanKey("abc", "def", 3, "ghi"),
				SBK_1 = new SortedBeanKey("%ab/", "d&^f", 3, "g_-hi");

		private static List<SortedBeanKey> SBK_MULTI = Arrays.asList(SBK_0, SBK_1);

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
			final char delimiter = ',';
			List<Long> ids = Arrays.asList(23L, 52L, 103L);
			List<TraceKey> pks = StreamTool.map(ids, TraceKey::new);
			String encoded = encodeMulti(pks, delimiter);
			List<TraceKey> decodedPks = decodeMulti(TraceKey.class, delimiter, encoded);
			List<Long> decodedIds = StreamTool.map(decodedPks, TraceKey::getId);
			Assert.assertEquals(ids, decodedIds);
		}

		@Test
		public void testStringPk(){
			String encoded = encode(SBK_0);
			SortedBeanKey decoded = decode(SortedBeanKey.class, encoded);
			Assert.assertEquals(decoded, SBK_0);
		}

		@Test
		public void testStringPkWithReservedCharacters(){
			String encoded = encode(SBK_1);
			SortedBeanKey decoded = decode(SortedBeanKey.class, encoded);
			Assert.assertEquals(decoded, SBK_1);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testInvalidDelimiter(){
			encodeMulti(SBK_MULTI, '/');
		}

		@Test
		public void testEncodeMulti(){
			final char delimiter = ',';
			String encoded = encodeMulti(SBK_MULTI, delimiter);
			List<SortedBeanKey> decoded = decodeMulti(SortedBeanKey.class, delimiter, encoded);
			Assert.assertEquals(decoded, SBK_MULTI);
		}
	}

}
