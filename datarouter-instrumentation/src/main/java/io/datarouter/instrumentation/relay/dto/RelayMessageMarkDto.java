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
package io.datarouter.instrumentation.relay.dto;

import io.datarouter.instrumentation.relay.type.RelayMessageMarkType;
import io.datarouter.instrumentation.typescript.TsNullable;

public record RelayMessageMarkDto(
		RelayMessageMarkType type,
		@TsNullable RelayMessageMarkAttrsDto attrs){

	public static final RelayMessageMarkDto
			CODE = new RelayMessageMarkDto(RelayMessageMarkType.CODE, null),
			EM = new RelayMessageMarkDto(RelayMessageMarkType.EM, null),
			ITALIC = new RelayMessageMarkDto(RelayMessageMarkType.ITALIC, null),
			MONOSPACE = new RelayMessageMarkDto(RelayMessageMarkType.MONOSPACE, null),
			STRONG = new RelayMessageMarkDto(RelayMessageMarkType.STRONG, null),
			STRIKE = new RelayMessageMarkDto(RelayMessageMarkType.STRIKE, null),
			UNDERLINE = new RelayMessageMarkDto(RelayMessageMarkType.UNDERLINE, null);

	public static RelayMessageMarkDto link(String href){
		return new RelayMessageMarkDto(RelayMessageMarkType.LINK, new RelayMessageMarkAttrsDto(href, null));
	}

	public static RelayMessageMarkDto textColor(String color){
		return new RelayMessageMarkDto(RelayMessageMarkType.TEXT_COLOR, new RelayMessageMarkAttrsDto(null, color));
	}

}
