package io.datarouter.util.duration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

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

	public Duration(long amont, TimeUnit unit){
		nano = unit.toNanos(amont);
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

	public static class DurationTests{

		@Test
		public void parserAndtoStringTest(){
			Duration duration = new Duration(3, TimeUnit.DAYS);
			AssertJUnit.assertEquals("3d", duration.toString());
			duration = new Duration("1d2h65m15s");
			AssertJUnit.assertEquals("1d3h5m15s", duration.toString());
			duration = new Duration("2h1d65m15s");
			AssertJUnit.assertEquals("1d3h5m15s", duration.toString());
			duration = new Duration("15s");
			AssertJUnit.assertEquals(15, duration.toSecond());
			AssertJUnit.assertEquals("15s", duration.toString());
			duration = new Duration("100000d5s123ms");
			AssertJUnit.assertEquals("100000d5s123ms", duration.toString());
			duration = new Duration("100000 d 5s 123 ms");
			AssertJUnit.assertEquals("100000d5s123ms", duration.toString());
			duration = new Duration("48h");
			AssertJUnit.assertEquals("2d", duration.toString());
			duration = new Duration("0");
			AssertJUnit.assertEquals("0ms", duration.toString());
			duration = new Duration("max");
			AssertJUnit.assertEquals(MAX_VALUE.toString(TimeUnit.NANOSECONDS), duration.toString(TimeUnit.NANOSECONDS));
			duration = new Duration("1d1d");
			AssertJUnit.assertEquals("2d", duration.toString());
			duration = new Duration("4ms1us");
			AssertJUnit.assertEquals("4ms1us", duration.toString(TimeUnit.MICROSECONDS));
			duration = new Duration(1234, TimeUnit.NANOSECONDS);
			AssertJUnit.assertEquals("1us", duration.toString(TimeUnit.MICROSECONDS));
			AssertJUnit.assertEquals("1us", duration.toString(TimeUnit.NANOSECONDS));
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
			AssertJUnit.assertEquals(1000, new Duration(1, TimeUnit.SECONDS).toMillis());
		}
	}
}
