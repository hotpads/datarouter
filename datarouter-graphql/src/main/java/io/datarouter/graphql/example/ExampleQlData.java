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
package io.datarouter.graphql.example;

import java.util.List;
import java.util.Map;

import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleConferenceRoomGraphQlType;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleFloorGraphQlType;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleOrgGraphQlType;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleTeamGraphQlType;

public class ExampleQlData{

	public static final List<ExampleConferenceRoomGraphQlType> tenthFloorRooms = List.of(
			new ExampleConferenceRoomGraphQlType("troverted", true),
			new ExampleConferenceRoomGraphQlType("teger", true),
			new ExampleConferenceRoomGraphQlType("Denting", true),
			new ExampleConferenceRoomGraphQlType("Terruptible", true));

	public static final Map<String,ExampleOfficeGraphQlType> placeToOffice = Map.of(
			"san francisco", new ExampleOfficeGraphQlType(
					"san francisco",
					new ExampleFloorGraphQlType(10, tenthFloorRooms, null)),
			"seattle", new ExampleOfficeGraphQlType(
					"seattle",
					new ExampleFloorGraphQlType(10, tenthFloorRooms, null)));

	public static final List<ExampleOrgGraphQlType> orgs = List.of(
			new ExampleOrgGraphQlType("engineering", null),
			new ExampleOrgGraphQlType("sales", null),
			new ExampleOrgGraphQlType("marketing", null));

	public static final Map<String,List<ExampleTeamGraphQlType>> orgsToTeams = Map.of(
			"engineering", List.of(
					new ExampleTeamGraphQlType("infra", 20),
					new ExampleTeamGraphQlType("consumer", 15),
					new ExampleTeamGraphQlType("devops", 10)),
			"sales", List.of(
					new ExampleTeamGraphQlType("financial products", 12),
					new ExampleTeamGraphQlType("bundles", 10)),
			"marketing", List.of(
					new ExampleTeamGraphQlType("social media", 5),
					new ExampleTeamGraphQlType("traditional", 5)));

}
