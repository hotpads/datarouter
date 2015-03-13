package com.hotpads.util.core.date;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class DurationWithCarriedUnits {
	
	private static final String DEFAULT_DELIMITER = ", ";	
	private static final String LESS_THAN_ONE = "less than one";
	
	private long[] unitValues = new long[DurationUnit.values().length];
	
	public DurationWithCarriedUnits(long millis){
		unitValues[DurationUnit.MILLISECONDS.getIndex()] = millis % 1000;
		unitValues[DurationUnit.SECONDS.getIndex()] = (millis / (1000l)) % 60;
		unitValues[DurationUnit.MINUTES.getIndex()] = (millis / (1000l * 60)) % 60;
		unitValues[DurationUnit.HOURS.getIndex()] = (millis / (1000l * 60 * 60)) % 24;
		unitValues[DurationUnit.DAYS.getIndex()] = (millis / (1000l * 60 * 60 * 24)) % (365/12); //not exact for millis > 1 year due to rounding on the 365/12
		unitValues[DurationUnit.MONTHS.getIndex()] = new Double(Math.floor(millis / (1000.0 * 60.0 * 60.0 * 24.0 * (365.0/12.0)))).longValue() % 12;
		unitValues[DurationUnit.YEARS.getIndex()] = (millis / (1000l * 60 * 60 * 24 * 365));
	}
	
	public long get(int field){
		return unitValues[field];
	}
	
	@Override
	public String toString(){
		return toStringByMaxUnits(Integer.MAX_VALUE);
	}

	public String toStringByMaxUnits(int numUnits) {
		return toStringByMaxUnits(numUnits, DEFAULT_DELIMITER);
	}

	public String toStringByMaxUnits(int maxUnits, String delimiter) {
		return toStringByMaxUnitsMaxPrecision(DurationUnit.MILLISECONDS, maxUnits);
	}
	
	public String toStringByMaxPrecision(DurationUnit maxPrecision) {
		return toStringByMaxPrecision(maxPrecision, DEFAULT_DELIMITER);
	}
	
	public String toStringByMaxPrecision(DurationUnit maxPrecision, String delimiter) {
		return toStringByMaxUnitsMaxPrecision(maxPrecision, Integer.MAX_VALUE);		
	}
	
	public String toStringByMaxUnitsMaxPrecision(DurationUnit maxPrecision, int maxUnits) {
		return toStringByMaxUnitsMaxPrecision(maxPrecision, maxUnits, DEFAULT_DELIMITER);
	}
	
	public String toStringByMaxUnitsMaxPrecision(DurationUnit maxPrecision, int maxUnits, String delimiter) {
		List<String> units = getNonZeroUnitStrings(maxPrecision, maxUnits);
		if (units.size() > 0){
			StringBuilder sb = new StringBuilder();
			int appended = 0;
			for(String unit : units){
				if(appended > 0){ sb.append(delimiter); }
				sb.append(unit);
				++appended;
			}
			return sb.toString();
//			return Join.join(delimiter, units);
		}
		return LESS_THAN_ONE + " " + maxPrecision.getDisplay();
	}
	
	private List<String> getNonZeroUnitStrings(DurationUnit mostPreciseUnit, int maxUnits){
		ArrayList<String> unitStrings = new ArrayList<String>();
		
		Iterator<DurationUnit> iter = Arrays.asList(DurationUnit.values()).iterator();
		int unitsSinceLargestNonzero = 0;
		while (iter.hasNext() && unitsSinceLargestNonzero < maxUnits){
			DurationUnit du = iter.next();
			
			if (du.getIndex() > mostPreciseUnit.getIndex())	break;
			
			long val = get(du.getIndex()); 
			if (val > 0){
				unitStrings.add(val + " " + ((val > 1) ? du.getDisplayPlural() : du.getDisplay()));
			}
			
			if (unitStrings.size() > 0) unitsSinceLargestNonzero++; 
		}
		return unitStrings;
	}

	public static class Tests{
		@Test public void testTypicalUse(){
			DurationWithCarriedUnits wpd;

			wpd = new DurationWithCarriedUnits(convert(0, 0, 5,2,31,7,521));
			Assert.assertEquals("5 days, 2 hours", wpd.toStringByMaxUnits(2));			
		}
		
		@Test public void testFullStack(){
			DurationWithCarriedUnits wpd;
			
			wpd = new DurationWithCarriedUnits(convert(0, 0, 5,2,31,7,521));
			Assert.assertEquals("5 days, 2 hours, 31 minutes, 7 seconds, 521 milliseconds", 
					wpd.toString());
			
			wpd = new DurationWithCarriedUnits(convert(0, 0, 5,2,31,7,521));
			Assert.assertEquals("5 days, 2 hours, 31 minutes, 7 seconds, 521 milliseconds", 
					wpd.toStringByMaxPrecision(DurationUnit.MILLISECONDS));
			
			wpd = new DurationWithCarriedUnits(convert(0, 0, 5,2,31,7,521));
			Assert.assertEquals("5 days, 2 hours, 31 minutes, 7 seconds, 521 milliseconds", 
					wpd.toStringByMaxUnits(Integer.MAX_VALUE));
		}
		
		@Test public void testTruncation(){
			DurationWithCarriedUnits wpd;
			
			wpd = new DurationWithCarriedUnits(convert(0, 0, 0,0,0,7,521));
			Assert.assertEquals("7 seconds",wpd.toStringByMaxUnitsMaxPrecision(DurationUnit.SECONDS,2));
			
			wpd = new DurationWithCarriedUnits(convert(0, 0, 0,2,31,7,521));
			Assert.assertEquals("less than one day", wpd.toStringByMaxPrecision(DurationUnit.DAYS));
			
			wpd = new DurationWithCarriedUnits(convert(0, 0, 5,0,31,7,521));
			Assert.assertEquals("5 days", wpd.toStringByMaxUnits(2));
			
		}
		
		@Test public void testLessThan(){
			DurationWithCarriedUnits wpd;
			
			wpd = new DurationWithCarriedUnits(convert(0, 0, 0,2,31,7,521));
			Assert.assertEquals("less than one day", wpd.toStringByMaxPrecision(DurationUnit.DAYS));

			wpd = new DurationWithCarriedUnits(convert(0, 0, 0,0,0,0,521));
			Assert.assertEquals("less than one second",
					wpd.toStringByMaxUnitsMaxPrecision(DurationUnit.SECONDS,2));
			
		}
		
		@Test public void testSingularUnits(){
			DurationWithCarriedUnits wpd;
			
			wpd = new DurationWithCarriedUnits(convert(0, 0, 5,1,1,1,521));
			Assert.assertEquals("5 days, 1 hour", wpd.toStringByMaxUnits(2));
			
		}

		@Test public void testMonths() {
			long millis = convert(0, 3,5,0,0,0,0);
			DurationWithCarriedUnits wpd = new DurationWithCarriedUnits(millis);
			Assert.assertEquals("3 months", wpd.toStringByMaxUnits(1));
		}

		@Test public void testYears() {
			long millis = convert(2,3,0,0,0,0,0);
			DurationWithCarriedUnits wpd = new DurationWithCarriedUnits(millis);
			Assert.assertEquals("2 years", wpd.toStringByMaxUnits(1));
			Assert.assertEquals("2 years, 3 months", wpd.toStringByMaxUnits(2));
			
		}
		
		private static long convert(int years, int months, int d, int h, int m, int s, int ms){
			long millis = 0;
			millis += ms;
			millis += s * 1000l;
			millis += m * 1000l * 60;
			millis += h * 1000l * 60 * 60;
			millis += d * 1000l * 60 * 60 * 24;
			millis += (long) (months * (365.0/12) * 1000l * 60 * 60 * 24);
			millis += years * 365 * 1000l * 60 * 60 * 24;
			return millis;
		}
	}
}