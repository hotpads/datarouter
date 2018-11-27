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
package io.datarouter.util.duration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.lang.ClassTool;

public class Duration{

	public static final Duration ZERO = new Duration(0, TimeUnit.MILLISECONDS);

	public static final Duration MAX_VALUE = new Duration(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

	public static final String REGEX =
			"^0$|^(m|M)(a|A)(x|X)$|^((\\d+d)?(\\d+h)?(\\d+m)?(\\d+s)?(\\d+ms)?(\\d+us)?){1,1}$";

	private static final TimeUnit[] timeUnits = new TimeUnit[]{
		TimeUnit.DAYS,
		TimeUnit.HOURS,
		TimeUnit.MINUTES,
		TimeUnit.SECONDS,
		TimeUnit.MILLISECONDS,
		TimeUnit.MICROSECONDS,
	};

	private static final String[] strings = new String[]{
		"d",
		"h",
		"m",
		"s",
		"ms",
		"us",
	};

	private long nano;

	public Duration(String string) throws IllegalArgumentException{
		if("0".equals(string)){
			nano = 0;
			return;
		}
		string = string.toLowerCase().replaceAll("\\s", "");
		if("max".equals(string)){
			nano = Long.MAX_VALUE;
			return;
		}
		String[] values = string.split("[a-z]+");
		String[] unites = string.split("\\d+");
		List<String> asList = Arrays.asList(strings);
		for(int i = 0; i < values.length; i++){
			nano += timeUnits[asList.indexOf(unites[i + 1])].toNanos(Long.parseLong(values[i]));
		}
	}

	public Duration(long amount, TimeUnit unit){
		nano = unit.toNanos(amount);
	}

	public long toSecond(){
		return to(TimeUnit.SECONDS);
	}

	public long toMillis(){
		return to(TimeUnit.MILLISECONDS);
	}

	public long to(TimeUnit timeUnit){
		return timeUnit.convert(nano, TimeUnit.NANOSECONDS);
	}

	public java.time.Duration toJavaDuration(){
		return java.time.Duration.ofNanos(to(TimeUnit.NANOSECONDS));
	}

	@Override
	public String toString(){
		return toString(TimeUnit.MILLISECONDS);
	}

	public String toString(TimeUnit precision){
		int maxIndex = Arrays.asList(timeUnits).indexOf(precision);
		if(maxIndex == -1){
			maxIndex = timeUnits.length - 1;
		}
		long rest = nano;
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < maxIndex + 1; i++){
			long unit = timeUnits[i].toNanos(1);
			long val = rest / unit;
			rest = rest % unit;
			if(val != 0 || i == maxIndex && builder.length() == 0){
				builder.append(val + strings[i]);
			}
		}
		return builder.toString();
	}

	public static boolean isDuration(String string){
		try{
			new Duration(string);
		}catch(RuntimeException e){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode(){
		return Long.hashCode(nano);
	}

	@Override
	public boolean equals(Object obj){
		if(ClassTool.differentClass(this, obj)){
			return false;
		}
		Duration other = (Duration)obj;
		return nano == other.nano;
	}


	public static class DurationTests{
		@Test
		public void parserAndtoStringTest(){
			Duration duration = new Duration(3, TimeUnit.DAYS);
			Assert.assertEquals(duration.toString(), "3d");
			duration = new Duration("1d2h65m15s");
			Assert.assertEquals(duration.toString(), "1d3h5m15s");
			duration = new Duration("2h1d65m15s");
			Assert.assertEquals(duration.toString(), "1d3h5m15s");
			duration = new Duration("15s");
			Assert.assertEquals(duration.toSecond(), 15);
			Assert.assertEquals(duration.toString(), "15s");
			duration = new Duration("100000d5s123ms");
			Assert.assertEquals(duration.toString(), "100000d5s123ms");
			duration = new Duration("100000 d 5s 123 ms");
			Assert.assertEquals(duration.toString(), "100000d5s123ms");
			duration = new Duration("48h");
			Assert.assertEquals(duration.toString(), "2d");
			duration = new Duration("0");
			Assert.assertEquals(duration.toString(), "0ms");
			duration = new Duration("max");
			Assert.assertEquals(duration.toString(TimeUnit.NANOSECONDS), MAX_VALUE.toString(TimeUnit.NANOSECONDS));
			duration = new Duration("1d1d");
			Assert.assertEquals(duration.toString(), "2d");
			duration = new Duration("4ms1us");
			Assert.assertEquals(duration.toString(TimeUnit.MICROSECONDS), "4ms1us");
			duration = new Duration(1234, TimeUnit.NANOSECONDS);
			Assert.assertEquals(duration.toString(TimeUnit.MICROSECONDS), "1us");
			Assert.assertEquals(duration.toString(TimeUnit.NANOSECONDS), "1us");
		}

		@Test
		public void testIsDuration(){
			Assert.assertTrue(isDuration("3d"));
			Assert.assertTrue(isDuration("1d2h65m15s"));
			Assert.assertTrue(isDuration("2h1d65m15s"));
			Assert.assertTrue(isDuration("15s"));
			Assert.assertTrue(isDuration("100000d5s123ms"));
			Assert.assertTrue(isDuration("100000 d 5s 123 ms"));
			Assert.assertTrue(isDuration("48h"));
			Assert.assertTrue(isDuration("0"));
			Assert.assertTrue(isDuration("1d1d"));
			Assert.assertTrue(isDuration("4ms1us"));
			Assert.assertTrue(isDuration("MAX"));
			Assert.assertFalse(isDuration("1banana"));
			Assert.assertFalse(isDuration("1.5d7m"));
			Assert.assertFalse(isDuration("1d7"));
			Assert.assertFalse(isDuration("max5d"));
		}

		@Test
		public void testConvertion(){
			Assert.assertEquals(new Duration(1, TimeUnit.SECONDS).toMillis(), 1000);
		}
	}
}
