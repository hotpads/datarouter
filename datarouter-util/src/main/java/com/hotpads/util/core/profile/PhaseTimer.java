package com.hotpads.util.core.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.util.core.collections.Pair;

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
		if(phaseIndex>=0){
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
		for(int i = 0; i < phaseNamesAndTimes.size(); ++i){
			Pair<String,Long> nameAndTime = phaseNamesAndTimes.get(i);
			if(nameAndTime.getRight() < showPhasesAtLeastThisMsLong){
				continue;
			}
			sb.append(delimiter + "[" + nameAndTime.getLeft() + ":" + DrNumberFormatter.addCommas(nameAndTime
					.getRight()) + "ms]");
		}
		return sb.toString();
	}

	public Long getElapsedTimeBetweenFirstAndLastEvent(){
		if(phaseNamesAndTimes.size() > 0){
			return phaseNamesAndTimes.stream().map(Pair::getRight).mapToLong(Long::longValue).sum();
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
		Map<String,Long> resultMap = new HashMap<>(phaseNamesAndTimes.size());
		for(int i = 0; i < phaseNamesAndTimes.size(); i++){
			resultMap.put(phaseNamesAndTimes.get(i).getLeft(), phaseNamesAndTimes.get(i).getRight());
		}
		return resultMap;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
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
