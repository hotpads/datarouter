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
package io.datarouter.graphql.web.exception;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.validation.ValidationError;
import io.datarouter.graphql.error.DatarouterGraphQlDataValidationError;
import io.datarouter.instrumentation.metric.Metrics;

public class GraphQlExceptionCounters{

	public static final String PREFIX = "GraphQl Exception";

	public static void inc(Throwable exception, ErrorClassification classification){
		inc(classification + " " + exception.getClass().getName(), 1L);
	}

	public static void inc(GraphQLError graphQlError, ErrorClassification classification){
		if(ValidationError.class.isAssignableFrom(graphQlError.getClass())){
			inc(classification + " " + ((ValidationError)graphQlError).getValidationErrorType(), 1L);
		}else{
			inc(classification + " " + graphQlError.getClass().getSimpleName(), 1L);
		}
	}

	public static void inc(DatarouterGraphQlDataValidationError qlError){
		inc(qlError.getErrorType() + " " + qlError.getError().code, 1L);
	}

	public static void inc(String key, long delta){
		Metrics.count(PREFIX + " " + key, delta);
	}

}
