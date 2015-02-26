package com.hotpads.util.core.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.CollectionTool;
import com.hotpads.datarouter.util.core.NumberFormatter;



/*
 * create one of these when you want timing to start
 * 
 * add events of any name whenever you want
 * 
 * print it out whenever you want
 */
public class PhaseTimer {

	private long lastMarker = System.currentTimeMillis();
	private List<String> phaseNames = new ArrayList<String>();
	private List<Long> phaseTimes = new ArrayList<Long>();
	private String name = null;

	public PhaseTimer() { }
	public PhaseTimer(String name) {
		this.name = name;
	}
	
	/****************** static factories ******************************/
	
	public static PhaseTimer nullSafe(PhaseTimer timer){
		return timer==null?new PhaseTimer():timer;
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
		if(!phaseNames.contains(eventName)){
			return add(eventName);
		}
		int i = phaseNames.indexOf(eventName);
		long newMarker = System.currentTimeMillis();
		phaseTimes.set(i, phaseTimes.get(i) + newMarker - lastMarker);
		lastMarker = newMarker;
		return this;
	}
		
	@Deprecated
	public PhaseTimer addEvent(String eventName){
		return add(eventName);
	}
	
	public int numEvents(){
		return this.phaseNames.size();
	}
	
	public String toString(int showPhasesAtLeastThisMsLong){
		return toString("",showPhasesAtLeastThisMsLong);
	}
	
	public String toString(){
		return toString("",Integer.MIN_VALUE);
	}
	
	public String toString(String delimiter){
		return(toString(delimiter,Integer.MIN_VALUE));
	}
	
	public String toString(String delimiter,int showPhasesAtLeastThisMsLong){
		StringBuilder sb = new StringBuilder();
		sb.append("[total:"+NumberFormatter.addCommas(
				getElapsedTimeBetweenFirstAndLastEvent())+"ms]");
		if (name != null){ sb.append("<" + name + ">"); }
		for(int i=0; i < phaseNames.size(); ++i){
			if(phaseTimes.get(i) < showPhasesAtLeastThisMsLong) continue;
			sb.append(delimiter+
					"["+phaseNames.get(i)+":"+
					NumberFormatter.addCommas(phaseTimes.get(i))+"ms]");
		}
		return sb.toString();
	}
	
	public Long getElapsedTimeBetweenFirstAndLastEvent(){
		if(phaseTimes.size() > 0){
			return CollectionTool.getSumOfLongs(phaseTimes);
		}
		else{
			return 0L;
		}
	}
	
	public float getItemsPerSecond(int numItems){
		long elapsedTime = getElapsedTimeBetweenFirstAndLastEvent();
		if(elapsedTime < 1){ elapsedTime = 1; }
		float seconds = (float)elapsedTime/(float)1000;
		return (float)numItems/(float)seconds;
	}
	
	public Map<String,Long> asMap(){
		Map<String,Long> m = new HashMap<String,Long>(phaseNames.size());
		for(int i=0; i<phaseNames.size(); i++){
			m.put(phaseNames.get(i), phaseTimes.get(i));
		}
		return m;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public static class Tests {
		@Test public void testToString() throws Exception{
			PhaseTimer t = new PhaseTimer("TestTimer");

			Assert.assertEquals(t.toString(),t.toString(-1));
			Assert.assertEquals(t.toString(),t.toString(100));
			Assert.assertEquals(t.toString(),t.toString("",-1));
			Assert.assertEquals(t.toString("+"),t.toString("+",-1));
			Assert.assertEquals(t.toString(100),t.toString("",100));
			
			t.add("uno");
			
			Assert.assertEquals(t.toString(),t.toString(-1));
			Assert.assertFalse(t.toString().equals(t.toString(100)));
			Assert.assertEquals(t.toString(),t.toString("",-1));
			Assert.assertEquals(t.toString("+"),t.toString("+",-1));
			Assert.assertEquals(t.toString(100),t.toString("",100));
			
			Thread.sleep(200);
			t.add("dos");
			
			Assert.assertEquals(t.toString(),t.toString(-1));
			Assert.assertFalse(t.toString().equals(t.toString(100)));
			Assert.assertEquals(t.toString(),t.toString("",-1));
			Assert.assertEquals(t.toString("+"),t.toString("+",-1));
			Assert.assertEquals(t.toString(100),t.toString("",100));
			
			Thread.sleep(500);
			t.add("tres");
			Assert.assertEquals(t.toString(),t.toString(-1));
			Assert.assertFalse(t.toString().equals(t.toString(100)));
			Assert.assertFalse(t.toString().equals(t.toString(400)));
			Assert.assertFalse(t.toString(100).equals(t.toString(400)));
			Assert.assertEquals(t.toString(),t.toString("",-1));
			Assert.assertEquals(t.toString("+"),t.toString("+",-1));
			Assert.assertEquals(t.toString(100),t.toString("",100));
			Assert.assertEquals(t.toString(400),t.toString("",400));
		}
	}	
	
}
