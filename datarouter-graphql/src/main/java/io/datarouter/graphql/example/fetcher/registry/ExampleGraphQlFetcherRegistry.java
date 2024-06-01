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
package io.datarouter.graphql.example.fetcher.registry;

import io.datarouter.graphql.client.util.example.ExampleGraphQlFetcherIdentifiers;
import io.datarouter.graphql.example.fetcher.ExampleBookRoomDataFetcher;
import io.datarouter.graphql.example.fetcher.ExampleLocationDataFetcher;
import io.datarouter.graphql.example.fetcher.ExampleOfficeDataFetcher;
import io.datarouter.graphql.example.fetcher.ExampleOrgDataFetcher;
import io.datarouter.graphql.example.fetcher.ExampleSnackDataFetcher;
import io.datarouter.graphql.example.fetcher.ExampleTeamsDataFetcher;
import io.datarouter.graphql.web.GraphQlFetcherRegistry;

public class ExampleGraphQlFetcherRegistry extends GraphQlFetcherRegistry{

	public ExampleGraphQlFetcherRegistry(){
		bind(ExampleGraphQlFetcherIdentifiers.BOOK_ROOM, ExampleBookRoomDataFetcher.class);
		bind(ExampleGraphQlFetcherIdentifiers.LOCATION, ExampleLocationDataFetcher.class);
		bind(ExampleGraphQlFetcherIdentifiers.OFFICE, ExampleOfficeDataFetcher.class);
		bind(ExampleGraphQlFetcherIdentifiers.SNACKS, ExampleSnackDataFetcher.class);
		bind(ExampleGraphQlFetcherIdentifiers.ORG, ExampleOrgDataFetcher.class);
		bind(ExampleGraphQlFetcherIdentifiers.TEAMS, ExampleTeamsDataFetcher.class);
	}

}
