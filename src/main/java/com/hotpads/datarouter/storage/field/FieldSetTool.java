package com.hotpads.datarouter.storage.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.number.VarLong;

public class FieldSetTool{
	static Logger logger = LoggerFactory.getLogger(FieldSetTool.class);

	public static int getNumNonNullFields(FieldSet<?> prefix){
		int numNonNullFields = 0;
		for(Object value : CollectionTool.nullSafe(prefix.getFieldValues())){
			if(value != null){
				++numNonNullFields;
			}
		}
		return numNonNullFields;
	}

	public static void appendWhereClauseDisjunction(StringBuilder sql,
			Collection<? extends FieldSet<?>> fieldSets){
		if(CollectionTool.isEmpty(fieldSets)){ return; }
		int counter = 0;
		for(FieldSet<?> fieldSet : IterableTool.nullSafe(fieldSets)){
			if(counter > 0){
				sql.append(" or ");
			}
			//heavy on parenthesis.  optimize later
			sql.append("("+FieldTool.getSqlNameValuePairsEscapedConjunction(fieldSet.getFields())+")");
			++counter;
		}
	}

	public static List<String> getPersistentStrings(Collection<? extends FieldSet<?>> keys){
		List<String> outs = ListTool.createArrayListWithSize(keys);
		for(FieldSet<?> f : IterableTool.nullSafe(keys)){
			outs.add(f.getPersistentString());
		}
		return outs;
	}

	public static <T> Map<String, Pair<Field<T>, Field<T>>> getFieldDifferences(Collection<Field<?>> left,
			Collection<Field<?>> right) {

		Map<String, Pair<Field<T>, Field<T>>> diffMap = Maps.newHashMap();

		Map<String, Field<?>> leftMap = generateFieldMap(left), rightMap = generateFieldMap(right);
		for (String key : Sets.union(leftMap.keySet(), rightMap.keySet())) {
			Field<T> leftField = (Field<T>) leftMap.get(key), rightField = (Field<T>) rightMap.get(key);

			if (ObjectTool.isOneNullButNotTheOther(leftField, rightField)
					|| ObjectTool.notEquals(leftField.getValue(), rightField.getValue())) {
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
			fieldMap.put(field.getName(), field);
		}
		return fieldMap;
	}

//	public static String getCsv(FieldSet<?> fieldSet){
//		StringBuilder sb = new StringBuilder();
//		for(Field<?> field : fieldSet.getFields()){
//			if(sb.length() > 0){ sb.append(","); }
//			String value = field.getSqlEscaped();
//			boolean containsQuotes = value.contains("\"");
//			if(containsQuotes){
//				sb.append("\""+value+"\"");
//			}else{
//				sb.append(value);
//			}
//		}
//		return sb.toString();
//	}


	/***************************** construct fieldsets using reflection ***************************/

	public static <F extends FieldSet<?>>F fieldSetFromHibernateResultUsingReflection(Class<F> cls,
			List<Field<?>> fields, Object sqlObject){
		F targetFieldSet = ReflectionTool.create(cls);
		Object[] cols = (Object[])sqlObject;
		int counter = 0;
		for(Field<?> field : fields){
			field.fromHibernateResultUsingReflection(targetFieldSet, cols[counter]);
			++counter;
		}
		return targetFieldSet;
	}

	public static <F> F fieldSetFromJdbcResultSetUsingReflection(Class<F> cls,
			List<Field<?>> fields, ResultSet rs, boolean ignorePrefix){
		F targetFieldSet = ReflectionTool.create(cls);
		int counter = 0;
		for(Field<?> field : fields){
			field.fromJdbcResultSetUsingReflection(targetFieldSet, rs);
			++counter;
		}
		return targetFieldSet;
	}
	
	public static <PK extends PrimaryKey<PK>, PKLookup extends Lookup<PK>> PKLookup lookupFromJdbcResultSetUsingReflection(Class<PKLookup> cls,
			List<Field<?>> fields, ResultSet rs, Class<PK> keyClass){
		PKLookup targetFieldSet = ReflectionTool.create(cls);
		targetFieldSet.setPrimaryKey(ReflectionTool.create(keyClass));
		for(Field<?> field : fields){
			field.fromJdbcResultSetUsingReflection(targetFieldSet, rs);
		}
		return targetFieldSet;
	}

	public static <F> F fieldSetFromByteStream(Class<F> cls,
			Map<String,Field<?>> fieldByPrefixedName, InputStream is) throws IOException{
		int databeanLength = (int)new VarLong(is).getValue();
		return fieldSetFromByteStreamKnownLength(cls, fieldByPrefixedName, is, databeanLength);
	}

	public static <F> F fieldSetFromBytes(Class<F> cls, Map<String,Field<?>> fieldByPrefixedName,
			byte[] bytes) throws IOException{
		return fieldSetFromByteStreamKnownLength(cls, fieldByPrefixedName, new ByteArrayInputStream(bytes),
				bytes.length);
	}

	public static <F> F fieldSetFromByteStreamKnownLength(Class<F> cls,
			Map<String,Field<?>> fieldByPrefixedName, InputStream is, int numBytes) throws IOException{
		F targetFieldSet = ReflectionTool.create(cls);
		int numBytesThroughDatabean = 0;
		while(true){
			VarLong nameLength = new VarLong(is);//will throw IllegalArgumentException at the end of the stream
			byte[] nameBytes = new byte[nameLength.getValueInt()];
			is.read(nameBytes);
			numBytesThroughDatabean += nameLength.getNumBytes() + nameLength.getValueInt();
			String prefixedName = StringByteTool.fromUtf8Bytes(nameBytes);
			Field<?> field = fieldByPrefixedName.get(prefixedName);
			if(field==null){ continue; }
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


	/**************************** bytes ******************/

	/*
	 * the trailingSeparatorAfterEndingString is for backwards compatibility with some early tables
	 * that appended a trailing 0 to the byte[] even though it wasn't necessary
	 */
	public static byte[] getConcatenatedValueBytes(Collection<Field<?>> fields, boolean allowNulls,
			boolean trailingSeparatorAfterEndingString){
		int numFields = FieldTool.countNonNullLeadingFields(fields);
		if(numFields==0){ return null; }
		if(numFields==1){
			if(trailingSeparatorAfterEndingString){
				return CollectionTool.getFirst(fields).getBytesWithSeparator();
			}else{
				return CollectionTool.getFirst(fields).getBytes();
			}
		}
		byte[][] fieldArraysWithSeparators = new byte[CollectionTool.size(fields)][];
		int fieldIdx=-1;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			++fieldIdx;
			boolean lastField = fieldIdx == numFields - 1;
			if(!allowNulls && field.getValue()==null){
				throw new IllegalArgumentException("field:"+field.getName()+" cannot be null in");
			}
			if(!lastField || trailingSeparatorAfterEndingString){
				fieldArraysWithSeparators[fieldIdx] = field.getBytesWithSeparator();
			}else{
				fieldArraysWithSeparators[fieldIdx] = field.getBytes();
			}
			if(lastField){ break; }
		}
		return ByteTool.concatenate(fieldArraysWithSeparators);
	}

	/*
	 * should combine this with getConcatenatedValueBytes
	 */
	public static byte[] getBytesForNonNullFieldsWithNoTrailingSeparator(FieldSet<?> fields){
		int numNonNullFields = getNumNonNullFields(fields);
		byte[][] fieldArraysWithSeparators = new byte[numNonNullFields][];
		int fieldIdx=-1;
		for(Field<?> field : IterableTool.nullSafe(fields.getFields())){
			++fieldIdx;
			if(fieldIdx == numNonNullFields - 1){//last field
				fieldArraysWithSeparators[fieldIdx] = field.getBytes();
				break;
			}
			fieldArraysWithSeparators[fieldIdx] = field.getBytesWithSeparator();
		}
		return ByteTool.concatenate(fieldArraysWithSeparators);
	}

	/**
	 * @param fields
	 * @param includePrefix usually refers to the "key." prefix before a PK
	 * @param skipNullValues important to include nulls in PK's, but usually skip them in normal fields
	 * @return
	 */
	public static byte[] getSerializedKeyValues(Collection<Field<?>> fields, boolean includePrefix,
			boolean skipNullValues){
		if(CollectionTool.isEmpty(fields)){ return new byte[0]; }
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			//prep the values
			byte[] keyBytes = includePrefix ?
					StringByteTool.getUtf8Bytes(field.getPrefixedName()) : field.getColumnNameBytes();
			VarLong keyLength = new VarLong(ArrayTool.length(keyBytes));
			byte[] valueBytes = field.getBytes();
			VarLong valueLength = new VarLong(ArrayTool.length(valueBytes));
			//abort if value is 0 bytes
			if(ArrayTool.isEmpty(valueBytes) && skipNullValues){ continue; }
			try{
				//write out the bytes
				baos.write(keyLength.getBytes());
				baos.write(keyBytes);
				baos.write(valueLength.getBytes());
				baos.write(valueBytes);
			}catch(IOException e){
				throw new RuntimeException("a ByteArrayOutputStream threw an IOException... not sure how that could happen");
			}
		}
		return baos.toByteArray();
	}

	/*************************** tests *********************************/

	public static class FieldSetToolTests{

		@Test
		public void testGetConcatenatedValueBytes() {
			int someInt = 55;
			String someStringA = "abc";
			String someStringB = "xyz";
			List<Field<?>> fields = FieldTool.createList(
					new UInt31Field("someInt", someInt),
					new StringField("someStringA", someStringA, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField("someStringB", someStringB, MySqlColumnType.MAX_LENGTH_VARCHAR));
			ByteRange withTrailingByte = new ByteRange(getConcatenatedValueBytes(fields, false, true));
			ByteRange withoutTrailingByte = new ByteRange(getConcatenatedValueBytes(fields, false, false));
			int lengthWithout = 4 + 3 + 1 + 3;
			int lengthWith = lengthWithout + 1;
			Assert.assertEquals(lengthWith, withTrailingByte.getLength());
			Assert.assertEquals(lengthWithout, withoutTrailingByte.getLength());
		}

		@Test
		public void testGenerateFieldMap() {
			int testInt = 127;
			String someStr0 = "first", someStr1 = "second";

			List<Field<?>> fields = FieldTool.createList(
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

			List<Field<?>> left = FieldTool.createList(
					new StringField(one, "help", MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(two, "smite", MySqlColumnType.MAX_LENGTH_VARCHAR),
					new BooleanField(three, true),
					new LongField(four, sameRefLong),
					new DumbDoubleField(five, 5e6));
					// omitted six

			List<Field<?>> right = FieldTool.createList(
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

	/************************** field to byte helpers *****************************************/


}
