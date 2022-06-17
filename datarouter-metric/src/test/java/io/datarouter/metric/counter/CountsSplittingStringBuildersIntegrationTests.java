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
package io.datarouter.metric.counter;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.UlidTool;

public class CountsSplittingStringBuildersIntegrationTests{

	private static final long timestampLong = UlidTool.getTimestampMs(UlidTool.nextUlid());
	private static final String timestamp = String.valueOf(timestampLong);
	private static final String timestamp2 = String.valueOf(timestampLong + 1);

	private static final String additionalCounterName = "\tcounterName\t1";

	private static final String timestampCounterNameLine = timestamp + additionalCounterName;
	private static final String timestamp2CounterNameLine = timestamp2 + additionalCounterName;

	@Test
	public void testValidation(){
		var builder = new CountBlobDto.CountsSplittingStringBuilders(timestamp.length() - 1);
		Assert.assertThrows(IllegalArgumentException.class, () -> builder.append("", "counterName", "1"));
		Assert.assertThrows(IllegalArgumentException.class, () -> builder.append(null, "counterName", "1"));
		Assert.assertThrows(IllegalArgumentException.class, () -> builder.append(timestamp, "", "1"));
		Assert.assertThrows(IllegalArgumentException.class, () -> builder.append(timestamp, null, "1"));
		Assert.assertThrows(IllegalArgumentException.class, () -> builder.append(timestamp, "counterName", ""));
		Assert.assertThrows(IllegalArgumentException.class, () -> builder.append(timestamp, "counterName", null));
	}

	@Test
	public void testTooSmallForAnything(){
		//not long enough for any timestamp, so everything should be discarded
		var builder = new CountBlobDto.CountsSplittingStringBuilders(timestamp.length() - 1);

		//too small for anything
		builder.append(timestamp, "counterName", "1");
		builder.append(timestamp2, "counterName", "1");
		var list = builder.scanSplitCounts().list();
		Assert.assertEquals(list.size(), 1);
		Assert.assertTrue(list.get(0).isEmpty());
	}

	@Test
	public void testSameTimestampSplitting(){
		var builder = new CountBlobDto.CountsSplittingStringBuilders(
				timestampCounterNameLine.length() + additionalCounterName.length());//just long enough for two counts
		builder.append(timestamp, "counterName", "1");
		builder.append(timestamp, "counterName", "1");
		builder.append(timestamp, "counterName", "1");
		builder.append(timestamp, "counterName", "1");
		builder.append(timestamp, "counterName", "1");

		var list = builder.scanSplitCounts().list();
		Assert.assertEquals(list.size(), 3);
		Assert.assertEquals(list.get(0), timestampCounterNameLine + additionalCounterName);
		Assert.assertEquals(list.get(1), timestampCounterNameLine + additionalCounterName);
		Assert.assertEquals(list.get(2), timestampCounterNameLine);
	}

	@Test
	public void testDifferentTimestampNewlineSplitting(){
		//these get split, because there's no room for \n
		var builder = new CountBlobDto.CountsSplittingStringBuilders(timestampCounterNameLine.length() * 2);
		builder.append(timestamp, "counterName", "1");
		builder.append(timestamp2, "counterName", "1");

		var list = builder.scanSplitCounts().list();
		Assert.assertEquals(list.size(), 2);
		Assert.assertEquals(list.get(0), timestampCounterNameLine);
		Assert.assertEquals(list.get(1), timestamp2CounterNameLine);

		//these don't get split, because there's room for the \n
		builder = new CountBlobDto.CountsSplittingStringBuilders(timestampCounterNameLine.length() * 2 + 1);
		builder.append(timestamp, "counterName", "1");
		builder.append(timestamp2, "counterName", "1");

		list = builder.scanSplitCounts().list();
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0),
				timestampCounterNameLine
				+ "\n" + timestamp2CounterNameLine);
	}

	@Test
	public void testNormalSizeAppending(){
		var builder = new CountBlobDto.CountsSplittingStringBuilders(256 * 1024 - 30);

		builder.append(timestamp, "counterName", "1");
		builder.append(timestamp, "counterName", "1");
		builder.append(timestamp, "counterName", "1");
		builder.append(timestamp2, "counterName1", "3");
		builder.append(timestamp2, "counterName2", "2");
		builder.append(timestamp2, "counterName3", "1");
		builder.append(timestamp, "counterName", "1");
		builder.append(timestamp2, "counterName", "1");

		var list = builder.scanSplitCounts().list();
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0), timestampCounterNameLine + additionalCounterName.repeat(2)
				+ "\n" + timestamp2 + "\tcounterName1\t3\tcounterName2\t2\tcounterName3\t1"
				+ "\n" + timestampCounterNameLine
				+ "\n" + timestamp2CounterNameLine);
	}

}
