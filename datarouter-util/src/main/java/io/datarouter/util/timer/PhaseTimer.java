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
package io.datarouter.util.timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.duration.DatarouterDuration;

/*
 * create one of these when you want timing to start
 *
 * add events of any name whenever you want
 *
 * print it out whenever you want
 */
public class PhaseTimer{

	public record PhaseNameAndTime(
			String name,
			Long time){
	}

	private final String name;

	private final List<PhaseNameAndTime> phaseNamesAndTimes = new ArrayList<>();
	private long lastMarker;

	public PhaseTimer(){
		this(null);
	}

	public PhaseTimer(String name){
		this.name = name;
		this.lastMarker = System.currentTimeMillis();
	}

	/*------------------------- static factories ----------------------------*/

	public static PhaseTimer nullSafe(PhaseTimer timer){
		return timer == null ? new PhaseTimer() : timer;
	}

	/*------------------------- methods -------------------------------------*/

	public PhaseTimer add(String eventName){
		long newMarker = System.currentTimeMillis();
		phaseNamesAndTimes.add(new PhaseNameAndTime(eventName, newMarker - lastMarker));
		lastMarker = newMarker;
		return this;
	}

	public PhaseTimer add(PhaseTimer timer){
		phaseNamesAndTimes.addAll(timer.getPhaseNamesAndTimes());
		return this;
	}

	public PhaseTimer sum(String eventName){
		int eventIndex = findIndexByName(eventName);
		if(eventIndex < 0){
			return add(eventName);
		}
		synchronized(phaseNamesAndTimes){
			long newMarker = System.currentTimeMillis();
			PhaseNameAndTime prev = phaseNamesAndTimes.get(eventIndex);
			phaseNamesAndTimes.set(eventIndex, new PhaseNameAndTime(eventName, prev.time() + newMarker - lastMarker));
			lastMarker = newMarker;
		}
		return this;
	}

	public Long getPhaseTime(String eventName){
		return searchForName(eventName)
				.map(dto -> dto.time)
				.orElse(null);
	}

	private Optional<PhaseNameAndTime> searchForName(String eventName){
		return phaseNamesAndTimes.stream()
				.filter(nameAndTime -> nameAndTime.name().equals(eventName))
				.findAny();
	}

	private int findIndexByName(String eventName){
		for(int i = 0; i < phaseNamesAndTimes.size(); ++i){
			if(phaseNamesAndTimes.get(i).name().equals(eventName)){
				return i;
			}
		}
		return -1;
	}

	public int numEvents(){
		return phaseNamesAndTimes.size();
	}

	@Override
	public String toString(){
		var sb = new StringBuilder()
				.append("[total=")
				.append(getElapsedTimeBetweenFirstAndLastEvent());
		if(name != null){
			sb.append(" name=").append(name);
		}
		sb.append("]");
		phaseNamesAndTimes.forEach(dto -> sb.append("[" + dto.name() + "=" + dto.time() + "]"));
		return sb.toString();
	}

	public long getElapsedTimeBetweenFirstAndLastEvent(){
		return phaseNamesAndTimes.stream()
				.map(PhaseNameAndTime::time)
				.mapToLong(Long::longValue)
				.sum();
	}

	public String getElapsedString(){
		return new DatarouterDuration(getElapsedTimeBetweenFirstAndLastEvent(), TimeUnit.MILLISECONDS).toString();
	}

	public float getItemsPerSecond(long numItems){
		long elapsedTime = getElapsedTimeBetweenFirstAndLastEvent();
		if(elapsedTime < 1){
			elapsedTime = 1;
		}
		float seconds = (float)elapsedTime / (float)1000;
		return numItems / seconds;
	}

	public Map<String,Long> asMap(){
		return Scanner.of(phaseNamesAndTimes)
				.toMapSupplied(PhaseNameAndTime::name, PhaseNameAndTime::time, LinkedHashMap::new);
	}

	public String getName(){
		return name;
	}

	public List<PhaseNameAndTime> getPhaseNamesAndTimes(){
		return Collections.unmodifiableList(phaseNamesAndTimes);
	}

}
