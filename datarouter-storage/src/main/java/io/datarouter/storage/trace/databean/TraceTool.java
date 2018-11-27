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

import java.util.Collection;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import io.datarouter.util.iterable.IterableTool;

public class TraceTool{

	public static<EK extends BaseTraceEntityKey<EK>,
			PK extends BaseTraceSpanKey<EK,PK>,
			TK extends BaseTraceThreadKey<EK,TK>,
			D extends BaseTraceSpan<EK,PK,TK,D>>
	Long totalDurationOfNonChildren(Collection<D> spans){
		return spans.stream()
				.filter(BaseTraceSpan::isTopLevel)
				.mapToLong(BaseTraceSpan::getDuration)
				.sum();
	}

	public static<EK extends BaseTraceEntityKey<EK>,
			PK extends BaseTraceSpanKey<EK,PK>,
			TK extends BaseTraceThreadKey<EK,TK>,
			D extends BaseTraceSpan<EK,PK,TK,D>>
	SortedMap<TK,SortedSet<D>> getSpansByThreadKey(Collection<D> spans){
		SortedMap<TK,SortedSet<D>> out = new TreeMap<>();
		for(D span : IterableTool.nullSafe(spans)){
			TK threadKey = span.getThreadKey();
			if(out.get(threadKey) == null){
				out.put(threadKey, new TreeSet<D>());
			}
			out.get(threadKey).add(span);
		}
		return out;
	}

}
