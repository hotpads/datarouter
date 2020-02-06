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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.util.tuple.Pair;

/*
 * create one of these when you want timing to start
 *
 * add events of any name whenever you want
 *
 * print it out whenever you want
 */
public class PhaseTimer{

	private long lastMarker = System.currentTimeMillis();
	private List<Pair<String,Long>> phaseNamesAndTimes = new ArrayList<>();
	private String name;

	public PhaseTimer(){
	}

	public PhaseTimer(String name){
		this.name = name;
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
		int phaseIndex = getIndexOf(eventName);
		if(phaseIndex == -1){
			return add(eventName);
		}
		long newMarker = System.currentTimeMillis();
		phaseNamesAndTimes.get(phaseIndex).setRight(phaseNamesAndTimes.get(phaseIndex).getRight() + newMarker
				- lastMarker);
		lastMarker = newMarker;
		return this;
	}

	public Long getPhaseTime(String eventName){
		int phaseIndex = getIndexOf(eventName);
		if(phaseIndex >= 0){
			return phaseNamesAndTimes.get(phaseIndex).getRight();
		}
		return null;
	}

	private int getIndexOf(String eventName){
		for(int i = 0; i < phaseNamesAndTimes.size(); ++i){
			if(phaseNamesAndTimes.get(i).getLeft().equals(eventName)){
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
		StringBuilder sb = new StringBuilder();
		sb.append("[total=" + getElapsedTimeBetweenFirstAndLastEvent() + "]");
		if(name != null){
			sb.append("<" + name + ">");
		}
		for(int i = 0; i < phaseNamesAndTimes.size(); ++i){
			Pair<String,Long> nameAndTime = phaseNamesAndTimes.get(i);
			sb.append("[" + nameAndTime.getLeft() + "=" + nameAndTime.getRight() + "]");
		}
		return sb.toString();
	}

	public long getElapsedTimeBetweenFirstAndLastEvent(){
		if(phaseNamesAndTimes.size() > 0){
			return phaseNamesAndTimes.stream()
					.map(Pair::getRight)
					.mapToLong(Long::longValue)
					.sum();
		}
		return 0;
	}

	public float getItemsPerSecond(int numItems){
		long elapsedTime = getElapsedTimeBetweenFirstAndLastEvent();
		if(elapsedTime < 1){
			elapsedTime = 1;
		}
		float seconds = (float)elapsedTime / (float)1000;
		return numItems / seconds;
	}

	public Map<String,Long> asMap(){
		Map<String,Long> resultMap = new HashMap<>(phaseNamesAndTimes.size());
		for(int i = 0; i < phaseNamesAndTimes.size(); i++){
			resultMap.put(phaseNamesAndTimes.get(i).getLeft(), phaseNamesAndTimes.get(i).getRight());
		}
		return resultMap;
	}

	public void setName(String name){
		this.name = name;
	}

}
