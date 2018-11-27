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
package io.datarouter.storage.node.adapter.callsite;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.callsite.CallsiteRecorder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.adapter.BaseAdapter;
import io.datarouter.storage.setting.Setting;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.lang.LineOfCode;

public abstract class BaseCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>
extends BaseAdapter<PK,D,F,N> implements CallsiteAdapter{

	private final Setting<Boolean> recordCallsites;

	public BaseCallsiteAdapter(N backingNode){
		super(backingNode);
		this.recordCallsites = backingNode.getFieldInfo().getRecordCallsite();
	}

	@Override
	public String getToStringPrefix(){
		return "CallsiteAdapter";
	}

	@Override
	public LineOfCode getCallsite(){
		LineOfCode callsite = new LineOfCode(2);//adjust for this method and adapter method
		return callsite;
	}

	@Override
	public void recordCollectionCallsite(Config config, long startTimeNs, Collection<?> items){
		recordCallsite(config, startTimeNs, CollectionTool.size(items));
	}

	@Override
	public void recordCallsite(Config config, long startNs, int numItems){
		if(recordCallsites == null || BooleanTool.isFalseOrNull(recordCallsites.get())){
			return;
		}
		LineOfCode datarouterMethod = new LineOfCode(2);
		long durationNs = System.nanoTime() - startNs;
		CallsiteRecorder.record(backingNode.getName(), datarouterMethod.getMethodName(), config.getCallsite(),
				numItems, durationNs);
	}

}
