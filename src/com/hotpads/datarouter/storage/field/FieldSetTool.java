package com.hotpads.datarouter.storage.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.number.VarLong;

public class FieldSetTool{
	static Logger logger = Logger.getLogger(FieldSetTool.class);

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
			sql.append("("+fieldSet.getSqlNameValuePairsEscapedConjunction()+")");
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
	
	
	/***************************** construct fieldsets using reflection ***************************/

	public static <D extends FieldSet<?>> D fieldSetFromHibernateResultUsingReflection(
			Class<D> cls, List<Field<?>> fields, Object sqlObject){
		D targetFieldSet = ReflectionTool.create(cls);
		Object[] cols = (Object[])sqlObject;
		int counter = 0;
		for(Field<?> field : fields){
			field.fromHibernateResultUsingReflection(targetFieldSet, cols[counter]);
			++counter;
		}
		return targetFieldSet;
	}

	public static <D extends FieldSet<?>> D fieldSetFromJdbcResultSetUsingReflection(
			Class<D> cls, List<Field<?>> fields, ResultSet rs, boolean ignorePrefix){
		D targetFieldSet = ReflectionTool.create(cls);
		int counter = 0;
		for(Field<?> field : fields){
			field.fromJdbcResultSetUsingReflection(targetFieldSet, rs);
			++counter;
		}
		return targetFieldSet;
	}

	public static <D extends FieldSet<?>> D fieldSetFromByteStream(Class<D> cls, 
			Map<String,Field<?>> fieldByPrefixedName, InputStream is) throws IOException{
		int databeanLength = (int)new VarLong(is).getValue();
		return fieldSetFromByteStreamKnownLength(cls, fieldByPrefixedName, is, databeanLength);
	}

	public static <D extends FieldSet<?>> D fieldSetFromBytes(Class<D> cls, 
			Map<String,Field<?>> fieldByPrefixedName, byte[] bytes) throws IOException{
		return fieldSetFromByteStreamKnownLength(cls, fieldByPrefixedName, 
				new ByteArrayInputStream(bytes), bytes.length);
	}

	public static <D extends FieldSet<?>> D fieldSetFromByteStreamKnownLength(Class<D> cls, 
			Map<String,Field<?>> fieldByPrefixedName, InputStream is, int numBytes) throws IOException{
		D targetFieldSet = ReflectionTool.create(cls);
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
			if(valueLength.getValue() > 0){
				byte[] valueBytes = new byte[valueLength.getValueInt()];
				is.read(valueBytes);
				numBytesThroughDatabean += valueLength.getNumBytes() + valueLength.getValueInt();
				Object value = field.fromBytesButDoNotSet(valueBytes, 0);
				field.setUsingReflection(targetFieldSet, value);
			}
			if(numBytesThroughDatabean >= numBytes){
				break;
			}
		}
		return targetFieldSet;
	}
	

	/**************************** bytes ******************/
	
	public static byte[] getConcatenatedValueBytes(Collection<Field<?>> fields, boolean allowNulls){
		if(CollectionTool.size(fields)==1){ return CollectionTool.getFirst(fields).getBytesWithSeparator(); }
		if(CollectionTool.isEmpty(fields)){ return null; }
		byte[][] fieldArraysWithSeparators = new byte[CollectionTool.size(fields)][];
		int fieldIdx=-1;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			++fieldIdx;
			if(!allowNulls && field.getValue()==null){
				throw new IllegalArgumentException("field:"+field.getName()+" cannot be null in");
			}
			//TODO don't append a separator after the last field, but that will break all currently persisted entities
			fieldArraysWithSeparators[fieldIdx] = field.getBytesWithSeparator();
		}
		return ByteTool.concatenate(fieldArraysWithSeparators);
	}
	
	public static byte[] getSerializedKeyValues(Collection<Field<?>> fields, boolean includePrefix){
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
			if(ArrayTool.isEmpty(valueBytes)){ continue; }
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
	
}
