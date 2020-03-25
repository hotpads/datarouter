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
package io.datarouter.metric.counter.collection.archive.imp;

import io.datarouter.instrumentation.count.AtomicCounter;
import io.datarouter.instrumentation.count.CountCollectorPeriod;
import io.datarouter.metric.counter.collection.archive.BaseCountArchive;
import io.datarouter.metric.counter.collection.archive.WritableCountArchive;

public class MemoryCountArchive extends BaseCountArchive implements WritableCountArchive{

	private final Integer numToRetain;
	private final CountCollectorPeriod[] archive;

	public MemoryCountArchive(String sourceType, String source, Long periodMs, Integer numToRetain){
		super(sourceType, source, periodMs);
		this.numToRetain = numToRetain;
		this.archive = new CountCollectorPeriod[this.numToRetain];
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < archive.length; ++i){
			sb.append(i + ":");
			if(archive[i] != null){
				sb.append(archive[i]);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public void saveCounts(CountCollectorPeriod countMap){
		int index = getIndexForMs(countMap.getStartTimeMs());
		CountCollectorPeriod existingPeriod = archive[index];
		long countMapWindowStartMs = getWindowStartMs(countMap.getStartTimeMs());
		if(existingPeriod == null
				|| existingPeriod.getStartTimeMs() < countMapWindowStartMs
				|| existingPeriod.getStartTimeMs() >= countMapWindowStartMs + periodMs){
			AtomicCounter newMap = new AtomicCounter(countMapWindowStartMs, periodMs);
			newMap.merge(countMap);
			archive[index] = newMap;
		}else{
			archive[index].getCounter().merge(countMap);
		}
	}


	private long getWindowStartMs(long ms){
		long toTruncate = ms % periodMs;
		return ms - toTruncate;
	}

	private int getIndexForMs(long ms){
		long periodNumSinceEpoch = getWindowStartMs(ms) / periodMs;
		return (int)(periodNumSinceEpoch % numToRetain);
	}

}
