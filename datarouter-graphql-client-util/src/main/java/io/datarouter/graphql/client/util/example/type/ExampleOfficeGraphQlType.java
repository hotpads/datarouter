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
import io.datarouter.graphql.client.util.example.arg.ExampleBookRoomGraphQlArgumentType;
import io.datarouter.graphql.client.util.example.arg.ExampleOfficeGraphQlArgumentType;
import io.datarouter.graphql.client.util.example.arg.ExampleOfficeGraphQlArgumentType.ExampleSnackGraphQlArgumentType;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleConferenceRoomGraphQlType.ExampleConferenceRoomQueryBuilder;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleFloorGraphQlType.ExampleFloorGraphQlTypeBuilder;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleSnackGraphQlType.ExampleSnackQueryBuilder;
import io.datarouter.graphql.client.util.query.GraphQlClientQueryBuilder;
import io.datarouter.graphql.client.util.type.GraphQlType;

public class ExampleOfficeGraphQlType implements GraphQlType{

	public final String location;
	public final ExampleFloorGraphQlType floor;

	public ExampleOfficeGraphQlType(String location, ExampleFloorGraphQlType floor){
		this.location = location;
		this.floor = floor;
	}

	public static class ExampleOfficeGraphQlTypeBuilder extends GraphQlClientQueryBuilder{

		public ExampleOfficeGraphQlTypeBuilder(ExampleOfficeGraphQlArgumentType arg){
			super("office", arg, ExampleOfficeGraphQlType.class);
		}

		public ExampleOfficeGraphQlTypeBuilder location(){
			this.field("location");
			return this;
		}

		public ExampleOfficeGraphQlTypeBuilder floor(ExampleFloorGraphQlTypeBuilder builder){
			this.fieldWithSubQuery(builder);
			return this;
		}

	}

	public static class ExampleFloorGraphQlType implements GraphQlType{

		public final Integer floorNum;
		public final List<ExampleConferenceRoomGraphQlType> conferenceRooms;
		@Ql(fetcherId = ExampleGraphQlFetcherIdentifiers.SNACKS)
		public final List<ExampleSnackGraphQlType> snacks;

		public ExampleFloorGraphQlType(Integer floorNum, List<ExampleConferenceRoomGraphQlType> conferenceRooms,
				List<ExampleSnackGraphQlType> snacks){
			this.floorNum = floorNum;
			this.conferenceRooms = conferenceRooms;
			this.snacks = snacks;
		}

		public static class ExampleFloorGraphQlTypeBuilder extends GraphQlClientQueryBuilder{

			public ExampleFloorGraphQlTypeBuilder(){
				super("floor", ExampleFloorGraphQlType.class);
			}

			public ExampleFloorGraphQlTypeBuilder floorNum(){
				this.field("floorNum");
				return this;
			}

			public ExampleFloorGraphQlTypeBuilder conferenceRooms(ExampleConferenceRoomQueryBuilder builder){
				this.fieldWithSubQuery(builder);
				return this;
			}

			public ExampleFloorGraphQlTypeBuilder snacks(ExampleSnackQueryBuilder builder){
				this.fieldWithSubQuery(builder);
				return this;
			}

		}

	}

	public static class ExampleConferenceRoomGraphQlType implements GraphQlType{

		@Ql(required = true, description = "Name of the meeting room")
		public final String name;
		@Ql(required = true, description = "Availability of the room")
		public Boolean availability;

		public ExampleConferenceRoomGraphQlType(String name, boolean availability){
			this.name = name;
			this.availability = availability;
		}

		public static class ExampleConferenceRoomQueryBuilder extends GraphQlClientQueryBuilder{

			public ExampleConferenceRoomQueryBuilder(){
				super("conferenceRooms", ExampleConferenceRoomGraphQlType.class);
			}

			public ExampleConferenceRoomQueryBuilder(ExampleBookRoomGraphQlArgumentType arg){
				super("bookRoom", arg, ExampleConferenceRoomGraphQlType.class);
			}

			public ExampleConferenceRoomQueryBuilder name(){
				this.field("name");
				return this;
			}

			public ExampleConferenceRoomQueryBuilder availability(){
				this.field("availability");
				return this;
			}

		}

	}

	public static class ExampleSnackGraphQlType implements GraphQlType{

		public final String name;
		@Ql(description = "The count of snack")
		public final Integer inventory;

		public ExampleSnackGraphQlType(String name, Integer inventory){
			this.name = name;
			this.inventory = inventory;
		}

		public static class ExampleSnackQueryBuilder extends GraphQlClientQueryBuilder{

			public ExampleSnackQueryBuilder(ExampleSnackGraphQlArgumentType arg){
				super("snacks", arg, ExampleSnackGraphQlType.class);
			}

			public ExampleSnackQueryBuilder name(){
				this.field("name");
				return this;
			}

			public ExampleSnackQueryBuilder inventory(){
				this.field("inventory");
				return this;
			}

		}

	}

	public static class ExampleOrgGraphQlType implements GraphQlType{

		public final String orgName;
		@Ql(fetcherId = ExampleGraphQlFetcherIdentifiers.TEAMS)
		public final List<ExampleTeamGraphQlType> teams;

		public ExampleOrgGraphQlType(String orgName, List<ExampleTeamGraphQlType> teams){
			this.orgName = orgName;
			this.teams = teams;
		}

	}

	public static class ExampleTeamGraphQlType implements GraphQlType{

		public final String teamName;
		public final Integer size;

		public ExampleTeamGraphQlType(String teamName, Integer size){
			this.teamName = teamName;
			this.size = size;
		}

	}

}
