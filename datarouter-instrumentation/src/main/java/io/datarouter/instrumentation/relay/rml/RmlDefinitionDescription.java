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
package io.datarouter.instrumentation.relay.rml;

import java.util.List;

import io.datarouter.instrumentation.relay.type.RelayMessageBlockType;

public class RmlDefinitionDescription extends BaseRmlContainer<RmlDefinitionDescription>{

	public RmlDefinitionDescription(List<RmlBlock> content){
		super(RelayMessageBlockType.DEFINITION_DESCRIPTION, content);
	}

	@Override
	public RmlDefinitionDescription self(){
		return this;
	}

}
