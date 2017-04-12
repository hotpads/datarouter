package com.hotpads.datarouter.client.imp.jdbc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSetCollationOpt;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
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

	public static final Optional<String> PRIMARY_KEY_INDEX_NAME_OPTIONAL = Optional.of("PRIMARY");

	/*************************** primary methods ***************************************/

	public static String getCount(JdbcFieldCodecFactory codecFactory, String tableName,
			Collection<? extends FieldSet<?>> keys, MySqlCharacterSetCollationOpt characterSetCollation){
		checkTableName(tableName);
		StringBuilder sql = new StringBuilder();
		sql.append("select count(*) from " + tableName);
		if(keys.size() > 0){
			sql.append(" where ");
			appendWhereClauseDisjunction(codecFactory, sql, keys, characterSetCollation);
		}
		return sql.toString();
	}

	public static String getAll(Config config, String tableName, List<Field<?>> selectFields, String where,
			List<Field<?>> orderByFields){
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
			List<Field<?>> selectFields, Collection<? extends FieldSet<?>> keys,
			MySqlCharacterSetCollationOpt characterSetCollation){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		if(DrCollectionTool.notEmpty(keys)){
			sql.append(" where ");
			appendWhereClauseDisjunction(codecFactory, sql, keys, characterSetCollation);
		}
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}

	public static String deleteMulti(JdbcFieldCodecFactory codecFactory, Config config, String tableName,
			Collection<? extends FieldSet<?>> keys, MySqlCharacterSetCollationOpt characterSetCollation){
		StringBuilder sql = new StringBuilder();
		addDeleteFromClause(sql, tableName);
		if(DrCollectionTool.notEmpty(keys)){
			sql.append(" where ");
			appendWhereClauseDisjunction(codecFactory, sql, keys, characterSetCollation);
		}
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}

	public static String getWithPrefixes(JdbcFieldCodecFactory codecFactory, Config config, String tableName,
			List<Field<?>> selectFields, Collection<? extends FieldSet<?>> keys, boolean wildcardLastField,
			List<Field<?>> orderByFields, MySqlCharacterSetCollationOpt characterSetCollation){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		addFullPrefixWhereClauseDisjunction(codecFactory, sql, keys, wildcardLastField, characterSetCollation);
		addOrderByClause(sql, orderByFields);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}

	public static <T extends FieldSet<T>> String getInRanges(JdbcFieldCodecFactory codecFactory, Config config,
			String tableName, List<Field<?>> selectFields, Iterable<Range<T>> ranges, List<Field<?>> orderByFields,
			Optional<String> indexName, MySqlCharacterSetCollationOpt characterSetCollation){
		StringBuilder sql = new StringBuilder();
		addSelectFromClause(sql, tableName, selectFields);
		indexName.ifPresent(name -> addForceIndexClause(sql, name));
		boolean hasWhereClause = false;
		for(Range<T> range : ranges){
			if(needsRangeWhereClause(range.getStart(), range.getEnd())){
				if(hasWhereClause){
					sql.append(" or ");
				}else{
					sql.append(" where ");
					hasWhereClause = true;
				}
				addRangeWhereClause(codecFactory, sql, range, characterSetCollation);
			}
		}
		addOrderByClause(sql, orderByFields);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}

	/*************************** secondary methods ***************************************/

	public static void addForceIndexClause(StringBuilder sql, String indexName){
		sql.append(" force index (").append(indexName).append(")");
	}

	public static void addSelectFromClause(StringBuilder sql, String tableName, List<Field<?>> selectFields){
		checkTableName(tableName);
		sql.append("select ");
		Preconditions.checkArgument(DrCollectionTool.notEmpty(selectFields), "Please provide select fields");
		FieldTool.appendCsvColumnNames(sql, selectFields);
		sql.append(" from " + tableName);
	}

	private static void addDeleteFromClause(StringBuilder sql, String tableName){
		checkTableName(tableName);
		sql.append("delete from " + tableName);
	}

	private static void addWhereClauseWithWhere(StringBuilder sql, String where){
		if(DrStringTool.notEmpty(where)){
			sql.append(" where " + where);
		}
	}

	private static void addFullPrefixWhereClauseDisjunction(JdbcFieldCodecFactory codecFactory, StringBuilder sql,
			Collection<? extends FieldSet<?>> keys, boolean wildcardLastField,
			MySqlCharacterSetCollationOpt characterSetCollation){
		if(DrCollectionTool.isEmpty(keys)){
			return;
		}
		StringBuilder prefixWhereClauseDisjunction = getPrefixWhereClauseDisjunction(codecFactory, keys,
				wildcardLastField, characterSetCollation);
		if(prefixWhereClauseDisjunction.length() > 0){
			sql.append(" where ");
			sql.append(prefixWhereClauseDisjunction);
		}
	}

	private static StringBuilder getPrefixWhereClauseDisjunction(JdbcFieldCodecFactory codecFactory,
			Collection<? extends FieldSet<?>> keys, boolean wildcardLastField,
			MySqlCharacterSetCollationOpt characterSetCollation){
		int counter = 0;
		StringBuilder sql = new StringBuilder();
		for(FieldSet<?> key : keys){
			if(counter > 0){
				sql.append(" or ");
			}
			addPrefixWhereClause(codecFactory, sql, key, wildcardLastField, characterSetCollation);
			++counter;
		}
		return sql;
	}

	private static void addPrefixWhereClause(JdbcFieldCodecFactory codecFactory, StringBuilder sql, FieldSet<?> prefix,
			boolean wildcardLastField, MySqlCharacterSetCollationOpt characterSetCollation){
		int numNonNullFields = FieldSetTool.getNumNonNullLeadingFields(prefix);
		if(numNonNullFields == 0){
			return;
		}
		int numFullFieldsFinished = 0;
		List<JdbcFieldCodec<?,?>> codecs = codecFactory.createCodecs(prefix.getFields());
		for(JdbcFieldCodec<?,?> codec : codecs){
			Field<?> field = codec.getField();
			if(numFullFieldsFinished >= numNonNullFields){
				break;
			}
			if(numFullFieldsFinished > 0){
				sql.append(" and ");
			}
			boolean lastNonNullField = numFullFieldsFinished == numNonNullFields - 1;
			boolean stringField = !(field instanceof BasePrimitiveField<?>);
			boolean doPrefixMatchOnField = wildcardLastField && lastNonNullField && stringField;
			if(doPrefixMatchOnField){
				String sqlEscaped = codec.getSqlEscaped();
				String sqlEscapedWithWildcard = sqlEscaped.substring(0, sqlEscaped.length() - 1) + "%'";
				sql.append(field.getKey().getColumnName() + " like " + sqlEscapedWithWildcard);
			}else{
				sql.append(getSqlNameValuePairEscaped(codec, characterSetCollation));
			}
			++numFullFieldsFinished;
		}
	}

	private static boolean needsRangeWhereClause(FieldSet<?> start, FieldSet<?> end){
		return start != null && FieldTool.countNonNullLeadingFields(start.getFields()) > 0
				|| end != null && FieldTool.countNonNullLeadingFields(end.getFields()) > 0;
	}

	private static boolean shouldIntroduceLiteral(JdbcFieldCodec<?,?> codec,
			MySqlCharacterSetCollationOpt characterSetCollation){
		if(codec.getField().getValue() == null){
			return false;//NULL is not introducible (this code is currently unreachable, but not necessarily forever.)
		}
		if(!codec.getSqlColumnDefinition(false).getType().isIntroducible()){
			return false;//column is not an introducible type
		}
		if(!characterSetCollation.getCharacterSetOpt().isPresent() && !characterSetCollation
				.getCollationOpt().isPresent()){
			return false;//expected column character set and collation not passed in, so can't introduce
		}
		boolean characterSetConnectionMismatch = characterSetCollation.getCharacterSetOpt().orElse(JdbcConnectionPool
				.CHARACTER_SET_CONNECTION) != JdbcConnectionPool.CHARACTER_SET_CONNECTION;
		boolean collationConnectionMismatch = characterSetCollation.getCollationOpt().orElse(JdbcConnectionPool
				.COLLATION_CONNECTION) != JdbcConnectionPool.COLLATION_CONNECTION;
		//literals only benefit from introducer if the column's settings differ from the connection's settings
		return characterSetConnectionMismatch || collationConnectionMismatch;
	}

	//TODO basically a substitute for codec.getSqlNameValuePairEscaped until column-level charset and collation are
	//available
	private static String getSqlNameValuePairEscaped(JdbcFieldCodec<?,?> codec,
			MySqlCharacterSetCollationOpt characterSetCollation){
		if(codec.getField().getValue() == null){
			return codec.getSqlNameValuePairEscaped();
		}
		return codec.getField().getKey().getColumnName() + "=" + getLiteral(codec, characterSetCollation);
	}

	//TODO basically a substitute for codec.getSqlEscaped until column-level charset and collation are available
	private static String getLiteral(JdbcFieldCodec<?,?> codec, MySqlCharacterSetCollationOpt characterSetCollation){
		if(!shouldIntroduceLiteral(codec, characterSetCollation)){
			return codec.getSqlEscaped();
		}
		StringBuilder introducedLiteral = new StringBuilder();
		if(characterSetCollation.getCharacterSetOpt().isPresent()){
			introducedLiteral.append("_").append(characterSetCollation.getCharacterSetOpt().get().name()).append(" ");
		}
		introducedLiteral.append(codec.getSqlEscaped());
		if(characterSetCollation.getCollationOpt().isPresent()){
			introducedLiteral.append(" COLLATE ").append(characterSetCollation.getCollationOpt().get().name());
		}
		return introducedLiteral.toString();
	}

	public static void addRangeWhereClause(JdbcFieldCodecFactory codecFactory, StringBuilder sql,
			Range<? extends FieldSet<?>> range, MySqlCharacterSetCollationOpt characterSetCollation){
		if(range.isEmpty()){
			sql.append("0");
			return;
		}

		boolean hasStart = false;

		List<Field<?>> startFields = null;
		int numNonNullStartFields = 0;
		List<JdbcFieldCodec<?,?>> startCodecs = null;
		if(range.getStart() != null){
			startFields = DrListTool.nullSafe(range.getStart().getFields());
			numNonNullStartFields = FieldTool.countNonNullLeadingFields(startFields);
			if(numNonNullStartFields > 0){
				startCodecs = codecFactory.createCodecs(startFields);
			}
		}

		List<Field<?>> endFields = null;
		int numNonNullEndFields = 0;
		List<JdbcFieldCodec<?,?>> endCodecs = null;
		if(range.getEnd() != null){
			endFields = DrListTool.nullSafe(range.getEnd().getFields());
			numNonNullEndFields = FieldTool.countNonNullLeadingFields(endFields);
			if(numNonNullEndFields > 0){
				endCodecs = codecFactory.createCodecs(endFields);
			}
		}

		int numEqualsLeadingFields = 0;
		if(range.getStart() != null && range.getEnd() != null){
			int numNonNullLeadingFields = Math.min(numNonNullStartFields, numNonNullEndFields);
			for(int i = 0; i < numNonNullLeadingFields; i++){
				if(startFields.get(i).getValue().equals(endFields.get(i).getValue())){
					if(i > 0){
						sql.append(" and ");
					}else{
						sql.append("(");
					}
					sql.append(startFields.get(i).getKey().getColumnName() + "=" + getLiteral(startCodecs.get(i),
							characterSetCollation));
					numEqualsLeadingFields++;
				}else{
					break;
				}
			}
		}

		if(range.getStart() != null && numNonNullStartFields > 0 && numNonNullStartFields > numEqualsLeadingFields){
			hasStart = true;
			if(numEqualsLeadingFields > 0){
				sql.append(" and ");
			}
			sql.append("(");
			for(int i = numNonNullStartFields; i > numEqualsLeadingFields; --i){
				if(i < numNonNullStartFields){
					sql.append(" or ");
				}
				sql.append("(");
				for(int j = numEqualsLeadingFields; j < i; ++j){
					if(j > numEqualsLeadingFields){
						sql.append(" and ");
					}
					Field<?> startField = startFields.get(j);
					JdbcFieldCodec<?,?> startCodec = startCodecs.get(j);
					if(j < i - 1){
						sql.append(getSqlNameValuePairEscaped(startCodec, characterSetCollation));
					}else{
						if(range.getStartInclusive() && i == numNonNullStartFields){
							sql.append(startField.getKey().getColumnName() + ">=");
						}else{
							sql.append(startField.getKey().getColumnName() + ">");
						}
						sql.append(getLiteral(startCodec, characterSetCollation));
					}
				}
				sql.append(")");
			}
			sql.append(")");
		}

		if(range.getEnd() != null && numNonNullEndFields > 0 && numNonNullEndFields > numEqualsLeadingFields){
			if(numEqualsLeadingFields > 0 || hasStart){
				sql.append(" and ");
			}
			sql.append("(");
			for(int i = numEqualsLeadingFields; i < numNonNullEndFields; ++i){
				if(i > numEqualsLeadingFields){
					sql.append(" or ");
				}
				sql.append("(");
				for(int j = numEqualsLeadingFields; j <= i; ++j){
					if(j > numEqualsLeadingFields){
						sql.append(" and ");
					}
					Field<?> endField = endFields.get(j);
					JdbcFieldCodec<?,?> endCodec = endCodecs.get(j);
					if(j == i){
						if(range.getEndInclusive() && i == numNonNullEndFields - 1){
							sql.append(endField.getKey().getColumnName() + "<=");
						}else{
							sql.append(endField.getKey().getColumnName() + "<");
						}
						sql.append(getLiteral(endCodec, characterSetCollation));
					}else{
						sql.append(getSqlNameValuePairEscaped(endCodec, characterSetCollation));
					}
				}
				sql.append(")");
			}
			sql.append(")");
		}

		if(numEqualsLeadingFields > 0){
			sql.append(")");
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
			sql.append(field.getKey().getColumnName() + " asc");
			++counter;
		}
	}

	public static void addLimitOffsetClause(StringBuilder sql, Config config){
		config = Config.nullSafe(config);

		if(config.getLimit() != null && config.getOffset() != null){
			sql.append(" limit " + config.getOffset() + ", " + config.getLimit());
		}else if(config.getLimit() != null){
			sql.append(" limit " + config.getLimit());
		}else if(config.getOffset() != null){
			sql.append(" limit " + config.getOffset() + ", " + Integer.MAX_VALUE);// stupid mysql syntax
		}
	}

	public static List<String> getSqlNameValuePairsEscaped(JdbcFieldCodecFactory codecFactory,
			Collection<Field<?>> fields, MySqlCharacterSetCollationOpt characterSetCollation){
		List<String> sql = new ArrayList<>();
		for(JdbcFieldCodec<?,?> codec : codecFactory.createCodecs(fields)){
			sql.add(getSqlNameValuePairEscaped(codec, characterSetCollation));
		}
		return sql;
	}

	public static String getSqlNameValuePairsEscapedConjunction(JdbcFieldCodecFactory codecFactory,
			Collection<Field<?>> fields, MySqlCharacterSetCollationOpt characterSetCollation){
		List<String> nameValuePairs = getSqlNameValuePairsEscaped(codecFactory, fields, characterSetCollation);
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
			sb.append(field.getKey().getColumnName() + "=?");
			++appended;
		}
	}

	public static void appendWhereClauseDisjunction(JdbcFieldCodecFactory codecFactory, StringBuilder sql,
			Collection<? extends FieldSet<?>> fieldSets, MySqlCharacterSetCollationOpt characterSetCollation){
		int counter = 0;
		for(FieldSet<?> fieldSet : DrIterableTool.nullSafe(fieldSets)){
			if(counter > 0){
				sql.append(" or ");
			}
			sql.append(getSqlNameValuePairsEscapedConjunction(codecFactory, fieldSet.getFields(),
					characterSetCollation));
			++counter;
		}
	}

	// Preconditions

	public static void checkTableName(String tableName){
		if(DrStringTool.isEmpty(tableName)){
			throw new IllegalArgumentException("Please provide a table name");
		}
	}
}
