/*
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DurationWithCarriedUnits{

	private static final String DEFAULT_DELIMITER = ", ";
	private static final String LESS_THAN_ONE = "less than one";

	private long[] unitValues = new long[DurationUnit.values().length];

	public DurationWithCarriedUnits(long millis){
		unitValues[DurationUnit.MILLISECONDS.getIndex()] = millis % 1000;
		unitValues[DurationUnit.SECONDS.getIndex()] = (millis / 1000L) % 60;
		unitValues[DurationUnit.MINUTES.getIndex()] = (millis / (1000L * 60)) % 60;
		unitValues[DurationUnit.HOURS.getIndex()] = (millis / (1000L * 60 * 60)) % 24;
		unitValues[DurationUnit.DAYS.getIndex()] = (long)((millis / (1000L * 60 * 60 * 24)) % (365.0 / 12.0));
		unitValues[DurationUnit.MONTHS.getIndex()] = (long)(Math.floor(millis / (1000.0 * 60.0 * 60.0 * 24.0
				* (365.0 / 12.0))) % 12);
		unitValues[DurationUnit.YEARS.getIndex()] = millis / (1000L * 60 * 60 * 24 * 365);
	}

	public long get(int field){
		return unitValues[field];
	}

	@Override
	public String toString(){
		return toStringByMaxUnits(Integer.MAX_VALUE);
	}

	public String toStringByMaxUnits(int numUnits){
		return toStringByMaxUnitsMaxPrecision(DurationUnit.MILLISECONDS, numUnits);
	}

	public String toStringByMaxUnitsMaxPrecision(DurationUnit maxPrecision, int maxUnits){
		return toStringByMaxUnitsMaxPrecision(maxPrecision, maxUnits, DEFAULT_DELIMITER);
	}

	public String toStringByMaxUnitsMaxPrecision(DurationUnit maxPrecision, int maxUnits, String delimiter){
		List<String> units = getNonZeroUnitStrings(maxPrecision, maxUnits);
		if(units.size() > 0){
			StringBuilder sb = new StringBuilder();
			int appended = 0;
			for(String unit : units){
				if(appended > 0){
					sb.append(delimiter);
				}
				sb.append(unit);
				++appended;
			}
			return sb.toString();
		}
		return LESS_THAN_ONE + " " + maxPrecision.getDisplay();
	}

	public String toStringByMaxPrecision(DurationUnit maxPrecision){
		return toStringByMaxUnitsMaxPrecision(maxPrecision, Integer.MAX_VALUE);
	}

	private List<String> getNonZeroUnitStrings(DurationUnit mostPreciseUnit, int maxUnits){
		ArrayList<String> unitStrings = new ArrayList<>();

		Iterator<DurationUnit> iter = Arrays.asList(DurationUnit.values()).iterator();
		int unitsSinceLargestNonzero = 0;
		while(iter.hasNext() && unitsSinceLargestNonzero < maxUnits){
			DurationUnit du = iter.next();

			if(du.getIndex() > mostPreciseUnit.getIndex()){
				break;
			}

			long val = get(du.getIndex());
			if(val > 0){
				unitStrings.add(val + " " + ((val > 1) ? du.getDisplayPlural() : du.getDisplay()));
			}

			if(unitStrings.size() > 0){
				unitsSinceLargestNonzero++;
			}
		}
		return unitStrings;
	}

}
