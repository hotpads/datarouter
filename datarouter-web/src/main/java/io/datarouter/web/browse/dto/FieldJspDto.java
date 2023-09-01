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
package io.datarouter.web.browse.dto;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import io.datarouter.model.field.Field;
import j2html.utils.EscapeUtil;

public class FieldJspDto{

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz");

	private final FieldKeyJspDto key;
	private final String value;
	private final String valueString;
	private final String hoverString;

	public FieldJspDto(Field<?> field, ZoneId zoneId){
		this.key = new FieldKeyJspDto(field.getKey());
		this.value = Optional.ofNullable(field.getValue())
				.map(Object::toString)
				.orElse("");
		this.valueString = EscapeUtil.escape(field.getValueString());
		this.hoverString = field.findAuxiliaryHumanReadableString(FORMATTER, zoneId).orElse("");
	}

	public FieldKeyJspDto getKey(){
		return key;
	}

	public String getValue(){
		return value;
	}

	public String getValueString(){
		return valueString;
	}

	public String getHoverString(){
		return hoverString;
	}

}
