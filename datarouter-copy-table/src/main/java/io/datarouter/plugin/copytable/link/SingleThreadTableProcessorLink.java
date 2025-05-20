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

public class SingleThreadTableProcessorLink extends DatarouterLink{

	public static final String
			P_sourceNodeName = "sourceNodeName",
			P_lastKeyString = "lastKeyString",
			P_scanBatchSize = "scanBatchSize",
			P_processorName = "processorName",
			P_toEmail = "toEmail",
			P_submitAction = "submitAction";

	public Optional<String> sourceNodeName = Optional.empty();
	public Optional<String> lastKeyString = Optional.empty();
	public Optional<Integer> scanBatchSize = Optional.empty();
	public Optional<String> processorName = Optional.empty();
	public Optional<String> toEmail = Optional.empty();
	public Optional<String> submitAction = Optional.empty();

	public SingleThreadTableProcessorLink(){
		super(new DatarouterCopyTablePaths().datarouter.tableProcessor.singleThread);
	}

}
