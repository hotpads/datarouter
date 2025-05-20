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
import io.datarouter.instrumentation.relay.dto.RelayMessageBlockDto;
import io.datarouter.instrumentation.relay.dto.RelayMessageBlockPaddingDto;
import io.datarouter.instrumentation.relay.dto.RelayMessageMarkDto;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockAlign;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockColor;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockCols;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockType;

public abstract class BaseRmlBlock<T extends BaseRmlBlock<T>> implements RmlBlock{

	protected final RelayMessageBlockType type;
	protected String text;
	protected List<RmlBlock> content;
	protected List<RelayMessageMarkDto> marks;
	protected RelayMessageBlockAttrsDto attrs;

	public BaseRmlBlock(RelayMessageBlockType type){
		this.type = type;
	}

	protected abstract T self();

	public T hideOnShortForm(){
		attrs = RelayMessageBlockAttrsDto.merge(attrs, RelayMessageBlockAttrsDto.shortFormAlt(
				Rml.container(List.of()).build()));
		return self();
	}

	public T withShortFormAlt(RmlBlock shortFormAlt){
		attrs = RelayMessageBlockAttrsDto.merge(attrs, RelayMessageBlockAttrsDto.shortFormAlt(shortFormAlt.build()));
		return self();
	}

	public T withPlaintextAlt(String plaintextAlt){
		attrs = RelayMessageBlockAttrsDto.merge(attrs, RelayMessageBlockAttrsDto.plaintextAlt(plaintextAlt));
		return self();
	}

	public T withAlign(RelayMessageBlockAlign align){
		attrs = RelayMessageBlockAttrsDto.merge(attrs, RelayMessageBlockAttrsDto.align(align));
		return self();
	}

	public T withPadding(RelayMessageBlockPaddingDto padding){
		attrs = RelayMessageBlockAttrsDto.merge(attrs, RelayMessageBlockAttrsDto.padding(padding));
		return self();
	}

	public T withCols(RelayMessageBlockCols cols){
		attrs = RelayMessageBlockAttrsDto.merge(attrs, RelayMessageBlockAttrsDto.cols(cols));
		return self();
	}

	public T withBackgroundColor(RelayMessageBlockColor backgroundColor){
		attrs = RelayMessageBlockAttrsDto.merge(attrs, RelayMessageBlockAttrsDto.backgroundColor(backgroundColor));
		return self();
	}

	public T withContextColor(RelayMessageBlockColor contextColor){
		attrs = RelayMessageBlockAttrsDto.merge(attrs, RelayMessageBlockAttrsDto.contextColor(contextColor));
		return self();
	}

	@Override
	public RelayMessageBlockDto build(){
		return new RelayMessageBlockDto(
				type,
				text,
				content == null ? null : content.stream().map(RmlBlock::build).toList(),
				marks,
				attrs);
	}

}