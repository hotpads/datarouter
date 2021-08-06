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
package io.datarouter.storage.config.schema;

import java.util.Optional;

import io.datarouter.storage.client.ClientId;

public class SchemaUpdateResult{

	public final String ddl;
	public final Optional<String> startupBlockReason;
	public final ClientId clientId;

	public SchemaUpdateResult(String ddl, String startupBlockReason, ClientId clientId){
		this.ddl = ddl;
		this.startupBlockReason = Optional.ofNullable(startupBlockReason);
		this.clientId = clientId;
	}

}
