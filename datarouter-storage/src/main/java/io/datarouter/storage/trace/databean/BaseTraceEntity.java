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
package io.datarouter.storage.trace.databean;

import java.util.ArrayList;

import io.datarouter.model.entity.BaseEntity;
import io.datarouter.model.key.entity.base.BaseEntityKey;

public abstract class BaseTraceEntity<EK extends BaseEntityKey<EK>> extends BaseEntity<EK>{

	// BaseEntity relies on these to store databeans, so they must be used by TraceNodes to add databeans to the entity
	public static final String QUALIFIER_PREFIX_Trace = "T";
	public static final String QUALIFIER_PREFIX_TraceThread = "TT";
	public static final String QUALIFIER_PREFIX_TraceSpan = "TS";

	public BaseTraceEntity(){
		super(null);
	}

	public BaseTraceEntity(EK key){
		super(key);
	}

	public abstract BaseTrace<?,?,?> getTrace();

	public abstract ArrayList<? extends BaseTraceThread<?,?,?>> getTraceThreads();

	public abstract ArrayList<? extends BaseTraceSpan<?,?,?,?>> getTraceSpans();

}
