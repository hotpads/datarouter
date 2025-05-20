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

import io.datarouter.instrumentation.relay.dto.RelayMessageBlockAttrsDto;
import io.datarouter.instrumentation.relay.dto.RelayMessageBlockShapeDto;
import io.datarouter.instrumentation.relay.dto.RelayMessageBlockSizeDto;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockMediaType;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockType;

public class RmlMedia extends BaseRmlBlock<RmlMedia>{

	public RmlMedia(RelayMessageBlockMediaType mediaType, String src, String alt){
		super(RelayMessageBlockType.MEDIA);
		attrs = RelayMessageBlockAttrsDto.media(src, mediaType, alt);
	}

	// size could be made available on other blocks but many wouldn't make sense
	public RmlMedia withSize(RelayMessageBlockSizeDto size){
		attrs = RelayMessageBlockAttrsDto.merge(attrs, RelayMessageBlockAttrsDto.size(size));
		return self();
	}

	// shape could be made available on other blocks but many wouldn't make sense
	public RmlMedia withShape(RelayMessageBlockShapeDto shape){
		attrs = RelayMessageBlockAttrsDto.merge(attrs, RelayMessageBlockAttrsDto.shape(shape));
		return self();
	}

	@Override
	protected RmlMedia self(){
		return this;
	}

}