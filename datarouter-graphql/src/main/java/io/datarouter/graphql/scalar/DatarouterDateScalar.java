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
package io.datarouter.graphql.scalar;

import java.util.Date;

import graphql.language.StringValue;
import graphql.scalars.util.Kit;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.datarouter.util.DateTool;

public class DatarouterDateScalar{

	public static final GraphQLScalarType GraphQlDate = GraphQLScalarType.newScalar()
			.name("Date")
			.description("DatarouterGraphQl Date Type")
			.coercing(new GraphQlDateCoercing())
			.build();

	public static class GraphQlDateCoercing implements Coercing<Date,String>{

		@Override
		public String serialize(Object dataFetcherResult) throws CoercingSerializeException{
			Date date;
			if(dataFetcherResult instanceof Date){
				date = (Date)dataFetcherResult;
			}else if(dataFetcherResult instanceof String){
				date = DateTool.parseIso(dataFetcherResult.toString());
			}else{
				throw new CoercingSerializeException("Expected a 'String' or 'java.util.Date' but was '" + Kit.typeName(
						dataFetcherResult) + "'.");
			}
			return DateTool.getInternetDate(date, 3);
		}

		@Override
		public Date parseValue(Object input) throws CoercingParseValueException{
			Date date;
			if(input instanceof Date){
				date = (Date)input;
			}else if(input instanceof String){
				date = DateTool.parseIso(input.toString());
			}else{
				throw new CoercingParseValueException("Expected a 'String' or 'java.util.Date' but was '" + Kit
						.typeName(input) + "'.");
			}
			return date;
		}

		@Override
		public Date parseLiteral(Object input) throws CoercingParseLiteralException{
			if(!(input instanceof StringValue)){
				throw new CoercingParseLiteralException("Expected AST type 'StringValue' but was '" + Kit.typeName(
						input) + "'.");
			}
			return DateTool.parseIso(input.toString());
		}

	}

}
