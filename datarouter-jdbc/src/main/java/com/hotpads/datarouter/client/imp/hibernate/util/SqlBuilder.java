package com.hotpads.datarouter.client.imp.hibernate.util;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodecFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.collections.Range;

public class SqlBuilder{
	
	/*************************** primary methods ***************************************/
	
	public static String getCount(Config config, String tableName, List<Field<?>> fields, 
			Collection<? extends FieldSet<?>> keys) {
		StringBuilder sql = new StringBuilder();
		sql.append("select count(*) from " + tableName);
		if (fields.size() > 0) {
			sql.append(" where ");
			FieldSetTool.appendWhereClauseDisjunction(sql, keys);			
		}
		return sql.toString();
	}
	
	public static String getAll(Config config, String tableName, List<Field<?>> selectFields, 
			String where, List<Field<?>> orderByFields){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		addWhereClauseWithWhere(sql, where);
		addOrderByClause(sql, orderByFields);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String deleteAll(Config config, String tableName){
		StringBuilder sql = new StringBuilder();
		addDeleteFromClause(sql, tableName);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String getMulti(
			Config config, String tableName, List<Field<?>> selectFields, 
			Collection<? extends FieldSet<?>> keys){
		if(DrCollectionTool.isEmpty(keys)){//getAll() passes null in for keys
			throw new IllegalArgumentException("no keys provided... use getAll if you want the whole table.");
		}
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		sql.append(" where ");
		FieldSetTool.appendWhereClauseDisjunction(sql, keys);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String deleteMulti(Config config, String tableName, Collection<? extends FieldSet<?>> keys){
		if(DrCollectionTool.isEmpty(keys)){//getAll() passes null in for keys
			throw new IllegalArgumentException("no keys provided... use getAll if you want the whole table.");
		}
		StringBuilder sql = new StringBuilder();
		addDeleteFromClause(sql, tableName);
		sql.append(" where ");
		FieldSetTool.appendWhereClauseDisjunction(sql, keys);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String getWithPrefixes(JdbcFieldCodecFactory codecFactory, Config config, String tableName,
			List<Field<?>> selectFields, Collection<? extends FieldSet<?>> keys, boolean wildcardLastField,
			List<Field<?>> orderByFields){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		sql.append(" where ");
		addPrefixWhereClauseDisjunction(codecFactory, sql, keys, wildcardLastField);
		addOrderByClause(sql, orderByFields);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static String deleteWithPrefixes(JdbcFieldCodecFactory codecFactory, Config config, String tableName,
			Collection<? extends FieldSet<?>> keys, boolean wildcardLastField){
		StringBuilder sql = new StringBuilder();
		addDeleteFromClause(sql, tableName);
		sql.append(" where ");
		addPrefixWhereClauseDisjunction(codecFactory, sql, keys, wildcardLastField);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	public static <T extends FieldSet<T>>String getInRange(JdbcFieldCodecFactory codecFactory, Config config,
			String tableName, List<Field<?>> selectFields, Range<T> range, List<Field<?>> orderByFields){
		return getInRange(codecFactory, config, tableName, selectFields, range.getStart(), range.getStartInclusive(),
				range.getEnd(), range.getEndInclusive(), orderByFields);
	}
	
	public static String getInRange(JdbcFieldCodecFactory codecFactory, Config config, String tableName,
			List<Field<?>> selectFields, FieldSet<?> start, boolean startInclusive, FieldSet<?> end,
			boolean endInclusive, List<Field<?>> orderByFields){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		if(needsRangeWhereClause(start, end)){
			sql.append(" where ");
		}
		addRangeWhereClause(codecFactory, sql, start, startInclusive, end, endInclusive);
		addOrderByClause(sql, orderByFields);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}
	
	
	/*************************** secondary methods ***************************************/

	public static void addSelectFromClause(StringBuilder sql, String tableName, List<Field<?>> selectFields){
		sql.append("select ");
		FieldTool.appendCsvColumnNames(sql, selectFields);
		sql.append(" from "+tableName);
	}

	public static void addDeleteFromClause(StringBuilder sql, String tableName){
		sql.append("delete from "+tableName);
	}
	
	public static void addWhereClauseWithWhere(StringBuilder sql, String where){
		if(DrStringTool.notEmpty(where)){
			sql.append(" where "+where);
		}
	}
	
	public static void addPrefixWhereClauseDisjunction(JdbcFieldCodecFactory codecFactory, StringBuilder sql, 
			Collection<? extends FieldSet<?>> keys, boolean wildcardLastField){
		int counter = 0;
		if(keys.size()>1){
			sql.append("(");
		}
		for(FieldSet<?> key : DrIterableTool.nullSafe(keys)){
			if(counter>0){ 
				sql.append(") or ("); 
			}
			addPrefixWhereClause(codecFactory, sql, key, wildcardLastField);
			++counter;
		}
		if(counter>1){
			sql.append(")");
		}
	}
	
	public static void addPrefixWhereClause(JdbcFieldCodecFactory codecFactory, StringBuilder sql, FieldSet<?> prefix, 
			boolean wildcardLastField){
		int numNonNullFields = FieldSetTool.getNumNonNullLeadingFields(prefix);
		if(numNonNullFields==0){ return; }
		int numFullFieldsFinished = 0;
		List<JdbcFieldCodec<?,?>> codecs = codecFactory.createCodecs(prefix.getFields());
		for(JdbcFieldCodec<?,?> codec : codecs){
			Field<?> field = codec.getField();
			if(numFullFieldsFinished >= numNonNullFields) {
				break;
			}
			if(field.getValue()==null) {
				throw new DataAccessException("Prefix query on "+
						prefix.getClass()+" cannot contain intermediate nulls.");
			}
			if(numFullFieldsFinished > 0){ 
				sql.append(" and "); 
			}
			boolean lastNonNullField = (numFullFieldsFinished == numNonNullFields-1);
			boolean stringField = !(field instanceof BasePrimitiveField<?>);
			boolean doPrefixMatchOnField = wildcardLastField && lastNonNullField && stringField;
			if(doPrefixMatchOnField){
				String s = codec.getSqlEscaped();
				String sqlEscapedWithWildcard = s.substring(0, s.length()-1) + "%'";
				sql.append(field.getColumnName()+" like "+sqlEscapedWithWildcard);
			}else{
				sql.append(codec.getSqlNameValuePairEscaped());
			}
			++numFullFieldsFinished;
		}
	}
	
	public static boolean needsRangeWhereClause(FieldSet<?> start, FieldSet<?> end){
		if(start==null && end==null){ return false; }
		if(end!=null){ return true; }
		List<Field<?>> startFields = DrListTool.createArrayList(start.getFields());
		int numNonNullStartFields = FieldTool.countNonNullLeadingFields(startFields);
		if(numNonNullStartFields > 0){ return true; }
		return false; 
	}
	
	public static void addRangeWhereClause(JdbcFieldCodecFactory codecFactory, StringBuilder sql,
			FieldSet<?> start, boolean startInclusive, 
			FieldSet<?> end, boolean endInclusive){
				
		if(start != null && DrCollectionTool.notEmpty(start.getFields())){
			List<Field<?>> startFields = DrListTool.createArrayList(start.getFields());
			List<JdbcFieldCodec<?,?>> startCodecs = codecFactory.createCodecs(startFields);
			int numNonNullStartFields = FieldTool.countNonNullLeadingFields(startFields);
			if(numNonNullStartFields > 0){
				sql.append("(");
				for(int i=numNonNullStartFields; i > 0; --i){
					if(i<numNonNullStartFields){ sql.append(" or "); }
					sql.append("(");
					for(int j=0; j < i; ++j){
						if(j>0){ sql.append(" and "); }
						Field<?> startField = startFields.get(j);
						JdbcFieldCodec<?,?> startCodec = startCodecs.get(j);
						if(j < (i-1)){
							sql.append(startCodec.getSqlNameValuePairEscaped());
						}else{
							if(startInclusive && i==numNonNullStartFields){
								sql.append(startField.getColumnName()+">="+startCodec.getSqlEscaped());
							}else{
								sql.append(startField.getColumnName()+">"+startCodec.getSqlEscaped());
							}
						}
					}
					sql.append(")");
				}
				sql.append(")");
			}
		}
		
		if(start!=null && end!=null){ sql.append(" and "); }
		
//		select a, b, c, d from SortedBean where ((a>='alp')) and (a<='emu' and b is null and c is null and d is null
		
		if(end != null && DrCollectionTool.notEmpty(end.getFields())){
			List<Field<?>> endFields = DrListTool.createArrayList(end.getFields());
			List<JdbcFieldCodec<?,?>> endCodecs = codecFactory.createCodecs(endFields);
			int numNonNullEndFields = FieldTool.countNonNullLeadingFields(endFields);
			if(numNonNullEndFields > 0){
				sql.append("(");
				for(int i=0; i < numNonNullEndFields; ++i){
					if(i>0){ sql.append(" or "); }
					sql.append("(");
					for(int j=0; j <= i; ++j){
						if(j>0){ sql.append(" and "); }
						Field<?> endField = endFields.get(j);
						JdbcFieldCodec<?,?> endCodec = endCodecs.get(j);
						if(j==i){
							if(endInclusive && i==(numNonNullEndFields-1)){
								sql.append(endField.getColumnName()+"<="+endCodec.getSqlEscaped());
							}else{
								sql.append(endField.getColumnName()+"<"+endCodec.getSqlEscaped());
							}
						}else{
							sql.append(endCodec.getSqlNameValuePairEscaped());
						}
					}
					sql.append(")");
				}
				sql.append(")");
			}
		}
	}
	
	public static void addOrderByClause(StringBuilder sql, List<Field<?>> orderByFields){
		if(DrCollectionTool.isEmpty(orderByFields)){ return; }
		sql.append(" order by ");
		int counter = 0;
		for(Field<?> field : orderByFields){
			if(counter > 0){ sql.append(", "); }
			sql.append(field.getColumnName()+" asc");
			++counter;
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
