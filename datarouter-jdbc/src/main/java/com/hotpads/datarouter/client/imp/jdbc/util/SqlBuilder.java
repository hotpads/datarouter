package com.hotpads.datarouter.client.imp.jdbc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.config.Config;
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

	public static String getCount(JdbcFieldCodecFactory codecFactory, String tableName,
			Collection<? extends FieldSet<?>> keys){
		checkTableName(tableName);
		StringBuilder sql = new StringBuilder();
		sql.append("select count(*) from " + tableName);
		if (keys.size() > 0) {
			sql.append(" where ");
			appendWhereClauseDisjunction(codecFactory, sql, keys);
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

	public static String getMulti(JdbcFieldCodecFactory codecFactory, Config config, String tableName,
			List<Field<?>> selectFields, Collection<? extends FieldSet<?>> keys){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		if(DrCollectionTool.notEmpty(keys)){
			sql.append(" where ");
			appendWhereClauseDisjunction(codecFactory, sql, keys);
		}
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}

	public static String deleteMulti(JdbcFieldCodecFactory codecFactory, Config config, String tableName,
			Collection<? extends FieldSet<?>> keys){
		StringBuilder sql = new StringBuilder();
		addDeleteFromClause(sql, tableName);
		if(DrCollectionTool.notEmpty(keys)){
			sql.append(" where ");
			appendWhereClauseDisjunction(codecFactory, sql, keys);
		}
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}

	public static String getWithPrefixes(JdbcFieldCodecFactory codecFactory, Config config, String tableName,
			List<Field<?>> selectFields, Collection<? extends FieldSet<?>> keys, boolean wildcardLastField,
			List<Field<?>> orderByFields){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		addFullPrefixWhereClauseDisjunction(codecFactory, sql, keys, wildcardLastField);
		addOrderByClause(sql, orderByFields);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}

	public static String deleteWithPrefixes(JdbcFieldCodecFactory codecFactory, Config config, String tableName,
			Collection<? extends FieldSet<?>> keys, boolean wildcardLastField){
		StringBuilder sql = new StringBuilder();
		addDeleteFromClause(sql, tableName);
		addFullPrefixWhereClauseDisjunction(codecFactory, sql, keys, wildcardLastField);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}

	public static <T extends FieldSet<T>> String getInRanges(JdbcFieldCodecFactory codecFactory, Config config,
			String tableName, List<Field<?>> selectFields, Iterable<Range<T>> ranges, List<Field<?>> orderByFields){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		boolean hasWhereClause = false;
		for(Range<T> range : ranges){
			if(needsRangeWhereClause(range.getStart(), range.getEnd())){
				if(hasWhereClause){
					sql.append(" or ");
				}else{
					sql.append(" where ");
					hasWhereClause = true;
				}
				addRangeWhereClause(codecFactory, sql, range);
			}
		}
		addOrderByClause(sql, orderByFields);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}

	public static <T extends FieldSet<T>> String getInRange(JdbcFieldCodecFactory codecFactory, Config config,
			String tableName, List<Field<?>> selectFields, Range<T> range, List<Field<?>> orderByFields){
		return getInRanges(codecFactory, config, tableName, selectFields, Arrays.asList(range), orderByFields);
	}

	/*************************** secondary methods ***************************************/

	public static void addSelectFromClause(StringBuilder sql, String tableName, List<Field<?>> selectFields){
		checkTableName(tableName);
		sql.append("select ");
		Preconditions.checkArgument(DrCollectionTool.notEmpty(selectFields), "Please provide select fields");
		FieldTool.appendCsvColumnNames(sql, selectFields);
		sql.append(" from "+tableName);
	}

	private static void addDeleteFromClause(StringBuilder sql, String tableName){
		checkTableName(tableName);
		sql.append("delete from "+tableName);
	}

	private static void addWhereClauseWithWhere(StringBuilder sql, String where){
		if(DrStringTool.notEmpty(where)){
			sql.append(" where "+where);
		}
	}

	private static void addFullPrefixWhereClauseDisjunction(JdbcFieldCodecFactory codecFactory, StringBuilder sql,
			Collection<? extends FieldSet<?>> keys, boolean wildcardLastField){
		if(DrCollectionTool.isEmpty(keys)){
			return;
		}
		StringBuilder prefixWhereClauseDisjunction = getPrefixWhereClauseDisjunction(codecFactory, keys,
				wildcardLastField);
		if(prefixWhereClauseDisjunction.length() > 0){
			sql.append(" where ");
			sql.append(prefixWhereClauseDisjunction);
		}
	}

	private static StringBuilder getPrefixWhereClauseDisjunction(JdbcFieldCodecFactory codecFactory,
			Collection<? extends FieldSet<?>> keys, boolean wildcardLastField){
		int counter = 0;
		StringBuilder sql = new StringBuilder();
		for(FieldSet<?> key : keys){
			if(counter>0){
				sql.append(" or ");
			}
			addPrefixWhereClause(codecFactory, sql, key, wildcardLastField);
			++counter;
		}
		return sql;
	}

	private static void addPrefixWhereClause(JdbcFieldCodecFactory codecFactory, StringBuilder sql, FieldSet<?> prefix,
			boolean wildcardLastField){
		int numNonNullFields = FieldSetTool.getNumNonNullLeadingFields(prefix);
		if(numNonNullFields==0){
			return;
		}
		int numFullFieldsFinished = 0;
		List<JdbcFieldCodec<?,?>> codecs = codecFactory.createCodecs(prefix.getFields());
		for(JdbcFieldCodec<?,?> codec : codecs){
			Field<?> field = codec.getField();
			if(numFullFieldsFinished >= numNonNullFields) {
				break;
			}
			if(numFullFieldsFinished > 0){
				sql.append(" and ");
			}
			boolean lastNonNullField = numFullFieldsFinished == numNonNullFields-1;
			boolean stringField = !(field instanceof BasePrimitiveField<?>);
			boolean doPrefixMatchOnField = wildcardLastField && lastNonNullField && stringField;
			if(doPrefixMatchOnField){
				String sqlEscaped = codec.getSqlEscaped();
				String sqlEscapedWithWildcard = sqlEscaped.substring(0, sqlEscaped.length()-1) + "%'";
				sql.append(field.getKey().getColumnName()+" like "+sqlEscapedWithWildcard);
			}else{
				sql.append(codec.getSqlNameValuePairEscaped());
			}
			++numFullFieldsFinished;
		}
	}

	private static boolean needsRangeWhereClause(FieldSet<?> start, FieldSet<?> end){
		return start != null && FieldTool.countNonNullLeadingFields(start.getFields()) > 0
				|| end != null && FieldTool.countNonNullLeadingFields(end.getFields()) > 0;
	}

	public static void addRangeWhereClause(JdbcFieldCodecFactory codecFactory, StringBuilder sql,
			Range<? extends FieldSet<?>> range){
		boolean hasStart = false;

		if(range.getStart() != null){
			List<Field<?>> startFields = DrListTool.nullSafe(range.getStart().getFields());
			int numNonNullStartFields = FieldTool.countNonNullLeadingFields(startFields);
			if(numNonNullStartFields > 0){
				hasStart = true;
				List<JdbcFieldCodec<?,?>> startCodecs = codecFactory.createCodecs(startFields);
				sql.append("(");
				for(int i=numNonNullStartFields; i > 0; --i){
					if(i<numNonNullStartFields){
						sql.append(" or ");
					}
					sql.append("(");
					for(int j=0; j < i; ++j){
						if(j>0){
							sql.append(" and ");
						}
						Field<?> startField = startFields.get(j);
						JdbcFieldCodec<?,?> startCodec = startCodecs.get(j);
						if(j < i-1){
							sql.append(startCodec.getSqlNameValuePairEscaped());
						}else{
							if(range.getStartInclusive() && i==numNonNullStartFields){
								sql.append(startField.getKey().getColumnName()+">="+startCodec.getSqlEscaped());
							}else{
								sql.append(startField.getKey().getColumnName()+">"+startCodec.getSqlEscaped());
							}
						}
					}
					sql.append(")");
				}
				sql.append(")");
			}
		}

//		select a, b, c, d from SortedBean where ((a>='alp')) and (a<='emu' and b is null and c is null and d is null

		if(range.getEnd() != null){
			List<Field<?>> endFields = DrListTool.nullSafe(range.getEnd().getFields());
			int numNonNullEndFields = FieldTool.countNonNullLeadingFields(endFields);
			if(numNonNullEndFields > 0){
				List<JdbcFieldCodec<?,?>> endCodecs = codecFactory.createCodecs(endFields);
				if(hasStart){
					sql.append(" and ");
				}
				sql.append("(");
				for(int i=0; i < numNonNullEndFields; ++i){
					if(i>0){
						sql.append(" or ");
					}
					sql.append("(");
					for(int j=0; j <= i; ++j){
						if(j>0){
							sql.append(" and ");
						}
						Field<?> endField = endFields.get(j);
						JdbcFieldCodec<?,?> endCodec = endCodecs.get(j);
						if(j==i){
							if(range.getEndInclusive() && i==numNonNullEndFields-1){
								sql.append(endField.getKey().getColumnName()+"<="+endCodec.getSqlEscaped());
							}else{
								sql.append(endField.getKey().getColumnName()+"<"+endCodec.getSqlEscaped());
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
		if(DrCollectionTool.isEmpty(orderByFields)){
			return;
		}
		sql.append(" order by ");
		int counter = 0;
		for(Field<?> field : orderByFields){
			if(counter > 0){
				sql.append(", ");
			}
			sql.append(field.getKey().getColumnName()+" asc");
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


	/************** methods originaly in FieldTool ***********************/

	public static List<String> getSqlNameValuePairsEscaped(JdbcFieldCodecFactory codecFactory,
			Collection<Field<?>> fields){
		List<String> sql = new ArrayList<>();
		for(JdbcFieldCodec<?,?> codec : codecFactory.createCodecs(fields)){
			sql.add(codec.getSqlNameValuePairEscaped());
		}
		return sql;
	}

	public static String getSqlNameValuePairsEscapedConjunction(JdbcFieldCodecFactory codecFactory,
			Collection<Field<?>> fields){
		List<String> nameValuePairs = getSqlNameValuePairsEscaped(codecFactory, fields);
		StringBuilder sb = new StringBuilder();
		int numAppended = 0;
		for(String nameValuePair : nameValuePairs){
			if(numAppended > 0){
				sb.append(" and ");
			}
			sb.append(nameValuePair);
			++numAppended;
		}
		return sb.toString();
	}

	public static void appendSqlUpdateClauses(StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			if(appended > 0){
				sb.append(",");
			}
			sb.append(field.getKey().getColumnName()+"=?");
			++appended;
		}
	}


	/******************** methods originally in FieldSetTool ***********************/

	public static void appendWhereClauseDisjunction(JdbcFieldCodecFactory codecFactory, StringBuilder sql,
			Collection<? extends FieldSet<?>> fieldSets){
		int counter = 0;
		for(FieldSet<?> fieldSet : DrIterableTool.nullSafe(fieldSets)){
			if(counter > 0){
				sql.append(" or ");
			}
			//heavy on parenthesis.  optimize later
			sql.append(getSqlNameValuePairsEscapedConjunction(codecFactory, fieldSet.getFields()));
			++counter;
		}
	}

	//Preconditions

	public static void checkTableName(String tableName){
		if(DrStringTool.isEmpty(tableName)){
			throw new IllegalArgumentException("Please provide a table name");
		}
	}

}
