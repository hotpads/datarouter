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
package io.datarouter.opencensus.adapter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.adapter.BaseAdapter;
import io.opencensus.trace.Span;

public abstract class BaseOpencensusAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>
extends BaseAdapter<PK,D,F,N>
implements OpencensusAdapter{
	private static final Logger logger = LoggerFactory.getLogger(BaseOpencensusAdapter.class);

	public BaseOpencensusAdapter(N backingNode){
		super(backingNode);
	}

	@Override
	public String getToStringPrefix(){
		return "OpencensusAdapter";
	}

	@Override
	public Optional<Span> startSpan(){
		if(TracerTool.getCurrentTraceparent() == null){
			return Optional.empty();
		}
		try{
			return DatarouterOpencensusTool.createOpencensusSpan();
		}catch(Exception e){
			logger.error("", e);
			return Optional.empty();
		}
	}

}
