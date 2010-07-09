package com.hotpads.datarouter.storage.field;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.IterableTool;

public class FieldSetTool{
	static Logger logger = Logger.getLogger(FieldSetTool.class);

	public static int getNumNonNullFields(FieldSet prefix){
		int numNonNullFields = 0;
		for(Object value : CollectionTool.nullSafe(prefix.getFieldValues())){
			if(value != null){
				++numNonNullFields;
			}
		}
		return numNonNullFields;
	}

	public static void appendWhereClauseDisjunction(StringBuilder sql,
			Collection<? extends FieldSet> fieldSets){
		if(CollectionTool.isEmpty(fieldSets)){ return; }
		int counter = 0;
		for(FieldSet fieldSet : IterableTool.nullSafe(fieldSets)){
			if(counter > 0){
				sql.append(" or ");
			}
			//heavy on parenthesis.  optimize later
			sql.append("("+fieldSet.getSqlNameValuePairsEscapedConjunction()+")");
			++counter;
		}
	}

	public static <D extends FieldSet> D fieldSetFromHibernateResultUsingReflection(
			Class<D> cls, List<Field<?>> fields, Object sqlObject, boolean ignorePrefix){
		D targetFieldSet = null;
		try{
			Object[] cols = (Object[])sqlObject;
			//use getDeclaredConstructor to access non-public constructors
			Constructor<D> constructor = cls.getDeclaredConstructor();
			constructor.setAccessible(true);
			targetFieldSet = constructor.newInstance();
			int counter = 0;
			for(Field<?> field : fields){
				field.fromHibernateResultUsingReflection(targetFieldSet, cols[counter], ignorePrefix);
				++counter;
			}
		}catch(Exception e){
			logger.warn(ExceptionTool.getStackTraceAsString(e));
			throw new DataAccessException(e.getClass().getSimpleName()+" on "+cls.getName());
		}
		return targetFieldSet;
	}

	public static <D extends FieldSet> D fieldSetFromJdbcResultSetUsingReflection(
			Class<D> cls, List<Field<?>> fields, ResultSet rs, boolean ignorePrefix){
		D targetFieldSet = null;
		try{
			//use getDeclaredConstructor to access non-public constructors
			Constructor<D> constructor = cls.getDeclaredConstructor();
			constructor.setAccessible(true);
			targetFieldSet = constructor.newInstance();
			int counter = 0;
			for(Field<?> field : fields){
				field.fromJdbcResultSetUsingReflection(targetFieldSet, rs, ignorePrefix);
				++counter;
			}
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()+" on "+cls.getName());
		}
		return targetFieldSet;
	}
	

	/**************************** bytes ******************/
	
	public static byte[] getBytes(Collection<Field<?>> fields){
		if(CollectionTool.size(fields)==1){ return CollectionTool.getFirst(fields).getBytes(); }
		if(CollectionTool.isEmpty(fields)){ return new byte[0]; }
		byte[][] fieldArraysWithSeparators = new byte[CollectionTool.size(fields)][];
		int fieldIdx=-1;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			++fieldIdx;
			fieldArraysWithSeparators[fieldIdx] = field.getBytesWithSeparator();
		}
		return ByteTool.concatenate(fieldArraysWithSeparators);
	}
	
}
