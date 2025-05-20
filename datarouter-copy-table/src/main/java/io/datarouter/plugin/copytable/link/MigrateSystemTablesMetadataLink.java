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
package io.datarouter.plugin.copytable.link;

import java.util.Optional;

import io.datarouter.httpclient.endpoint.link.DatarouterLink;
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;

public class MigrateSystemTablesMetadataLink extends DatarouterLink{

	public static final String
			P_sourceClientName = "sourceClientName",
			P_targetClientName = "targetClientName",
			P_submitAction = "submitAction";

	public Optional<String> sourceClientName = Optional.empty();
	public Optional<String> targetClientName = Optional.empty();
	public Optional<String> submitAction = Optional.empty();

	public MigrateSystemTablesMetadataLink(){
		super(new DatarouterCopyTablePaths().datarouter.systemTableCopier.migrateSystemTablesMetadata);
	}

}
