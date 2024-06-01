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
package io.datarouter.graphql.client.util.example.type;

import java.util.List;

import io.datarouter.graphql.client.util.config.Ql;
import io.datarouter.graphql.client.util.example.ExampleGraphQlFetcherIdentifiers;
import io.datarouter.graphql.client.util.example.type.ExampleLocationGraphQlType.ExampleLocationGraphQlTypeBuilder;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleConferenceRoomGraphQlType;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleConferenceRoomGraphQlType.ExampleConferenceRoomQueryBuilder;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleOfficeGraphQlTypeBuilder;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleOrgGraphQlType;
import io.datarouter.graphql.client.util.query.GraphQlClientQueryBuilder;
import io.datarouter.graphql.client.util.type.GraphQlRootType;
import io.datarouter.graphql.client.util.type.GraphQlType;

public class ExampleGraphQlRootType implements GraphQlType{

	@Ql(root = GraphQlRootType.QUERY, fetcherId = ExampleGraphQlFetcherIdentifiers.OFFICE)
	public final ExampleOfficeGraphQlType office;
	@Ql(root = GraphQlRootType.QUERY, fetcherId = ExampleGraphQlFetcherIdentifiers.LOCATION)
	public final List<ExampleLocationGraphQlType> location;
	@Ql(root = GraphQlRootType.MUTATION, fetcherId = ExampleGraphQlFetcherIdentifiers.BOOK_ROOM)
	public final List<ExampleConferenceRoomGraphQlType> bookRoom;
	@Ql(root = GraphQlRootType.QUERY, fetcherId = ExampleGraphQlFetcherIdentifiers.ORG)
	public final List<ExampleOrgGraphQlType> org;

	public ExampleGraphQlRootType(
			ExampleOfficeGraphQlType office,
			List<ExampleLocationGraphQlType> location,
			List<ExampleConferenceRoomGraphQlType> bookRoom,
			List<ExampleOrgGraphQlType> org){
		this.office = office;
		this.location = location;
		this.bookRoom = bookRoom;
		this.org = org;
	}

	public static class ExampleQueryBuilder extends GraphQlClientQueryBuilder{

		public ExampleQueryBuilder(GraphQlRootType root){
			super(root, ExampleGraphQlRootType.class);
		}

		public ExampleQueryBuilder office(ExampleOfficeGraphQlTypeBuilder builder){
			this.fieldWithSubQuery(builder);
			return this;
		}

		public ExampleQueryBuilder location(ExampleLocationGraphQlTypeBuilder builder){
			this.fieldWithSubQuery(builder);
			return this;
		}

		public ExampleQueryBuilder bookRoom(ExampleConferenceRoomQueryBuilder builder){
			this.fieldWithSubQuery(builder);
			return this;
		}

	}

}
