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
package io.datarouter.storage.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSet;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.storage.config.Config;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.tuple.Range;

public abstract class Sql<C,P,Q extends Sql<C,P,Q>>{
	private static final Logger logger = LoggerFactory.getLogger(Sql.class);

	protected final Q implementation;
	protected final StringBuilder sqlBuilder;
	protected final ArrayList<BiConsumer<P,Integer>> parameterSetters;

	protected Sql(Class<Q> subclass){
		this.implementation = subclass.cast(this);
		this.sqlBuilder = new StringBuilder();
		this.parameterSetters = new ArrayList<>();
	}

	public abstract Q addLimitOffsetClause(Config config);

	public abstract Q addSqlNameValueWithOperator(Field<?> field, String operator, boolean rejectNulls);

	public abstract Q appendColumnEqualsValueParameter(Field<?> field);

	public abstract P prepare(C connection);

	public Q appendSqlNameValue(Field<?> field, boolean rejectNulls){
		return addSqlNameValueWithOperator(field, "=", rejectNulls);
	}

	public Q append(String value){
		sqlBuilder.append(value);
		return implementation;
	}

	public Q appendParameter(String value, BiConsumer<P,Integer> parameterSetter){
		sqlBuilder.append(value);
		parameterSetters.add(parameterSetter);
		return implementation;
	}

	public Q insert(String tableName, List<List<Field<?>>> databeans, boolean ignore){
		append("insert");
		if(ignore){
			append(" ignore");
		}
		append(" into ");
		append(tableName);
		append(" (");
		FieldTool.appendCsvColumnNames(sqlBuilder, CollectionTool.findFirst(databeans).get());
		append(") values ");
		boolean didOneDatabean = false;
		for(List<Field<?>> databeanFields : databeans){
			if(didOneDatabean){
				append(", ");
			}
			append("(");
			boolean didOneField = false;
			for(Field<?> field : databeanFields){
				if(didOneField){
					append(", ");
				}
				appendColumnEqualsValueParameter(field);
				didOneField = true;
			}
			append(")");
			didOneDatabean = true;
		}
		return implementation;
	}

	public Q update(String tableName, List<Field<?>> fieldsToUpdate, List<? extends FieldSet<?>> keys){
		addUpdateClause(tableName);
		boolean didOne = false;
		for(Field<?> field : fieldsToUpdate){
			if(didOne){
				append(", ");
			}
			appendSqlNameValue(field, false);//TODO rejectNulls=true and remove that option
			didOne = true;
		}
		appendWhereClauseDisjunction(keys);
		return implementation;
	}

	public Q deleteMulti(String tableName, Config config, Collection<? extends FieldSet<?>> keys){
		addDeleteFromClause(tableName);
		appendWhereClauseDisjunction(keys);
		addLimitOffsetClause(config);
		return implementation;
	}

	public Q getMulti(
			String tableName,
			Config config,
			List<Field<?>> selectFields,
			Collection<? extends FieldSet<?>> keys,
			String indexName){
		addSelectFromClause(tableName, selectFields);
		addForceIndexClause(indexName);
		appendWhereClauseDisjunction(keys);
		addLimitOffsetClause(config);
		return implementation;
	}

	public Q getWithPrefixes(
			String tableName,
			Config config,
			String indexName,
			List<Field<?>> selectFields,
			Collection<? extends FieldSet<?>> keys,
			List<Field<?>> orderByFields){
		addSelectFromClause(tableName, selectFields);
		addForceIndexClause(indexName);
		appendPrefixWhereClauseDisjunction(keys);
		addOrderByClause(orderByFields);
		addLimitOffsetClause(config);
		return implementation;
	}

	public <T extends FieldSet<T>> Q getInRanges(
			String tableName,
			Config config,
			List<Field<?>> selectFields,
			Iterable<Range<T>> ranges,
			List<Field<?>> orderByFields,
			String indexName){
		addSelectFromClause(tableName, selectFields);
		addForceIndexClause(indexName);
		boolean hasWhereClause = false;
		for(Range<T> range : ranges){
			if(needsRangeWhereClause(range.getStart(), range.getEnd())){
				if(hasWhereClause){
					append(" or ");
				}else{
					append(" where ");
					hasWhereClause = true;
				}
				addRangeWhereClause(range);
			}
		}
		addOrderByClause(orderByFields);
		addLimitOffsetClause(config);
		return implementation;
	}

	public Q appendWhereClauseDisjunction(Collection<? extends FieldSet<?>> fieldSets){
		if(CollectionTool.nullSafeIsEmpty(fieldSets)){
			return implementation;
		}
		append(" where ");
		boolean didOne = false;
		for(FieldSet<?> fieldSet : fieldSets){
			if(didOne){
				append(" or ");
			}
			getSqlNameValuePairsEscapedConjunction(fieldSet.getFields());
			didOne = true;
		}
		return implementation;
	}

	private boolean addPrefixWhereClause(FieldSet<?> prefix, boolean shouldAppendWhere){
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
				append(" and ");
			}else if(shouldAppendWhere){
				append(" where ");
				shouldAppendWhere = false;
			}
			appendSqlNameValue(field, true);
			++numFullFieldsFinished;
		}
		return shouldAppendWhere;
	}

	private Q appendPrefixWhereClauseDisjunction(Collection<? extends FieldSet<?>> keys){
		boolean didOne = false;
		boolean shouldAppendWhere = true;
		for(FieldSet<?> key : keys){
			if(didOne){
				append(" or ");
			}
			shouldAppendWhere = addPrefixWhereClause(key, shouldAppendWhere);
			didOne = true;
		}
		return implementation;
	}

	private void getSqlNameValuePairsEscapedConjunction(Collection<Field<?>> fields){
		boolean didOne = false;
		for(Field<?> field : fields){
			if(didOne){
				append(" and ");
			}
			appendSqlNameValue(field, true);
			didOne = true;
		}
	}

	private Q addRangeWhereClause(Range<? extends FieldSet<?>> range){
		if(range.isEmpty()){
			append("0");
			logger.warn("range should probably not be empty at this point {} {}", range, sqlBuilder, new Exception());
			return implementation;
		}
		boolean hasStart = false;

		List<Field<?>> startFields = null;
		int numNonNullStartFields = 0;
		if(range.getStart() != null){
			startFields = range.getStart().getFields();
			numNonNullStartFields = FieldTool.countNonNullLeadingFields(startFields);
		}

		List<Field<?>> endFields = null;
		int numNonNullEndFields = 0;
		if(range.getEnd() != null){
			endFields = range.getEnd().getFields();
			numNonNullEndFields = FieldTool.countNonNullLeadingFields(endFields);
		}

		int numEqualsLeadingFields = 0;
		if(range.getStart() != null && range.getEnd() != null){
			int numNonNullLeadingFields = Math.min(numNonNullStartFields, numNonNullEndFields);
			for(int i = 0; i < numNonNullLeadingFields; i++){
				if(startFields.get(i).getValue().equals(endFields.get(i).getValue())){
					if(i > 0){
						append(" and ");
					}else{
						append("(");
					}
					appendSqlNameValue(startFields.get(i), true);
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
				append(" and ");
			}
			append("(");
			for(int i = numNonNullStartFields; i > numEqualsLeadingFields; --i){
				if(i < numNonNullStartFields){
					append(" or ");
				}
				append("(");
				for(int j = numEqualsLeadingFields; j < i; ++j){
					if(j > numEqualsLeadingFields){
						append(" and ");
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
					addSqlNameValueWithOperator(startField, operator, true);
				}
				append(")");
			}
			append(")");
		}

		if(range.getEnd() != null && numNonNullEndFields > 0 && numNonNullEndFields > numEqualsLeadingFields){
			if(numEqualsLeadingFields > 0 || hasStart){
				append(" and ");
			}
			append("(");
			for(int i = numEqualsLeadingFields; i < numNonNullEndFields; ++i){
				if(i > numEqualsLeadingFields){
					append(" or ");
				}
				append("(");
				for(int j = numEqualsLeadingFields; j <= i; ++j){
					if(j > numEqualsLeadingFields){
						append(" and ");
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
					addSqlNameValueWithOperator(endField, operator, true);
				}
				append(")");
			}
			append(")");
		}

		if(numEqualsLeadingFields > 0){
			append(")");
		}
		return implementation;
	}

	public Q deleteAll(Config config, String tableName){
		addDeleteFromClause(tableName);
		addLimitOffsetClause(config);
		return implementation;
	}

	public Q addForceIndexClause(String indexName){
		if(indexName == null){
			return implementation;
		}
		append(" force index (");
		append(indexName);
		append(")");
		return implementation;
	}

	public Q addSelectFromClause(String tableName, List<Field<?>> selectFields){
		append("select ");
		FieldTool.appendCsvColumnNames(sqlBuilder, selectFields);
		append(" from " + tableName);
		return implementation;
	}

	public Q addDeleteFromClause(String tableName){
		append("delete from " + tableName);
		return implementation;
	}

	public Q addUpdateClause(String tableName){
		append("update ");
		append(tableName);
		append(" set ");
		return implementation;
	}

	public static boolean needsRangeWhereClause(FieldSet<?> start, FieldSet<?> end){
		return start != null && FieldTool.countNonNullLeadingFields(start.getFields()) > 0
				|| end != null && FieldTool.countNonNullLeadingFields(end.getFields()) > 0;
	}

	public Q addOrderByClause(List<Field<?>> orderByFields){
		if(CollectionTool.nullSafeIsEmpty(orderByFields)){
			return implementation;
		}
		append(" order by ");
		int counter = 0;
		for(Field<?> field : orderByFields){
			if(counter > 0){
				append(", ");
			}
			append(field.getKey().getColumnName() + " asc");
			++counter;
		}
		return implementation;
	}

	@Override
	public String toString(){
		return sqlBuilder.toString();
	}

}
