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

import io.datarouter.instrumentation.relay.dto.RelayMessageBlockAttrsDto;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockColor;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockType;

public class RmlButton extends BaseRmlContainer<RmlButton>{

	public RmlButton(
			String href,
			List<RmlBlock> content){
		super(RelayMessageBlockType.BUTTON, content);
		attrs = RelayMessageBlockAttrsDto.button(href, RelayMessageBlockColor.DEFAULT);
	}

	@Override
	protected RmlButton self(){
		return this;
	}

}