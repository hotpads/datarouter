/**
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
package io.datarouter.storage.node.adapter.counter;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.adapter.BaseAdapter;
import io.datarouter.storage.node.adapter.counter.formatter.NodeCounterFormatter;

public abstract class BaseCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>
extends BaseAdapter<PK,D,F,N>
implements CounterAdapter<PK,D,F,N>{

	protected NodeCounterFormatter<PK,D,F,N> counter;

	public BaseCounterAdapter(N backingNode){
		super(backingNode);
		this.counter = new NodeCounterFormatter<>(backingNode);
	}

	@Override
	protected String getToStringPrefix(){
		return "CounterAdapter";
	}

	@Override
	public NodeCounterFormatter<PK,D,F,N> getCounter(){
		return counter;
	}

	@Override
	public N getBackingNode(){
		return backingNode;
	}
}
