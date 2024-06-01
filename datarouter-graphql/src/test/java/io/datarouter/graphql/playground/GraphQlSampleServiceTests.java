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
package io.datarouter.graphql.playground;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.graphql.client.util.type.GraphQlRootType;
import io.datarouter.graphql.config.DatarouterGraphQlTestNgModuleFactory;
import io.datarouter.graphql.example.ExampleGraphQlHandler;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterGraphQlTestNgModuleFactory.class)
public class GraphQlSampleServiceTests{

	@Inject
	private GraphQlPlaygroundSampleService sampleService;

	@Test
	public void testGraphQlHandlersForRegistrationAndSchema(){
		Map<GraphQlRootType,Map<String,String>> sampleQueryByType = sampleService.buildSchemaQuerySamples(
				ExampleGraphQlHandler.class);
		Assert.assertEquals(sampleQueryByType.size(), 2);
		Assert.assertEquals(sampleQueryByType.get(GraphQlRootType.MUTATION).get("bookRoom"), mutationString());
		Assert.assertEquals(sampleQueryByType.get(GraphQlRootType.QUERY).get("office"), queryString());
		Assert.assertEquals(sampleQueryByType.get(GraphQlRootType.QUERY).get("location"), queryStringWithList());
	}

	private String queryString(){
		return GraphQlPlaygroundSampleService.SAMPLE_INFO
				+ "query{office(location:\"san francisco\"){"
				+ "location\n"
				+ "floor{"
					+ "floorNum\n"
					+ "conferenceRooms{"
						+ "name\n"
						+ "availability\n"
					+ "}\n"
					+ "snacks(limit:3){"
						+ "name\n"
						+ "inventory\n"
					+ "}\n"
				+ "}\n}}";
	}

	private String queryStringWithList(){
		return GraphQlPlaygroundSampleService.SAMPLE_INFO
				+ "query{location(locations:[\"San Francisco\",\"New York\"])"
				+ "{name\n"
				+ "teamSize\n"
				+ "}}";
	}

	private String mutationString(){
		return GraphQlPlaygroundSampleService.SAMPLE_INFO
				+ "mutation{bookRoom(roomName:\"troverted\"){"
					+ "name\n"
					+ "availability\n"
				+ "}}";
	}

}
