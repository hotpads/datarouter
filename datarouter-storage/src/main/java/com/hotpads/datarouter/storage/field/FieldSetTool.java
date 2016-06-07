package com.hotpads.datarouter.storage.field;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.number.VarLong;

public class FieldSetTool{

	public static int getNumNonNullLeadingFields(FieldSet<?> prefix){
		int numNonNullFields = 0;
		for(Object value : DrCollectionTool.nullSafe(prefix.getFieldValues())){
			if(value == null){ break; }
			++numNonNullFields;

		}
		return numNonNullFields;
	}

	public static int getNumNonNullFields(FieldSet<?> prefix){
		int numNonNullFields = 0;
		for(Object value : DrCollectionTool.nullSafe(prefix.getFieldValues())){
			if(value != null){
				++numNonNullFields;
			}
		}
		return numNonNullFields;
	}

	@Deprecated //use Percent Codecs
	public static List<String> getPersistentStrings(Collection<? extends FieldSet<?>> keys){
		List<String> outs = DrListTool.createArrayListWithSize(keys);
		for(FieldSet<?> f : DrIterableTool.nullSafe(keys)){
			outs.add(f.getPersistentString());
		}
		return outs;
	}

	@Deprecated //use Percent Codecs
	public static String getPersistentString(List<Field<?>> fields){
		StringBuilder sb = new StringBuilder();
		boolean doneOne = false;
		for(Field<?> field : fields){
			if(doneOne){
				sb.append("_");
			}else{
				doneOne = true;
			}
			sb.append(field.getValueString());
		}
		return sb.toString();
	}

	public static <T> Map<String, Pair<Field<T>, Field<T>>> getFieldDifferences(Collection<Field<?>> left,
			Collection<Field<?>> right) {

		Map<String, Pair<Field<T>, Field<T>>> diffMap = Maps.newHashMap();

		Map<String, Field<?>> leftMap = generateFieldMap(left), rightMap = generateFieldMap(right);
		for (String key : Sets.union(leftMap.keySet(), rightMap.keySet())) {
			Field<T> leftField = (Field<T>) leftMap.get(key), rightField = (Field<T>) rightMap.get(key);

			if (DrObjectTool.isOneNullButNotTheOther(leftField, rightField)
					|| DrObjectTool.notEquals(leftField.getValue(), rightField.getValue())) {
				diffMap.put(key, Pair.create(leftField, rightField));
			}
		}

		return diffMap;
	}

	public static Map<String, Field<?>> generateFieldMap(Collection<Field<?>> fields) {
		Map<String, Field<?>> fieldMap = Maps.newTreeMap();
		if (fields == null) {
			return fieldMap;
		}

		Iterator<Field<?>> fieldIter = fields.iterator();
		while (fieldIter.hasNext()) {
			Field<?> field = fieldIter.next();
			fieldMap.put(field.getKey().getName(), field);
		}
		return fieldMap;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>Map<PK,List<Field<?>>> getFieldsByKey(
			Iterable<D> databeans, DatabeanFielder<PK,D> fielder){
		Map<PK,List<Field<?>>> fieldsByKey = new HashMap<>();
		for(D databean : databeans){
			fieldsByKey.put(databean.getKey(), fielder.getFields(databean));
		}
		return fieldsByKey;
	}


	/***************************** construct fieldsets using reflection ***************************/

	public static <F> F fieldSetFromByteStream(Supplier<F> supplier,
			Map<String,Field<?>> fieldByPrefixedName, InputStream is) throws IOException{
		int databeanLength = (int)new VarLong(is).getValue();
		return fieldSetFromByteStreamKnownLength(supplier, fieldByPrefixedName, is, databeanLength);
	}

	public static <F> F fieldSetFromBytes(Supplier<F> supplier, Map<String,Field<?>> fieldByPrefixedName,
			byte[] bytes) throws IOException{
		return fieldSetFromByteStreamKnownLength(supplier, fieldByPrefixedName, new ByteArrayInputStream(bytes),
				bytes.length);
	}

	public static <F> F fieldSetFromByteStreamKnownLength(Supplier<F> supplier,
			Map<String,Field<?>> fieldByPrefixedName, InputStream is, int numBytes) throws IOException{
		F targetFieldSet = supplier.get();
		int numBytesThroughDatabean = 0;
		while(true){
			VarLong nameLength = new VarLong(is);//will throw IllegalArgumentException at the end of the stream
			byte[] nameBytes = new byte[nameLength.getValueInt()];
			is.read(nameBytes);
			numBytesThroughDatabean += nameLength.getNumBytes() + nameLength.getValueInt();
			String prefixedName = StringByteTool.fromUtf8Bytes(nameBytes);
			Field<?> field = fieldByPrefixedName.get(prefixedName);
			if(field==null){
				continue;
			}
			VarLong valueLength = new VarLong(is);
			numBytesThroughDatabean += valueLength.getNumBytes();
			byte[] valueBytes = new byte[valueLength.getValueInt()];
			if(valueLength.getValue() > 0){
				is.read(valueBytes);
				numBytesThroughDatabean += valueLength.getValueInt();
			}
			Object value = field.fromBytesButDoNotSet(valueBytes, 0);
			field.setUsingReflection(targetFieldSet, value);
			if(numBytesThroughDatabean >= numBytes){
				break;
			}
		}
		return targetFieldSet;
	}

	public static <F extends FieldSet<?>> F fromConcatenatedValueBytes(Class<F> cls, List<Field<?>> fields, byte[] bytes){
		F fieldSet = ReflectionTool.create(cls);
		if(DrArrayTool.isEmpty(bytes)){ return fieldSet; }

		int byteOffset = 0;
		for(Field<?> field : fields){
			if(byteOffset==bytes.length){ break; }//ran out of bytes.  leave remaining fields blank
			int numBytesWithSeparator = field.numBytesWithSeparator(bytes, byteOffset);
			Object value = field.fromBytesWithSeparatorButDoNotSet(bytes, byteOffset);
			field.setUsingReflection(fieldSet, value);
			byteOffset+=numBytesWithSeparator;
		}

		return fieldSet;
	}


	/*************************** tests *********************************/

	public static class FieldSetToolTests{

		@Test
		public void testGetConcatenatedValueBytes() {
			int someInt = 55;
			String someStringA = "abc";
			String someStringB = "xyz";
			List<Field<?>> fields = Arrays.asList(
					new UInt31Field("someInt", someInt),
					new StringField("someStringA", someStringA, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField("someStringB", someStringB, MySqlColumnType.MAX_LENGTH_VARCHAR));
			ByteRange withTrailingByte = new ByteRange(FieldTool.getConcatenatedValueBytes(fields, false, true, true));
			ByteRange withoutTrailingByte = new ByteRange(FieldTool.getConcatenatedValueBytes(fields, false, true,
					false));
			int lengthWithout = 4 + 3 + 1 + 3;
			int lengthWith = lengthWithout + 1;
			Assert.assertEquals(lengthWith, withTrailingByte.getLength());
			Assert.assertEquals(lengthWithout, withoutTrailingByte.getLength());
		}

		@Test
		public void testGenerateFieldMap() {
			int testInt = 127;
			String someStr0 = "first", someStr1 = "second";

			List<Field<?>> fields = Arrays.asList(
					new StringField("hahah", someStr0, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField("moose", someStr1, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new UInt31Field("integ", testInt));

			Map<String, Field<?>> fieldMap = generateFieldMap(fields);
			Assert.assertEquals(fields.size(), fieldMap.size());
			Assert.assertNotNull(fieldMap.get("hahah"));
		}

		@Test
		public <T> void testGetFieldDifferences() {
			String one = "one", two = "two", three = "three", four = "four", five = "five", six = "six";
			Long sameRefLong = new Long(123456789000l);

			List<Field<?>> left = Arrays.asList(
					new StringField(one, "help", MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(two, "smite", MySqlColumnType.MAX_LENGTH_VARCHAR),
					new BooleanField(three, true),
					new LongField(four, sameRefLong),
					new DumbDoubleField(five, 5e6));
					// omitted six

			List<Field<?>> right = Arrays.asList(
					new StringField(one, "help", MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(two, two, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new BooleanField(three, null),
					new LongField(four, sameRefLong),
					// omitted five
					new UInt31Field(six, 55));

			Map<String, Pair<Field<T>, Field<T>>> diffs = getFieldDifferences(left, right);
			Pair<Field<T>, Field<T>> test = null;

			test = diffs.get(one);
			Assert.assertNull(test);

			test = diffs.get(two);
			Assert.assertNotNull(test);
			Assert.assertNotSame(test.getLeft().getValue(), test.getRight().getValue());

			test = diffs.get(three);
			Assert.assertNotNull(test);
			Assert.assertNull(test.getRight().getValue());
			Assert.assertNotSame(test.getLeft().getValue(), test.getRight().getValue());

			test = diffs.get(four);
			Assert.assertNull(test);

			test = diffs.get(five);
			Assert.assertNotNull(test);
			Assert.assertNull(test.getRight());

			test = diffs.get(six);
			Assert.assertNotNull(test);
			Assert.assertNull(test.getLeft());

			test = diffs.get("this test does not exist");
			Assert.assertNull(test);

		}
	}

}
