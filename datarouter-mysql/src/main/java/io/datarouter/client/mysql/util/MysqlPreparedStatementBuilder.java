/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.mysql.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptions;
import io.datarouter.client.mysql.field.MysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSet;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.storage.config.Config;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.tuple.Range;

@Singleton
public class MysqlPreparedStatementBuilder{
	private static final Logger logger = LoggerFactory.getLogger(MysqlPreparedStatementBuilder.class);

	@Inject
	private MysqlFieldCodecFactory codecFactory;

	public DatarouterMysqlStatement getWithPrefixes(Config config, String tableName, String indexName,
			List<Field<?>> selectFields, Collection<? extends FieldSet<?>> keys, List<Field<?>> orderByFields,
			MysqlLiveTableOptions mysqlTableOptions){
		DatarouterMysqlStatement statement = new DatarouterMysqlStatement();
		SqlBuilder.addSelectFromClause(statement.getSql(), tableName, selectFields);
		SqlBuilder.addForceIndexClause(statement.getSql(), indexName);
		appendPrefixWhereClauseDisjunction(statement, keys, mysqlTableOptions);
		SqlBuilder.addOrderByClause(statement.getSql(), orderByFields);
		SqlBuilder.addLimitOffsetClause(statement.getSql(), config);
		return statement;
	}

	public DatarouterMysqlStatement deleteMulti(Config config, String tableName,
			Collection<? extends FieldSet<?>> keys, MysqlLiveTableOptions mysqlTableOptions){
		DatarouterMysqlStatement statement = new DatarouterMysqlStatement();
		SqlBuilder.addDeleteFromClause(statement.getSql(), tableName);
		appendWhereClauseDisjunction(statement, keys, mysqlTableOptions);
		SqlBuilder.addLimitOffsetClause(statement.getSql(), config);
		return statement;
	}

	public DatarouterMysqlStatement select(String tableName, List<Field<?>> selectFields){
		DatarouterMysqlStatement statement = new DatarouterMysqlStatement();
		SqlBuilder.addSelectFromClause(statement.getSql(), tableName, selectFields);
		return statement;
	}

	public DatarouterMysqlStatement getMulti(Config config, String tableName, List<Field<?>> selectFields,
			Collection<? extends FieldSet<?>> keys, String indexName, MysqlLiveTableOptions mysqlTableOptions){
		DatarouterMysqlStatement statement = select(tableName, selectFields);
		SqlBuilder.addForceIndexClause(statement.getSql(), indexName);
		appendWhereClauseDisjunction(statement, keys, mysqlTableOptions);
		SqlBuilder.addLimitOffsetClause(statement.getSql(), config);
		return statement;
	}

	public <T extends FieldSet<T>> DatarouterMysqlStatement getInRanges(Config config, String tableName,
			List<Field<?>> selectFields, Iterable<Range<T>> ranges, List<Field<?>> orderByFields, String indexName,
			MysqlLiveTableOptions mysqlTableOptions){
		DatarouterMysqlStatement statement = new DatarouterMysqlStatement();
		SqlBuilder.addSelectFromClause(statement.getSql(), tableName, selectFields);
		SqlBuilder.addForceIndexClause(statement.getSql(), indexName);
		boolean hasWhereClause = false;
		for(Range<T> range : ranges){
			if(SqlBuilder.needsRangeWhereClause(range.getStart(), range.getEnd())){
				if(hasWhereClause){
					statement.append(" or ");
				}else{
					statement.append(" where ");
					hasWhereClause = true;
				}
				addRangeWhereClause(statement, range, mysqlTableOptions);
			}
		}
		SqlBuilder.addOrderByClause(statement.getSql(), orderByFields);
		SqlBuilder.addLimitOffsetClause(statement.getSql(), config);
		return statement;
	}

	public DatarouterMysqlStatement update(String tableName, List<Field<?>> fieldsToUpdate,
			List<? extends FieldSet<?>> keys, MysqlLiveTableOptions mysqlTableOptions){
		DatarouterMysqlStatement statement = new DatarouterMysqlStatement();
		SqlBuilder.addUpdateClause(statement.getSql(), tableName);
		for(Iterator<Field<?>> iterator = fieldsToUpdate.iterator(); iterator.hasNext();){
			Field<?> field = iterator.next();
			MysqlFieldCodec<?> codec = codecFactory.createCodec(field);
			statement.append(field.getKey().getColumnName()).append("=?", codec::setPreparedStatementValue);
			if(iterator.hasNext()){
				statement.append(",");
			}
		}
		appendWhereClauseDisjunction(statement, keys, mysqlTableOptions);
		return statement;
	}

	public DatarouterMysqlStatement insert(String tableName, List<List<Field<?>>> databeansFields, boolean ignore){
		DatarouterMysqlStatement statement = new DatarouterMysqlStatement();
		statement.append("insert");
		if(ignore){
			statement.append(" ignore");
		}
		statement.append(" into ").append(tableName).append(" (");
		FieldTool.appendCsvColumnNames(statement.getSql(), CollectionTool.findFirst(databeansFields).get());
		statement.append(") values ");
		for(Iterator<List<Field<?>>> databeansIterator = databeansFields.iterator(); databeansIterator.hasNext();){
			statement.append("(");
			appendCsvFields(statement, databeansIterator.next());
			statement.append(")");
			if(databeansIterator.hasNext()){
				statement.append(",");
			}
		}
		return statement;
	}

	public void appendWhereClauseDisjunction(DatarouterMysqlStatement statement,
			Collection<? extends FieldSet<?>> fieldSets, MysqlLiveTableOptions mysqlTableOptions){
		if(CollectionTool.isEmpty(fieldSets)){
			return;
		}
		statement.append(" where ");
		for(Iterator<? extends FieldSet<?>> iterator = fieldSets.iterator(); iterator.hasNext();){
			getSqlNameValuePairsEscapedConjunction(statement, iterator.next().getFields(), mysqlTableOptions);
			if(iterator.hasNext()){
				statement.append(" or ");
			}
		}
	}

	public void appendSqlNameValue(DatarouterMysqlStatement statement, Field<?> field,
			MysqlLiveTableOptions mysqlTableOptions){
		getSqlNameValueWithOperator(statement, field, mysqlTableOptions, "=");
	}

	public void appendCsvFields(DatarouterMysqlStatement statement, List<Field<?>> fields){
		for(Iterator<? extends Field<?>> iterator = fields.iterator(); iterator.hasNext();){
			MysqlFieldCodec<?> codec = codecFactory.createCodec(iterator.next());
			statement.append(codec.getSqlParameter(), codec::setPreparedStatementValue);
			if(iterator.hasNext()){
				statement.append(",");
			}
		}
	}

	public void appendPrefixWhereClauseDisjunction(DatarouterMysqlStatement statement,
			Collection<? extends FieldSet<?>> keys, MysqlLiveTableOptions mysqlTableOptions){
		int counter = 0;
		boolean shouldAppendWhere = true;
		for(FieldSet<?> key : keys){
			if(counter > 0){
				statement.append(" or ");
			}
			shouldAppendWhere = addPrefixWhereClause(statement, key, mysqlTableOptions, shouldAppendWhere);
			++counter;
		}
	}

	private boolean addPrefixWhereClause(DatarouterMysqlStatement statement, FieldSet<?> prefix,
			MysqlLiveTableOptions mysqlTableOptions, boolean shouldAppendWhere){
		int numNonNullFields = FieldSetTool.getNumNonNullLeadingFields(prefix);
		if(numNonNullFields == 0){
			return true;
		}
		int numFullFieldsFinished = 0;
		for(Field<?> field : prefix.getFields()){
			if(numFullFieldsFinished >= numNonNullFields){
				break;
			}
			if(numFullFieldsFinished > 0){
				statement.append(" and ");
			}else if(shouldAppendWhere){
				statement.append(" where ");
				shouldAppendWhere = false;
			}
			appendSqlNameValue(statement, field, mysqlTableOptions);
			++numFullFieldsFinished;
		}
		return shouldAppendWhere;
	}

	private void getSqlNameValuePairsEscapedConjunction(DatarouterMysqlStatement statement,
			Collection<Field<?>> fields, MysqlLiveTableOptions mysqlTableOptions){
		for(Iterator<Field<?>> iterator = fields.iterator(); iterator.hasNext();){
			appendSqlNameValue(statement, iterator.next(), mysqlTableOptions);
			if(iterator.hasNext()){
				statement.append(" and ");
			}
		}
	}

	private void getSqlNameValueWithOperator(DatarouterMysqlStatement statement, Field<?> field,
			MysqlLiveTableOptions mysqlTableOptions, String operator){
		statement.append(field.getKey().getColumnName());
		if(field.getValue() == null){
			throw new RuntimeException(field.getKey().getColumnName() + " should not be null, current sql is "
					+ statement.getSql());
		}
		MysqlFieldCodec<?> codec = codecFactory.createCodec(field);
		statement.append(operator)
				.append(codec.getIntroducedParameter(mysqlTableOptions), codec::setPreparedStatementValue);
	}

	private void addRangeWhereClause(DatarouterMysqlStatement statement, Range<? extends FieldSet<?>> range,
			MysqlLiveTableOptions mysqlTableOptions){
		if(range.isEmpty()){
			statement.append("0");
			logger.warn("range should probably not be empty at this point {} {}", range, statement.getSql(),
					new Exception());
			return;
		}

		boolean hasStart = false;

		List<Field<?>> startFields = null;
		int numNonNullStartFields = 0;
		if(range.getStart() != null){
			startFields = ListTool.nullSafe(range.getStart().getFields());
			numNonNullStartFields = FieldTool.countNonNullLeadingFields(startFields);
		}

		List<Field<?>> endFields = null;
		int numNonNullEndFields = 0;
		if(range.getEnd() != null){
			endFields = ListTool.nullSafe(range.getEnd().getFields());
			numNonNullEndFields = FieldTool.countNonNullLeadingFields(endFields);
		}

		int numEqualsLeadingFields = 0;
		if(range.getStart() != null && range.getEnd() != null){
			int numNonNullLeadingFields = Math.min(numNonNullStartFields, numNonNullEndFields);
			for(int i = 0; i < numNonNullLeadingFields; i++){
				if(startFields.get(i).getValue().equals(endFields.get(i).getValue())){
					if(i > 0){
						statement.append(" and ");
					}else{
						statement.append("(");
					}
					appendSqlNameValue(statement, startFields.get(i), mysqlTableOptions);
					numEqualsLeadingFields++;
				}else{
					break;
				}
			}
		}

		if(range.getStart() != null
				&& numNonNullStartFields > 0
				&& numNonNullStartFields > numEqualsLeadingFields){
			hasStart = true;
			if(numEqualsLeadingFields > 0){
				statement.append(" and ");
			}
			statement.append("(");
			for(int i = numNonNullStartFields; i > numEqualsLeadingFields; --i){
				if(i < numNonNullStartFields){
					statement.append(" or ");
				}
				statement.append("(");
				for(int j = numEqualsLeadingFields; j < i; ++j){
					if(j > numEqualsLeadingFields){
						statement.append(" and ");
					}
					Field<?> startField = startFields.get(j);
					String operator;
					if(j < i - 1){
						operator = "=";
					}else if(range.getStartInclusive() && i == numNonNullStartFields){
						operator = ">=";
					}else{
						operator = ">";
					}
					getSqlNameValueWithOperator(statement, startField, mysqlTableOptions, operator);
				}
				statement.append(")");
			}
			statement.append(")");
		}

		if(range.getEnd() != null && numNonNullEndFields > 0 && numNonNullEndFields > numEqualsLeadingFields){
			if(numEqualsLeadingFields > 0 || hasStart){
				statement.append(" and ");
			}
			statement.append("(");
			for(int i = numEqualsLeadingFields; i < numNonNullEndFields; ++i){
				if(i > numEqualsLeadingFields){
					statement.append(" or ");
				}
				statement.append("(");
				for(int j = numEqualsLeadingFields; j <= i; ++j){
					if(j > numEqualsLeadingFields){
						statement.append(" and ");
					}
					Field<?> endField = endFields.get(j);
					String operator;
					if(j != i){
						operator = "=";
					}else if(range.getEndInclusive() && i == numNonNullEndFields - 1){
						operator = "<=";
					}else{
						operator = "<";
					}
					getSqlNameValueWithOperator(statement, endField, mysqlTableOptions, operator);
				}
				statement.append(")");
			}
			statement.append(")");
		}

		if(numEqualsLeadingFields > 0){
			statement.append(")");
		}
	}

}
