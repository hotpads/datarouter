package com.hotpads.profile.count.collection;

import java.util.ArrayList;

/*
 * Static class for incrementing counts throughout an application.  Register with this class to receive counts.
 */
public class Counters{

	private static final ArrayList<CountCollector> collectors = new ArrayList<>();


	/************* admin ******************/

	public static void addCollector(CountCollector collector){
		collectors.add(collector);
	}

	public static void stopAndFlushAll(){
		for(int i=0; i < collectors.size(); ++i){
			collectors.get(i).stopAndFlushAll();
		}
	}


	/************* couting ********************/

	public static void inc(String key){
		inc(key, 1);
	}

	public static void inc(String key, long delta){
		for(int i=0; i < collectors.size(); ++i){
			collectors.get(i).increment(key.intern(), delta);
		}
	}

}
