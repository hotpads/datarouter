package com.hotpads.util.core.enums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.web.HTMLSelectOptionBean;

public class EnumTool {

	private static Pattern NO = Pattern.compile("(^(no ))|( no )|(/no )|(:no )|(:no$)|(: no$)");

	public static <T extends HpEnum> List<HTMLSelectOptionBean>
	getHTMLSelectOptions(T[] values){
		return getHTMLSelectOptions(values,null);
	}
	public static <T extends HpEnum> List<HTMLSelectOptionBean>
	getHTMLSelectOptions(T[] values, Integer ignoredValue){
		List<HTMLSelectOptionBean> options =
			new ArrayList<>();
		for(T type:values){
			if(ignoredValue!=null && ignoredValue==type.getInteger()) {
				continue;
			}
			options.add(
				new HTMLSelectOptionBean(type.getDisplay(),
										 type.getInteger().toString()));
		}
		return options;
	}

	public static <T extends StringPersistedEnum> List<HTMLSelectOptionBean>
	getHTMLSelectOptions(T[] values, String... ignoredValues){
		List<HTMLSelectOptionBean> options =
			new ArrayList<>();
		List<String> ignoredPersistentStrings =
			ignoredValues.length==0 || ignoredValues[0]==null
			? null : ImmutableList.copyOf(ignoredValues);
		for(T type:values){
			if(ignoredPersistentStrings!=null
				&& ignoredPersistentStrings.contains(type.getPersistentString())) {
				continue;
			}
			options.add(
				new HTMLSelectOptionBean(type.getDisplay(),
										 type.getPersistentString()));
		}
		return options;
	}

	public static <T extends HpVarEnum> List<HTMLSelectOptionBean>
	getHTMLSelectOptionsVarNames(T[] values){
		return getHTMLSelectOptionsVarNames(values,null);
	}

	public static <T extends HpVarEnum> List<HTMLSelectOptionBean>
	getHTMLSelectOptionsVarNames(T[] values, Integer ignoredValue){
		List<HTMLSelectOptionBean> options =
				new ArrayList<>();
		for(T type:values){
			if(ignoredValue!=null && ignoredValue==type.getInteger()) {
				continue;
			}
			options.add(
					new HTMLSelectOptionBean(type.getDisplay(),
							type.getVarName()));
		}
		return options;
	}

	public static <T extends HpEnum> List<Integer> getIntegers(Collection<T> enums){
		List<Integer> ints = Lists.newArrayList();
		if(enums==null||enums.isEmpty()) {
			return ints;
		}
		for(T e : enums){
			ints.add(e.getInteger());
		}
		return ints;
	}

	public static <T extends HpEnum> T getEnumFromDisplay(T[] values, String display, T defaultEnum){
		if(display==null) {
			return defaultEnum;
		}
		for(T type:values){
			if(type.getDisplay().equalsIgnoreCase(display)) {
				return type;
			}
		}
		return defaultEnum;
	}
	public static <T extends Enum<?>> T getEnumFromName(T[] values, String name, T defaultEnum){
		if(name==null) {
			return defaultEnum;
		}
		for(T type:values){
			if(type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return defaultEnum;
	}
	public static <T extends HpVarEnum> T getEnumFromVarName(T[] values, String varName, T defaultEnum){
		if(varName==null) {
			return defaultEnum;
		}
		for(T type:values){
			if(type.getVarName().equalsIgnoreCase(varName)) {
				return type;
			}
		}
		return defaultEnum;
	}
	public static <T extends HpEnum> T getEnumFromInteger(T[] values, Integer value, T defaultEnum){
		if(value==null) {
			return defaultEnum;
		}
		for(T type:values){
			if(type.getInteger().equals(value)) {
				return type;
			}
		}
		return defaultEnum;
	}

	public static <T extends PersistentString> T fromPersistentString(
			T[] types, String name){
		return fromPersistentString(types, name, true);
	}
	public static <T extends PersistentString> T fromPersistentString(
			T[] types, String name, boolean caseSensitive){
		if(name == null) {
			return null;
		}
		for(T t : types){
			if(caseSensitive && name.equals(t.getPersistentString())
					|| !caseSensitive
							&& name.equalsIgnoreCase(t.getPersistentString())){
				return t;
			}
		}
		return null;
	}

	public static <T extends HpEnum> Set<T> decodeEnums(int codedEnums, T[] values){
		Set<T> enums = new HashSet<>();
		for(T e:values){
			int i = e.getInteger();
			int mask = (int)Math.pow(2,i);
			if((mask&codedEnums) > 0){
				enums.add(e);
			}
		}
		return enums;
	}
	public static <T extends HpEnum> int encodeEnum(T enumToEncode){
		if(enumToEncode.getInteger()>30) {
			throw new IllegalArgumentException("Can't encode an enum with value >30");
		}
		return (int)Math.pow(2, enumToEncode.getInteger());
	}
	public static <T extends HpEnum> int addEnumToEncoding(T enumToEncode, int encoding){
		return encoding | encodeEnum(enumToEncode);
	}
	public static <T extends HpEnum> int removeEnumFromEncoding(T enumToRemove, int encoding){
		return encoding & ~encodeEnum(enumToRemove);
	}
	public static <T extends HpEnum> int encodeEnums(Collection<T> enumsToEncode){
		int encoded = 0;
		for(T e:enumsToEncode){
			encoded = encoded | encodeEnum(e);
		}
		return encoded;
	}
	public static <T extends HpEnum> int getMaxEnumValue(T[] values){
		int max = Integer.MIN_VALUE;
		for(T v:values){
			if(v.getInteger()!=null && v.getInteger()>max) {
				max = v.getInteger();
			}
		}
		return max;
	}

	public static <T extends AlternateNamedEnum> T getFromFreeText(T[] values, String freeText) {
		List<T> types = getFromFreeText(values, freeText, false);
		if (DrCollectionTool.notEmpty(types)) {
			return types.get(0);
		}
		return null;
	}

	public static <T extends AlternateNamedEnum> List<T> getMultipleValuesFromFreeText(T[] values, String freeText) {
		return getFromFreeText(values, freeText, true);
	}

	public static < T extends StringEnum<T>> Set<T> getStringEnumFromFreeText(T[] values, String freeText ) {
		Set<T> result = new HashSet<>();
		if (DrStringTool.isEmpty(freeText) || values == null || values.length == 0) {
			return result;
		}
		String[] elements = freeText.split("[,\\s]+");
		T sample = values[ 0 ];
		for ( String element : elements ) {
			element = element.trim();
			if (DrStringTool.isEmpty(element)) {
				continue;
			}
			T elementValue = sample.fromPersistentString( element );
			if ( elementValue == null ) {
				continue;
			}
			// This is a valid value - is it in the desired list?
			for ( T value : values ) {
				if (elementValue == value) {
					result.add( elementValue );
					break;
				}
			}
		}
		return result;
	}

	private static <T extends AlternateNamedEnum> List<T> getFromFreeText(T[] values, String freeText,
			boolean exhaustive) {
		if (DrStringTool.isEmpty(freeText)) {
			return new LinkedList<>();
		}
		freeText = freeText.toLowerCase();
		if (NO.matcher(freeText).find()) {
			return new LinkedList<>();
		}
		Set<T> types = new HashSet<>();
		String[] splitFreeText = freeText.split("\\s+");
		for (T a : values) {
			for (String alt : a.getAlternates()) {
				if (!freeText.contains(alt)) {
					continue;
				}
				for (String freeTextWord : splitFreeText) {
					if (alt.contains(freeTextWord)) {
						types.add(a);
						if (exhaustive) {
							break;
						}
						return DrListTool.createLinkedList(types);
					}
				}
			}
		}
		return DrListTool.createLinkedList(types);
	}

	/** tests *****************************************************************/
	public static class EnumToolTests {
		private enum TestEnum implements HpEnum, StringPersistedEnum, StringEnum<TestEnum> {
			UNKNOWN(-1, "Unknown", "unknown"),
			RENTAL(1, "Rental", "rental"),
			SALE(2, "For Sale", "sale"),
			CORPORATE(3, "Corporate Housing", "corporate"),
			SUBLET(4, "Sublet", "sublet"),
			ROOM(5, "Room", "room"),
			VACATION(6, "Vacation Rental", "vacation"),
			ASSISTED(7, "Assisted Living", "assisted"),
			COMMERCIAL(8, "Commercial", "commercial"),
			FORECLOSURE(9, "Foreclosure", "foreclosure"),
			NEW_HOME(10,"New Home", "newHome"),
			AUCTION(11,"Auction", "auction"),
			LAND(12,"Land", "land"),
			HOTEL(13,"Hotel", "hotel");

			private Integer value;
			private String display;
			private String var;

			private TestEnum(Integer value, String display, String varName){
				this.value = value;
				this.display = display;
				this.var = varName;
			}

			@Override
			public String getDisplay(){
				return display;
			}

			@Override
			public Integer getInteger(){
				return value;
			}

			@Override
			public String getPersistentString(){
				return var;
			}

			@Override
			public TestEnum fromPersistentString(String text){
				if (text == null || text.isEmpty() ) {
					return null;
				}
				text = text.trim();
				for ( TestEnum testEnum : values() ) {
					if (testEnum.getPersistentString().equalsIgnoreCase(text)) {
						return testEnum;
					}
				}
				return null;
			}
		}

		@Test
		public void testStringEnumFromFreeText() {
			List<TestEnum> ts = new ArrayList<>();
			ts.add(TestEnum.RENTAL);
			ts.add(TestEnum.SUBLET);
			ts.add(TestEnum.ASSISTED);
			ts.add(TestEnum.ROOM);
			ts.add(TestEnum.AUCTION);
			String sample = "";
			for ( TestEnum test : ts ) {
				sample = sample + "," + test.getPersistentString();
			}
			Set<TestEnum> set = EnumTool.getStringEnumFromFreeText(TestEnum.values(), sample);
			for ( TestEnum test : TestEnum.values()) {
				if ( ts.contains( test ) ) {
					Assert.assertTrue( set.contains( test ) );
				} else {
					Assert.assertFalse( set.contains( test ) );
				}
			}
		}

		@Test
		public void testEncoding(){
			List<TestEnum> ts = new ArrayList<>();
			ts.add(TestEnum.RENTAL);
			ts.add(TestEnum.SUBLET);
			ts.add(TestEnum.ASSISTED);
			ts.add(TestEnum.ROOM);
			ts.add(TestEnum.AUCTION);
			int e = encodeEnums(ts);
			Set<TestEnum> decoded = decodeEnums(e,TestEnum.values());
			Assert.assertTrue(decoded.contains(TestEnum.RENTAL));
			Assert.assertTrue(decoded.contains(TestEnum.SUBLET));
			Assert.assertTrue(decoded.contains(TestEnum.ASSISTED));
			Assert.assertTrue(decoded.contains(TestEnum.ROOM));
			Assert.assertFalse(decoded.contains(TestEnum.CORPORATE));
			Assert.assertFalse(decoded.contains(TestEnum.SALE));
			Assert.assertFalse(decoded.contains(TestEnum.NEW_HOME));
			Assert.assertTrue(decoded.contains(TestEnum.AUCTION));
			Assert.assertFalse(decoded.contains(TestEnum.FORECLOSURE));
			e = removeEnumFromEncoding(TestEnum.ROOM,e);
			decoded = decodeEnums(e,TestEnum.values());
			Assert.assertTrue(decoded.contains(TestEnum.AUCTION));
			Assert.assertFalse(decoded.contains(TestEnum.ROOM));
			e = addEnumToEncoding(TestEnum.ROOM,e);
			decoded = decodeEnums(e,TestEnum.values());
			Assert.assertTrue(decoded.contains(TestEnum.AUCTION));
			Assert.assertTrue(decoded.contains(TestEnum.ROOM));
		}

		@Test
		public void testGetHtmlSelectOptions(){
			Assert.assertEquals(
					TestEnum.values().length-1,
					getHTMLSelectOptions(
							TestEnum.values(),
							TestEnum.UNKNOWN.var)
					.size());
			Assert.assertEquals(
					TestEnum.values().length-2,
					getHTMLSelectOptions(
							TestEnum.values(),
							TestEnum.UNKNOWN.var,
							TestEnum.COMMERCIAL.var)
					.size());
			Assert.assertEquals(TestEnum.values().length,
					getHTMLSelectOptions(TestEnum.values()).size());
			Assert.assertEquals(TestEnum.values().length,
					getHTMLSelectOptions(TestEnum.values(),(String)null)
					.size());
		}

		@Test
		public void testFromPersistentString(){
			Assert.assertEquals(TestEnum.NEW_HOME,
					fromPersistentString(TestEnum.values(),
							TestEnum.NEW_HOME.getPersistentString()));
			Assert.assertNull(fromPersistentString(TestEnum.values(),
					TestEnum.NEW_HOME.getPersistentString().toLowerCase()));
			Assert.assertEquals(TestEnum.NEW_HOME,
					fromPersistentString(TestEnum.values(),
							TestEnum.NEW_HOME.getPersistentString()
							.toLowerCase(),false));
			Assert.assertNull(fromPersistentString(TestEnum.values(),
					TestEnum.NEW_HOME.getPersistentString().toLowerCase(),
					true));
		}

		@Test
		public void testNoPattern(){
			Assert.assertEquals(false, NO.matcher(" no").find());
			Assert.assertEquals(true, NO.matcher("no ").find());
			Assert.assertEquals(true, NO.matcher(" no ").find());
			Assert.assertEquals(true, NO.matcher("/no ").find());
			Assert.assertEquals(false, NO.matcher("/no").find());
			Assert.assertEquals(true, NO.matcher(":no ").find());
			Assert.assertEquals(true, NO.matcher(":no").find());
			Assert.assertEquals(true, NO.matcher(": no").find());
		}
	}

}
