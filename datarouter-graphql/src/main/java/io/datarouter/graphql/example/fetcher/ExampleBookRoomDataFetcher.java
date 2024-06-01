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
package io.datarouter.graphql.example.fetcher;

import java.util.List;

import io.datarouter.graphql.client.util.example.arg.ExampleBookRoomGraphQlArgumentType;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleConferenceRoomGraphQlType;
import io.datarouter.graphql.client.util.response.GraphQlErrorDto;
import io.datarouter.graphql.client.util.response.GraphQlResultDto;
import io.datarouter.graphql.example.ExampleQlData;
import io.datarouter.graphql.fetcher.BaseDataFetcher;

public class ExampleBookRoomDataFetcher
extends BaseDataFetcher<List<ExampleConferenceRoomGraphQlType>,ExampleBookRoomGraphQlArgumentType>{

	@Override
	public GraphQlResultDto<List<ExampleConferenceRoomGraphQlType>> getData(){
		String roomName = args.roomName;
		if(roomName == null){
			return GraphQlResultDto.withError(GraphQlErrorDto.invalidInput("roomName cannot be null!"));
		}
		ExampleQlData.tenthFloorRooms.stream()
				.filter(room -> room.name.equals(roomName))
				.forEach(room -> room.availability = false);
		return GraphQlResultDto.withData(ExampleQlData.tenthFloorRooms);
	}

}