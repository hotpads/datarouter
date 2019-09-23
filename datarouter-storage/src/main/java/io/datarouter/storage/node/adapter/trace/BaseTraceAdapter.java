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
package io.datarouter.storage.node.adapter.trace;

import io.datarouter.instrumentation.trace.TraceSpanFinisher;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.adapter.BaseAdapter;

public abstract class BaseTraceAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>
extends BaseAdapter<PK,D,F,N>
implements TraceAdapter<PK,D,F,N>{

	public BaseTraceAdapter(N backingNode){
		super(backingNode);
	}

	@Override
	protected String getToStringPrefix(){
		return "TraceAdapter";
	}

	@Override
	public TraceSpanFinisher startSpanForOp(String opName){
		String traceName = getName() + " " + opName;
		return TracerTool.startSpan(TracerThreadLocal.get(), traceName);
	}

}
