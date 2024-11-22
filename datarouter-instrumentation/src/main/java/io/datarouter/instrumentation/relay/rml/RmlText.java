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

import java.util.ArrayList;
import java.util.List;

import io.datarouter.instrumentation.relay.dto.RelayMessageMarkDto;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockType;

public class RmlText extends BaseRmlBlock<RmlText>{

	public RmlText(String text, List<RelayMessageMarkDto> marks){
		super(RelayMessageBlockType.TEXT);
		this.text = text;
		this.marks = new ArrayList<>(marks);
	}

	public RmlText code(){
		return withMark(RelayMessageMarkDto.CODE);
	}

	public RmlText em(){
		return withMark(RelayMessageMarkDto.EM);
	}

	public RmlText italic(){
		return withMark(RelayMessageMarkDto.ITALIC);
	}

	public RmlText link(String href){
		return withMark(RelayMessageMarkDto.link(href));
	}

	public RmlText monospace(){
		return withMark(RelayMessageMarkDto.MONOSPACE);
	}

	public RmlText strong(){
		return withMark(RelayMessageMarkDto.STRONG);
	}

	public RmlText strike(){
		return withMark(RelayMessageMarkDto.STRIKE);
	}

	public RmlText color(String textColor){
		return withMark(RelayMessageMarkDto.textColor(textColor));
	}

	public RmlText underline(){
		return withMark(RelayMessageMarkDto.UNDERLINE);
	}

	public RmlText withMark(RelayMessageMarkDto mark){
		this.marks.add(mark);
		return self();
	}

	@Override
	protected RmlText self(){
		return this;
	}

}