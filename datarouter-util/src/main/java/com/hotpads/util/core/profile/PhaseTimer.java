package com.hotpads.util.core.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrNumberFormatter;

/*
 * create one of these when you want timing to start
 *
 * add events of any name whenever you want
 *
 * print it out whenever you want
 */
public class PhaseTimer{

	private long lastMarker = System.currentTimeMillis();
	private List<String> phaseNames = new ArrayList<>();
	private List<Long> phaseTimes = new ArrayList<>();
	private String name;

	public PhaseTimer(){}

	public PhaseTimer(String name){
		this.name = name;
	}

	/****************** static factories ******************************/

	public static PhaseTimer nullSafe(PhaseTimer timer){
		return timer == null ? new PhaseTimer() : timer;
	}

	/*********************** methods ****************************************/

	public <T> T time(T returnVal, String eventName){
		add(eventName);
		return returnVal;
	}

	public PhaseTimer add(String eventName){
		phaseNames.add(eventName);
		long newMarker = System.currentTimeMillis();
		phaseTimes.add(newMarker - lastMarker);
		lastMarker = newMarker;
		return this;
	}

	public PhaseTimer sum(String eventName){
		int phaseIndex = phaseNames.indexOf(eventName);
		if(phaseIndex == -1){
			return add(eventName);
		}
		long newMarker = System.currentTimeMillis();
		phaseTimes.set(phaseIndex, phaseTimes.get(phaseIndex) + newMarker - lastMarker);
		lastMarker = newMarker;
		return this;
	}

	public Long getPhaseTime(String eventName){
		int phaseIndex = phaseNames.indexOf(eventName);
		if(phaseIndex>=0){
			return phaseTimes.get(phaseIndex);
		}
		return null;
	}

	public int numEvents(){
		return this.phaseNames.size();
	}

	public String toString(int showPhasesAtLeastThisMsLong){
		return toString("", showPhasesAtLeastThisMsLong);
	}

	@Override
	public String toString(){
		return toString("", Integer.MIN_VALUE);
	}

	public String toString(String delimiter){
		return toString(delimiter, Integer.MIN_VALUE);
	}

	public String toString(String delimiter,int showPhasesAtLeastThisMsLong){
		StringBuilder sb = new StringBuilder();
		sb.append("[total:" + DrNumberFormatter.addCommas(getElapsedTimeBetweenFirstAndLastEvent()) + "ms]");
		if(name != null){
			sb.append("<" + name + ">");
		}
		for(int i = 0; i < phaseNames.size(); ++i){
			if(phaseTimes.get(i) < showPhasesAtLeastThisMsLong){
				continue;
			}
			sb.append(delimiter + "[" + phaseNames.get(i) + ":" + DrNumberFormatter.addCommas(phaseTimes.get(i))
					+ "ms]");
		}
		return sb.toString();
	}

	public Long getElapsedTimeBetweenFirstAndLastEvent(){
		if(phaseTimes.size() > 0){
			return DrCollectionTool.getSumOfLongs(phaseTimes);
		}
		return 0L;
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
		Map<String,Long> resultMap = new HashMap<>(phaseNames.size());
		for(int i = 0; i < phaseNames.size(); i++){
			resultMap.put(phaseNames.get(i), phaseTimes.get(i));
		}
		return resultMap;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public List<String> getPhaseNames(){
		return phaseNames;
	}

	public List<Long> getPhaseTimes(){
		return phaseTimes;
	}

	public static class Tests{

		@Test
		public void testToString() throws Exception{
			PhaseTimer timer = new PhaseTimer("TestTimer");

			Assert.assertEquals(timer.toString(), timer.toString(-1));
			Assert.assertEquals(timer.toString(), timer.toString(100));
			Assert.assertEquals(timer.toString(), timer.toString("", -1));
			Assert.assertEquals(timer.toString("+"), timer.toString("+", -1));
			Assert.assertEquals(timer.toString(100), timer.toString("", 100));

			timer.add("uno");

			Assert.assertEquals(timer.toString(), timer.toString(-1));
			Assert.assertFalse(timer.toString().equals(timer.toString(100)));
			Assert.assertEquals(timer.toString(), timer.toString("", -1));
			Assert.assertEquals(timer.toString("+"), timer.toString("+", -1));
			Assert.assertEquals(timer.toString(100), timer.toString("", 100));

			Thread.sleep(200);
			timer.add("dos");

			Assert.assertEquals(timer.toString(), timer.toString(-1));
			Assert.assertFalse(timer.toString().equals(timer.toString(100)));
			Assert.assertEquals(timer.toString(), timer.toString("", -1));
			Assert.assertEquals(timer.toString("+"), timer.toString("+", -1));
			Assert.assertEquals(timer.toString(100), timer.toString("", 100));

			Thread.sleep(500);
			timer.add("tres");
			Assert.assertEquals(timer.toString(), timer.toString(-1));
			Assert.assertFalse(timer.toString().equals(timer.toString(100)));
			Assert.assertFalse(timer.toString().equals(timer.toString(400)));
			Assert.assertFalse(timer.toString(100).equals(timer.toString(400)));
			Assert.assertEquals(timer.toString(), timer.toString("", -1));
			Assert.assertEquals(timer.toString("+"), timer.toString("+", -1));
			Assert.assertEquals(timer.toString(100), timer.toString("", 100));
			Assert.assertEquals(timer.toString(400), timer.toString("", 400));
		}

	}

}
