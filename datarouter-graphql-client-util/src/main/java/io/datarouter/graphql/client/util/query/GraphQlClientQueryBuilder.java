/*
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
package io.datarouter.graphql.client.util.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.graphql.client.util.query.GraphQlClientTool.GraphQlParentFieldAndArg;
import io.datarouter.graphql.client.util.type.GraphQlArgumentType;
import io.datarouter.graphql.client.util.type.GraphQlRootType;
import io.datarouter.graphql.client.util.type.GraphQlType;

public abstract class GraphQlClientQueryBuilder{
	private static final Logger logger = LoggerFactory.getLogger(GraphQlClientQueryBuilder.class);

	private final List<GraphQlClientQueryBuilder> fieldSubQueries = new ArrayList<>();
	private final Map<String,Optional<GraphQlArgumentType>> fieldToArgs = new HashMap<>();
	private final GraphQlParentFieldAndArg parent;
	private final Set<String> fieldsFromType;
	private final Class<? extends GraphQlType> graphQlType;

	public GraphQlClientQueryBuilder(GraphQlRootType rootType, Class<? extends GraphQlType> clazz){
		this(rootType.getPersistentString(), clazz);
	}

	public GraphQlClientQueryBuilder(String parent, Class<? extends GraphQlType> clazz){
		this(parent, null, clazz);
	}

	public GraphQlClientQueryBuilder(String parent, GraphQlArgumentType arg, Class<? extends GraphQlType> clazz){
		this.parent = new GraphQlParentFieldAndArg(parent, Optional.ofNullable(arg));
		this.fieldsFromType = GraphQlClientTool.createSetOfGraphQlTypeFields(clazz);
		this.graphQlType = clazz;
	}

	public GraphQlClientQueryBuilder field(String fieldName){
		field(fieldName, null);
		return this;
	}

	public GraphQlClientQueryBuilder field(String fieldName, GraphQlArgumentType arg){
		validate(fieldName);
		fieldToArgs.put(fieldName, Optional.ofNullable(arg));
		return this;
	}

	public GraphQlClientQueryBuilder fieldWithSubQuery(GraphQlClientQueryBuilder builder){
		validate(builder.parent.fieldName());
		fieldSubQueries.add(builder);
		return this;
	}

	private void validate(String fieldName){
		if(!fieldsFromType.contains(fieldName) && !fieldName.contains("...")){
			logger.error("fieldName={} missing in type={}", fieldName, graphQlType);
		}
	}

	public String build(){
		return GraphQlClientTool.buildGraphQlQuery(parent, fieldToArgs, fieldSubQueries);
	}

}
