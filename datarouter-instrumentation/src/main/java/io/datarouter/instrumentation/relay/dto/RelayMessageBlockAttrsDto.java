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

import java.util.Optional;

import io.datarouter.instrumentation.relay.type.RelayMessageBlockAlign;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockColor;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockCols;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockMediaType;
import io.datarouter.instrumentation.typescript.TsNullable;

public record RelayMessageBlockAttrsDto(
		@TsNullable RelayMessageBlockDto shortFormAlt,
		@TsNullable Integer level,
		@TsNullable String href,
		@TsNullable String username,
		@TsNullable String text,
		@TsNullable String language,
		@TsNullable RelayMessageBlockColor contextColor,
		@TsNullable RelayMessageBlockColor backgroundColor,
		@TsNullable RelayMessageBlockAlign align,
		@TsNullable RelayMessageBlockPaddingDto padding,
		@TsNullable Long epochMs,
		@TsNullable RelayMessageBlockCols cols,
		@TsNullable RelayMessageBlockMediaType mediaType,
		@TsNullable String alt){

	public static RelayMessageBlockAttrsDto shortFormAlt(RelayMessageBlockDto shortFormAlt){
		return new RelayMessageBlockAttrsDto(shortFormAlt, null, null, null, null, null, null, null, null, null, null,
				null, null, null);
	}

	public static RelayMessageBlockAttrsDto level(Integer level){
		return new RelayMessageBlockAttrsDto(null, level, null, null, null, null, null, null, null, null, null, null,
				null, null);
	}

	public static RelayMessageBlockAttrsDto href(String href){
		return new RelayMessageBlockAttrsDto(null, null, href, null, null, null, null, null, null, null, null, null,
				null, null);
	}

	public static RelayMessageBlockAttrsDto username(String username, String href){
		return new RelayMessageBlockAttrsDto(null, null, null, username, href, null, null, null, null, null, null,
				null, null, null);
	}

	public static RelayMessageBlockAttrsDto text(String text){
		return new RelayMessageBlockAttrsDto(null, null, null, null, text, null, null, null, null, null, null, null,
				null, null);
	}

	public static RelayMessageBlockAttrsDto language(String language){
		return new RelayMessageBlockAttrsDto(null, null, null, null, null, language, null, null, null, null, null,
				null, null, null);
	}

	public static RelayMessageBlockAttrsDto button(String href, RelayMessageBlockColor contextColor){
		return new RelayMessageBlockAttrsDto(null, null, href, null, null, null, contextColor, null, null, null, null,
				null, null, null);
	}

	public static RelayMessageBlockAttrsDto backgroundColor(RelayMessageBlockColor backgroundColor){
		return new RelayMessageBlockAttrsDto(null, null, null, null, null, null, null, backgroundColor, null, null,
				null, null, null, null);
	}

	public static RelayMessageBlockAttrsDto contextColor(RelayMessageBlockColor contextColor){
		return new RelayMessageBlockAttrsDto(null, null, null, null, null, null, contextColor, null, null, null, null,
				null, null, null);
	}

	public static RelayMessageBlockAttrsDto align(RelayMessageBlockAlign align){
		return new RelayMessageBlockAttrsDto(null, null, null, null, null, null, null, null, align, null, null, null,
				null, null);
	}

	public static RelayMessageBlockAttrsDto padding(RelayMessageBlockPaddingDto padding){
		return new RelayMessageBlockAttrsDto(null, null, null, null, null, null, null, null, null, padding, null, null,
				null, null);
	}

	public static RelayMessageBlockAttrsDto epochMs(Long epochMs){
		return new RelayMessageBlockAttrsDto(null, null, null, null, null, null, null, null, null, null, epochMs, null,
				null, null);
	}

	public static RelayMessageBlockAttrsDto cols(RelayMessageBlockCols cols){
		return new RelayMessageBlockAttrsDto(null, null, null, null, null, null, null, null, null, null, null, cols,
				null, null);
	}

	public static RelayMessageBlockAttrsDto media(String src, RelayMessageBlockMediaType mediaType, String alt){
		return new RelayMessageBlockAttrsDto(null, null, src, null, null, null, null, null, null, null, null, null,
				mediaType, alt);
	}

	public static RelayMessageBlockAttrsDto merge(RelayMessageBlockAttrsDto prev, RelayMessageBlockAttrsDto next){
		if(prev == null){
			return next;
		}
		return new RelayMessageBlockAttrsDto(
				Optional.ofNullable(next.shortFormAlt).orElse(prev.shortFormAlt),
				Optional.ofNullable(next.level).orElse(prev.level),
				Optional.ofNullable(next.href).orElse(prev.href),
				Optional.ofNullable(next.username).orElse(prev.username),
				Optional.ofNullable(next.text).orElse(prev.text),
				Optional.ofNullable(next.language).orElse(prev.language),
				Optional.ofNullable(next.contextColor).orElse(prev.contextColor),
				Optional.ofNullable(next.backgroundColor).orElse(prev.backgroundColor),
				Optional.ofNullable(next.align).orElse(prev.align),
				Optional.ofNullable(next.padding).orElse(prev.padding),
				Optional.ofNullable(next.epochMs).orElse(prev.epochMs),
				Optional.ofNullable(next.cols).orElse(prev.cols),
				Optional.ofNullable(next.mediaType).orElse(prev.mediaType),
				Optional.ofNullable(next.alt).orElse(prev.alt));
	}

}
