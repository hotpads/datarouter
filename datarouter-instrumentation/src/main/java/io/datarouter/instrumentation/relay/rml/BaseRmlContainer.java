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
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.datarouter.instrumentation.relay.type.RelayMessageBlockType;

public abstract class BaseRmlContainer<T extends BaseRmlContainer<T>> extends BaseRmlBlock<T>{

	public BaseRmlContainer(RelayMessageBlockType type, List<RmlBlock> content){
		super(type);
		content.forEach(this::with);
	}

	public T with(Iterable<RmlBlock> blocks){
		blocks.forEach(this::with);
		return self();
	}

	public T with(Stream<RmlBlock> blocks){
		blocks.forEach(this::with);
		return self();
	}

	public T with(RmlBlock block){
		if(content == null){
			content = new ArrayList<>();
		}
		content.add(block);
		return self();
	}

	public T condWith(boolean conditional, RmlBlock block){
		if(conditional){
			with(block);
		}
		return self();
	}

	public T condWith(boolean conditional, Supplier<RmlBlock> block){
		if(conditional){
			with(block.get());
		}
		return self();
	}

}
