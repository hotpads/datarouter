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
package io.datarouter.util.timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.Java11;
import io.datarouter.util.UlidTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.tuple.Pair;

/*
 * create one of these when you want timing to start
 *
 * add events of any name whenever you want
 *
 * print it out whenever you want
 */
public class PhaseTimer{

	private final String id;
	private final List<Pair<String,Long>> phaseNamesAndTimes = new ArrayList<>();
	private String name;

	private long lastMarker = System.currentTimeMillis();

	public PhaseTimer(){
		this(null);
	}

	public PhaseTimer(String name){
		this.name = name;
		id = UlidTool.nextUlid();
	}

	/*------------------------- static factories ----------------------------*/

	public static PhaseTimer nullSafe(PhaseTimer timer){
		return timer == null ? new PhaseTimer() : timer;
	}

	/*------------------------- methods -------------------------------------*/

	public PhaseTimer add(String eventName){
		long newMarker = System.currentTimeMillis();
		phaseNamesAndTimes.add(new Pair<>(eventName, newMarker - lastMarker));
		lastMarker = newMarker;
		return this;
	}

	public PhaseTimer sum(String eventName){
		Optional<Pair<String,Long>> nameAndTimeOpt = searchForName(eventName);
		if(Java11.isEmpty(nameAndTimeOpt)){
			return add(eventName);
		}
		long newMarker = System.currentTimeMillis();
		Pair<String,Long> nameAndTime = nameAndTimeOpt.get();
		nameAndTime.setRight(nameAndTime.getRight() + newMarker - lastMarker);
		lastMarker = newMarker;
		return this;
	}

	public Long getPhaseTime(String eventName){
		return searchForName(eventName)
				.map(Pair::getRight)
				.orElse(null);
	}

	private Optional<Pair<String,Long>> searchForName(String eventName){
		return phaseNamesAndTimes.stream()
				.filter(nameAndTime -> nameAndTime.getLeft().equals(eventName))
				.findAny();
	}

	public int numEvents(){
		return phaseNamesAndTimes.size();
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[total=").append(getElapsedTimeBetweenFirstAndLastEvent()).append("]");
		if(name != null){
			sb.append("<").append(name).append(">");
		}
		phaseNamesAndTimes.forEach(pair -> sb.append("[" + pair.getLeft() + "=" + pair.getRight() + "]"));
		return sb.toString();
	}

	public String intermediateToString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[intermediate total=").append(getElapsedTimeBetweenFirstAndLastEvent()).append("]");
		sb.append("<id=").append(id).append(">");
		if(name != null){
			sb.append("<name=").append(name).append(">");
		}
		phaseNamesAndTimes.forEach(pair -> sb.append("[" + pair.getLeft() + "=" + pair.getRight() + "]"));
		return sb.toString();
	}

	public long getElapsedTimeBetweenFirstAndLastEvent(){
		return phaseNamesAndTimes.stream()
				.map(Pair::getRight)
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
				.toMapSupplied(Pair::getLeft, Pair::getRight, LinkedHashMap::new);
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public List<Pair<String,Long>> getPhaseNamesAndTimes(){
		return Collections.unmodifiableList(phaseNamesAndTimes);
	}

}
