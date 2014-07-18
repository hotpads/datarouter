package com.hotpads.setting.cached.imp;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;

public class Duration{
	
	public static final TimeUnit[] timeUnits = new TimeUnit[]{
			TimeUnit.DAYS,
			TimeUnit.HOURS,
			TimeUnit.MINUTES,
			TimeUnit.SECONDS,
	};
	public static final String[] strings = new String[]{
			"d",
			"h",
			"m",
			"s"
	};
	private long nano;

	public Duration(String string) throws ParseException{
		String[] values = string.toLowerCase().split("[a-z]");
		String[] unites = string.toLowerCase().split("\\d+");
		List<String> asList = Arrays.asList(strings);
		for(int i = 0; i < values.length; i++){
			nano += timeUnits[asList.indexOf(unites[i + 1])].toNanos(Long.parseLong(values[i]));
		}
	}

	public Duration(long d, TimeUnit u){
		nano = u.toNanos(d);
	}

	@Override
	public String toString(){
		long rest = nano;
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < timeUnits.length; i++){
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
			try{
				d = new Duration("1d2h65m15s");
				System.out.println(d.toString());
			}catch(ParseException e){
				Assert.fail();
			}
		}
	}
	
}
