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
package io.datarouter.graphql.client.util.example;

import io.datarouter.graphql.client.util.example.arg.ExampleBookRoomGraphQlArgumentType;
import io.datarouter.graphql.client.util.example.arg.ExampleOfficeGraphQlArgumentType;
import io.datarouter.graphql.client.util.example.arg.ExampleOfficeGraphQlArgumentType.ExampleSnackGraphQlArgumentType;
import io.datarouter.graphql.client.util.example.type.ExampleGraphQlRootType.ExampleQueryBuilder;
import io.datarouter.graphql.client.util.example.type.ExampleLocationGraphQlType.ExampleLocationGraphQlTypeBuilder;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleConferenceRoomGraphQlType.ExampleConferenceRoomQueryBuilder;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleFloorGraphQlType.ExampleFloorGraphQlTypeBuilder;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleOfficeGraphQlTypeBuilder;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleSnackGraphQlType.ExampleSnackQueryBuilder;
import io.datarouter.graphql.client.util.type.GraphQlRootType;

public class ExampleQueries{

	private static final ExampleSnackQueryBuilder SNACKS_QUERY = new ExampleSnackQueryBuilder(
			new ExampleSnackGraphQlArgumentType(5))
			.name()
			.inventory();

	private static final ExampleConferenceRoomQueryBuilder CONFERENCE_QUERY = new ExampleConferenceRoomQueryBuilder()
			.availability()
			.name();

	private static final ExampleFloorGraphQlTypeBuilder FLOOR_QUERY = new ExampleFloorGraphQlTypeBuilder()
			.floorNum()
			.conferenceRooms(CONFERENCE_QUERY)
			.snacks(SNACKS_QUERY);

	private static final ExampleOfficeGraphQlTypeBuilder OFFICE_QUERY = new ExampleOfficeGraphQlTypeBuilder(
			new ExampleOfficeGraphQlArgumentType("san francisco"))
			.location()
			.floor(FLOOR_QUERY);

	private static final ExampleConferenceRoomQueryBuilder BOOK_ROOM = new ExampleConferenceRoomQueryBuilder(
			new ExampleBookRoomGraphQlArgumentType("troverted"))
			.availability()
			.name();

	private static final ExampleLocationGraphQlTypeBuilder LOCATION_QUERY = new ExampleLocationGraphQlTypeBuilder()
			.teamSize()
			.name();

	public static final ExampleQueryBuilder EXAMPLE_OFFICE_QUERY = new ExampleQueryBuilder(GraphQlRootType.QUERY)
			.office(OFFICE_QUERY);

	public static final ExampleQueryBuilder EXAMPLE_BRANDS_QUERY = new ExampleQueryBuilder(GraphQlRootType.QUERY)
			.location(LOCATION_QUERY);

	public static final ExampleQueryBuilder EXAMPLE_OFFICE_AND_BRANDS_QUERY = new ExampleQueryBuilder(
			GraphQlRootType.QUERY)
			.office(OFFICE_QUERY)
			.location(LOCATION_QUERY);

	public static final ExampleQueryBuilder EXAMPLE_BOOK_ROOM_MUTATION = new ExampleQueryBuilder(
			GraphQlRootType.MUTATION)
			.bookRoom(BOOK_ROOM);

}
