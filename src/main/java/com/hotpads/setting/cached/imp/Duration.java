package com.hotpads.setting.cached.imp;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;

public class Duration{

	public static final Duration ZERO = new Duration(0, TimeUnit.MILLISECONDS);

	private static final TimeUnit[] timeUnits = new TimeUnit[]{
		TimeUnit.DAYS,
		TimeUnit.HOURS,
		TimeUnit.MINUTES,
		TimeUnit.SECONDS,
		TimeUnit.MILLISECONDS
	};

	private static final String[] strings = new String[]{
		"d",
		"h",
		"m",
		"s",
		"ms"
	};

	private long nano;

	public Duration(String string) throws IllegalArgumentException{
		string = string.toLowerCase().replaceAll("\\s", "");
		String[] values = string.split("[a-z]");
		String[] unites = string.split("\\d+");
		List<String> asList = Arrays.asList(strings);
		for(int i = 0; i < values.length; i++){
			nano += timeUnits[asList.indexOf(unites[i + 1])].toNanos(Long.parseLong(values[i]));
		}
	}

	public Duration(long d, TimeUnit u){
		nano = u.toNanos(d);
	}

	public long toSecond(){
		return TimeUnit.NANOSECONDS.toSeconds(nano);
	}

	@Override
	public String toString(){
		return toString(TimeUnit.MILLISECONDS);
	}
	
	public String toString(TimeUnit presision){
		if(nano == 0) {
			return "0";
		}
		int maxIndex = Arrays.asList(timeUnits).indexOf(presision);
		if (maxIndex == -1) {
			maxIndex = timeUnits.length - 1;
		}
		long rest = nano;
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < maxIndex + 1; i++){
			long aUnit = timeUnits[i].toNanos(1);
			long val = rest / aUnit;
			rest = rest % aUnit;
			if(val != 0){
				builder.append(val + strings[i]);
			}
		}
		return builder.toString();
	}

	public static class DurationTests{

		@Test
		public void toStrsingTest(){
			Duration d = new Duration(3, TimeUnit.DAYS);
			Assert.assertEquals("3d", d.toString());
			d = new Duration("1d2h65m15s");
			Assert.assertEquals("1d3h5m15s", d.toString());
			d = new Duration("15s");
			Assert.assertEquals(15, d.toSecond());
			Assert.assertEquals("15s", d.toString());
			d = new Duration("100000d5s123ms");
			Assert.assertEquals("100000d5s123ms", d.toString());
			d = new Duration("100000 d 5s 123 ms");
			Assert.assertEquals("100000d5s123ms", d.toString());
			d = new Duration("48h");
			Assert.assertEquals("2d", d.toString());
		}
	}

}
