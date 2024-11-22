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
package io.datarouter.instrumentation.relay.type;

import io.datarouter.enums.StringMappedEnum;

public enum RelayMessageMarkType{
	CODE("code"),
	EM("em"),
	ITALIC("italic"),
	LINK("link"),
	MONOSPACE("monospace"),
	STRONG("strong"),
	STRIKE("strike"),
	TEXT_COLOR("textColor"),
	UNDERLINE("underline"),
	;

	public static final StringMappedEnum<RelayMessageMarkType> BY_TYPE
			= new StringMappedEnum<>(values(), el -> el.type);

	private final String type;

	RelayMessageMarkType(String type){
		this.type = type;
	}

}
