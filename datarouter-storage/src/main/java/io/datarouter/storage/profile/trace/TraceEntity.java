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
package io.datarouter.storage.profile.trace;

import java.util.ArrayList;

import io.datarouter.model.entity.BaseEntity;
import io.datarouter.storage.profile.trace.key.TraceEntityKey;
import io.datarouter.util.collection.CollectionTool;

public class TraceEntity extends BaseEntity<TraceEntityKey>{

	//BaseEntity relies on these to store databeans, so they must be used by Trace Nodes to add databeans to the entity
	public static final String
		QUALIFIER_PREFIX_Trace = "T",
		QUALIFIER_PREFIX_TraceThread = "TT",
		QUALIFIER_PREFIX_TraceSpan = "TS";

	public TraceEntity(){
		super(null);
	}

	public TraceEntity(TraceEntityKey key){
		super(key);
	}


	/********************* get databeans ************************/

	public Trace getTrace(){
		return CollectionTool.getFirst(getDatabeansForQualifierPrefix(Trace.class, QUALIFIER_PREFIX_Trace));
	}

	public ArrayList<TraceThread> getTraceThreads(){
		return getListDatabeansForQualifierPrefix(TraceThread.class, QUALIFIER_PREFIX_TraceThread);
	}

	public ArrayList<TraceSpan> getTraceSpans(){
		return getListDatabeansForQualifierPrefix(TraceSpan.class, QUALIFIER_PREFIX_TraceSpan);
	}

}
