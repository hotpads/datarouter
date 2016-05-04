package com.hotpads.util.core.collections;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrStringTool;

public class KeyRangeTool{

	public static <PK extends PrimaryKey<PK>> Range<PK> forPrefix(PK prefix){
		return new Range<>(prefix, true, prefix, true);
	}

	public static <PK extends PrimaryKey<PK>> Range<PK> forPrefixWithWildcard(String prefixString,
			KeyWithStringFieldSuffixProvider<PK> keyWithStringFieldSuffixProvider){
		String startString = prefixString == null ? null : prefixString;
		String endString = incrementLastChar(prefixString);
		PK startKey = keyWithStringFieldSuffixProvider.createWithSuffixStringField(startString);
		PK endKey = keyWithStringFieldSuffixProvider.createWithSuffixStringField(endString);
		return new Range<>(startKey,endKey);
	}

	public static String incrementLastChar(String string){
		if(DrStringTool.isEmpty(string)){
			return null;
		}
		int lastCharPos = string.length() - 1;
		return string.substring(0, lastCharPos) + (char) (string.charAt(lastCharPos) + 1);
	}

	public interface KeyWithStringFieldSuffixProvider<PK extends PrimaryKey<PK>>{
		PK createWithSuffixStringField(String fieldValue);
	}

	public static class KeyRangeToolTests{

		@Test
		public void testForPrefixWithWildcard(){
			Integer foo = 4;
			String barPrefix = "degemer";
			Range<TestKey> range = forPrefixWithWildcard(barPrefix, prefix -> new TestKey(foo, prefix, null));
			Assert.assertTrue(range.getStartInclusive());
			Assert.assertFalse(range.getEndInclusive());
			Assert.assertEquals(range.getStart().foo, foo);
			Assert.assertEquals(range.getStart().bar, barPrefix);
			Assert.assertEquals(range.getStart().baz, null);
			Assert.assertEquals(range.getEnd().foo, foo);
			Assert.assertEquals(range.getEnd().bar, "degemes");
			Assert.assertEquals(range.getEnd().baz, null);
			Assert.assertTrue(range.contains(new TestKey(foo, "degemer", null)));
			Assert.assertTrue(range.contains(new TestKey(foo, "degemermat", 53L)));
			Assert.assertFalse(range.contains(new TestKey(foo, "degemeola", null)));
		}

		@SuppressWarnings("serial")
		private class TestKey extends BasePrimaryKey<TestKey>{

			private Integer foo;
			private String bar;
			private Long baz;

			public TestKey(Integer foo, String bar, Long baz){
				this.foo = foo;
				this.bar = bar;
				this.baz = baz;
			}

			@Override
			public List<Field<?>> getFields(){
				return Arrays.asList(
						new IntegerField(new IntegerFieldKey("foo"), foo),
						new StringField(new StringFieldKey("bar"), bar),
						new LongField(new LongFieldKey("baz"), baz));
			}

		}

	}
}
