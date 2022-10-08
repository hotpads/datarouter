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
import java.util.Objects;
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

	public static class PhaseNameAndTime{

		public String phaseName;
		public Long time;

		public PhaseNameAndTime(String phaseName, Long time){
			this.phaseName = phaseName;
			this.time = time;
		}

		public void setTime(Long time){
			this.time = time;
		}

		@Override
		public int hashCode(){
			return Objects.hash(phaseName, time);
		}

		@Override
		public boolean equals(Object obj){
			if(this == obj){
				return true;
			}
			if(obj == null){
				return false;
			}
			if(getClass() != obj.getClass()){
				return false;
			}
			PhaseNameAndTime other = (PhaseNameAndTime)obj;
			return Objects.equals(phaseName, other.phaseName) && Objects.equals(time, other.time);
		}

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
		Optional<PhaseNameAndTime> nameAndTimeOpt = searchForName(eventName);
		if(nameAndTimeOpt.isEmpty()){
			return add(eventName);
		}
		long newMarker = System.currentTimeMillis();
		PhaseNameAndTime nameAndTime = nameAndTimeOpt.get();
		nameAndTime.setTime(nameAndTime.time + newMarker - lastMarker);
		lastMarker = newMarker;
		return this;
	}

	public Long getPhaseTime(String eventName){
		return searchForName(eventName)
				.map(dto -> dto.time)
				.orElse(null);
	}

	private Optional<PhaseNameAndTime> searchForName(String eventName){
		return phaseNamesAndTimes.stream()
				.filter(nameAndTime -> nameAndTime.phaseName.equals(eventName))
				.findAny();
	}

	public int numEvents(){
		return phaseNamesAndTimes.size();
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[total=").append(getElapsedTimeBetweenFirstAndLastEvent());
		if(name != null){
			sb.append(" name=").append(name);
		}
		sb.append("]");
		phaseNamesAndTimes.forEach(dto -> sb.append("[" + dto.phaseName + "=" + dto.time + "]"));
		return sb.toString();
	}

	public long getElapsedTimeBetweenFirstAndLastEvent(){
		return phaseNamesAndTimes.stream()
				.map(dto -> dto.time)
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
				.toMapSupplied(dto -> dto.phaseName, dto -> dto.time, LinkedHashMap::new);
	}

	public String getName(){
		return name;
	}

	public List<PhaseNameAndTime> getPhaseNamesAndTimes(){
		return Collections.unmodifiableList(phaseNamesAndTimes);
	}

}
