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
package io.datarouter.storage.counter;

import java.util.ArrayList;

import io.datarouter.util.string.StringTool;

/*
 * Static class for incrementing counts throughout an application.  Register with this class to receive counts.
 */
public class Counters{

	public static long
		s = 1000,//ms in second
		m = 60 * s,//ms in minute
		h = 60 * m,
		d = 24 * h;

	public static long getMs(String stringDuration){
		int digits = Integer.valueOf(StringTool.retainDigits(stringDuration));
		String units = StringTool.retainLetters(stringDuration);
		if("s".equals(units)){
			return digits * s;
		}else if("m".equals(units)){
			return digits * m;
		}else if("h".equals(units)){
			return digits * h;
		}else if("d".equals(units)){
			return digits * d;
		}else{
			throw new IllegalArgumentException("unknown duration:" + stringDuration);
		}
	}

	public static String getSuffix(long periodMs){
		if(periodMs >= d){
			return periodMs / d + "d";
		}else if(periodMs >= h){
			return periodMs / h + "h";
		}else if(periodMs >= m){
			return periodMs / m + "m";
		}else if(periodMs >= s){
			return periodMs / s + "s";
		}else{
			throw new IllegalArgumentException("unknown duration:" + periodMs);
		}
	}

	private static final ArrayList<CountCollector> collectors = new ArrayList<>();

	/************* admin ******************/

	public static void addCollector(CountCollector collector){
		collectors.add(collector);
	}

	public static void stopAndFlushAll(){
		for(int i = 0; i < collectors.size(); ++i){
			collectors.get(i).stopAndFlushAll();
		}
	}

	/************* couting ********************/

	public static void inc(String key){
		inc(key, 1);
	}

	public static void inc(String key, long delta){
		for(int i = 0; i < collectors.size(); ++i){
			collectors.get(i).increment(key.intern(), delta);
		}
	}

}
