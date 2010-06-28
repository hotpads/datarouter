package com.hotpads.datarouter.client.imp.hibernate;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class SqlBuilder{
	
	/*************************** primary methods ***************************************/
	
	public static String getAll(
			Config config, String tableName, List<Field<?>> selectFields){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String deleteAll(
			Config config, String tableName){
		StringBuilder sql = new StringBuilder();
		addDeleteFromClause(sql, tableName);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String getMulti(
			Config config, String tableName, List<Field<?>> selectFields, 
			Collection<? extends FieldSet> keys){
		if(CollectionTool.isEmpty(keys)){//getAll() passes null in for keys
			throw new IllegalArgumentException("no keys provided... use getAll if you want the whole table.");
		}
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		sql.append(" where ");
		FieldSetTool.appendWhereClauseDisjunction(sql, keys);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String deleteMulti(
			Config config, String tableName,
			Collection<? extends FieldSet> keys){
		if(CollectionTool.isEmpty(keys)){//getAll() passes null in for keys
			throw new IllegalArgumentException("no keys provided... use getAll if you want the whole table.");
		}
		StringBuilder sql = new StringBuilder();
		addDeleteFromClause(sql, tableName);
		sql.append(" where ");
		FieldSetTool.appendWhereClauseDisjunction(sql, keys);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String getWithPrefixes(
			Config config, String tableName, List<Field<?>> selectFields, 
			Collection<? extends FieldSet> keys, boolean wildcardLastField){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		sql.append(" where ");
		addPrefixWhereClauseDisjunction(sql, keys, wildcardLastField);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String deleteWithPrefixes(
			Config config, String tableName, 
			Collection<? extends FieldSet> keys, boolean wildcardLastField){
		StringBuilder sql = new StringBuilder();
		addDeleteFromClause(sql, tableName);
		sql.append(" where ");
		addPrefixWhereClauseDisjunction(sql, keys, wildcardLastField);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String getInRange(
			Config config, String tableName, List<Field<?>> selectFields, 
			FieldSet start, boolean startInclusive, 
			FieldSet end, boolean endInclusive){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		sql.append(" where ");
		addRangeWhereClause(sql, start, startInclusive, end, endInclusive);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String getWithPrefixInRange(
			Config config, String tableName, List<Field<?>> selectFields, 
			FieldSet prefix, boolean wildcardLastField,
			FieldSet start, boolean startInclusive, 
			FieldSet end, boolean endInclusive){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		sql.append(" where ");
		sql.append("(");
		addPrefixWhereClause(sql, prefix, wildcardLastField);
		sql.append(") and (");
		addRangeWhereClause(sql, start, startInclusive, end, endInclusive);
		sql.append(")");
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	
	/*************************** secondary methods ***************************************/

	public static void addSelectFromClause(StringBuilder sql, String tableName, List<Field<?>> selectFields){
		sql.append("select ");
		FieldTool.appendCsvNames(sql, selectFields);
		sql.append(" from "+tableName);
	}

	public static void addDeleteFromClause(StringBuilder sql, String tableName){
		sql.append("delete from "+tableName);
	}
	
	public static void addPrefixWhereClauseDisjunction(StringBuilder sql, 
			Collection<? extends FieldSet> keys, boolean wildcardLastField){
		int counter = 0;
		for(FieldSet key : IterableTool.nullSafe(keys)){
			if(counter>0){ sql.append(" or "); }
			addPrefixWhereClause(sql, key, wildcardLastField);
			++counter;
		}
	}
	
	public static void addPrefixWhereClause(StringBuilder sql, FieldSet prefix, boolean wildcardLastField){
		int numNonNullFields = FieldSetTool.getNumNonNullFields(prefix);
		if(numNonNullFields==0){ return; }
		int numFullFieldsFinished = 0;
		for(Field<?> field : CollectionTool.nullSafe(prefix.getFields())){
			if(numFullFieldsFinished < numNonNullFields){
				if(numFullFieldsFinished > 0){ sql.append(" and "); }
				boolean lastNonNullField = (numFullFieldsFinished == numNonNullFields-1);
				boolean stringField = !(field instanceof PrimitiveField<?>);
				boolean doPrefixMatchOnField = wildcardLastField && lastNonNullField && stringField;
				if(doPrefixMatchOnField){
					String s = field.getSqlEscaped();
					String sqlEscapedWithWildcard = s.substring(0, s.length()-1) + "%'";
					sql.append(field.getName()+" like "+sqlEscapedWithWildcard);
				}else{
					sql.append(field.getSqlNameValuePairEscaped());
				}
				++numFullFieldsFinished;
			}
		}
	}
	
	public static void addRangeWhereClause(StringBuilder sql,
			FieldSet start, boolean startInclusive, 
			FieldSet end, boolean endInclusive){
		
		if(start != null && CollectionTool.notEmpty(start.getFields())){
			sql.append("(");
			List<Field<?>> startFields = ListTool.createArrayList(start.getFields());
			int numNonNullStartFields = FieldTool.countNonNullLeadingFields(startFields);
			for(int i=numNonNullStartFields; i > 0; --i){
				if(i<numNonNullStartFields){ sql.append(" or "); }
				sql.append("(");
				for(int j=0; j < i; ++j){
					if(j>0){ sql.append(" and "); }
					Field<?> startField = startFields.get(j);
					if(j < (i-1)){
						sql.append(startField.getSqlNameValuePairEscaped());
					}else{
						if(startInclusive && i==numNonNullStartFields){
							sql.append(startField.getName()+">="+startField.getSqlEscaped());
						}else{
							sql.append(startField.getName()+">"+startField.getSqlEscaped());
						}
					}
				}
				sql.append(")");
			}
			sql.append(")");
		}
		
		if(start!=null && end!=null){ sql.append(" and "); }
		
		if(end != null && CollectionTool.notEmpty(end.getFields())){
			sql.append("(");
			List<Field<?>> endFields = ListTool.createArrayList(end.getFields());
			int numNonNullEndFields = FieldTool.countNonNullLeadingFields(endFields);
			for(int i=0; i < numNonNullEndFields; ++i){
				if(i>0){ sql.append(" or "); }
				sql.append("(");
				for(int j=0; j <= i; ++j){
					if(j>0){ sql.append(" and "); }
					Field<?> endField = endFields.get(j);
					if(j==i){
						if(endInclusive && i==(numNonNullEndFields-1)){
							sql.append(endField.getName()+"<="+endField.getSqlEscaped());
						}else{
							sql.append(endField.getName()+"<"+endField.getSqlEscaped());
						}
					}else{
						sql.append(endField.getSqlNameValuePairEscaped());
					}
				}
				sql.append(")");
			}
			sql.append(")");
		}
	}
	
	public static void addLimitOffsetClause(StringBuilder sql, Config config){
		config = Config.nullSafe(config);
		
		if(config.getLimit()!=null && config.getOffset()!=null){
			sql.append(" limit "+config.getOffset()+", "+config.getLimit());
		}else if(config.getLimit()!=null){
			sql.append(" limit "+config.getLimit());
		}else if(config.getOffset()!=null){
			sql.append(" limit "+config.getOffset()+", "+Integer.MAX_VALUE);//stupid mysql syntax
		}
	}
}
