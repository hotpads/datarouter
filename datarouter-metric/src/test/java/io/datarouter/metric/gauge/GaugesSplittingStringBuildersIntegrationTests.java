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
package io.datarouter.metric.gauge;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.metric.dto.GaugeBlobDto;
import io.datarouter.metric.dto.GaugeBlobDto.GaugeBlobItemDto;
import io.datarouter.metric.dto.GaugeBlobDto.GaugesSplittingStringBuilders;
import io.datarouter.util.UlidTool;

public class GaugesSplittingStringBuildersIntegrationTests{

	private static final String ulid = UlidTool.nextUlid();
	private static final String ulid2 = UlidTool.nextUlid();

	private static final String ulid1GaugeNameLine = "gaugeName\t" + ulid + "\t1";
	private static final String ulid2GaugeNameLine = "gaugeName\t" + ulid2 + "\t1";

	@Test
	public void testValidation(){
		var builder = new GaugeBlobDto.GaugesSplittingStringBuilders(ulid.length() - 1);
		Assert.assertThrows(IllegalArgumentException.class, () -> append(builder, "", "gaugeName", "1"));
		Assert.assertThrows(IllegalArgumentException.class, () -> append(builder, null, "gaugeName", "1"));
		Assert.assertThrows(IllegalArgumentException.class, () -> append(builder, ulid, "", "1"));
		Assert.assertThrows(IllegalArgumentException.class, () -> append(builder, ulid, null, "1"));
		Assert.assertThrows(IllegalArgumentException.class, () -> append(builder, ulid, "gaugeName", ""));
		Assert.assertThrows(IllegalArgumentException.class, () -> append(builder, ulid, "gaugeName", null));
	}

	@Test
	public void testTooSmallForAnything(){
		//not long enough for any timestamp, so everything should be discarded
		var builder = new GaugeBlobDto.GaugesSplittingStringBuilders(ulid.length() - 1);

		//too small for anything
		append(builder, ulid, "gaugeName", "1");
		append(builder, ulid2, "gaugeName", "1");
		var list = builder.scanSplitGauges().list();
		Assert.assertEquals(list.size(), 0);
	}

	@Test
	public void testSingleLineFileSplitting(){
		var builder = new GaugeBlobDto.GaugesSplittingStringBuilders(
				ulid1GaugeNameLine.length());//just long enough for two gauges
		append(builder, ulid, "gaugeName", "1");
		append(builder, ulid, "gaugeName", "1");
		append(builder, ulid2, "gaugeName", "1");
		append(builder, ulid, "gaugeName", "1");
		append(builder, ulid2, "gaugeName", "1");
		append(builder, ulid, "gaugeName", "1");

		var list = builder.scanSplitGauges().list();
		Assert.assertEquals(list.size(), 6);
		Assert.assertEquals(list.get(0), ulid1GaugeNameLine);
		Assert.assertEquals(list.get(1), ulid1GaugeNameLine);
		Assert.assertEquals(list.get(2), ulid2GaugeNameLine);
		Assert.assertEquals(list.get(3), ulid1GaugeNameLine);
		Assert.assertEquals(list.get(4), ulid2GaugeNameLine);
		Assert.assertEquals(list.get(5), ulid1GaugeNameLine);
	}

	@Test
	public void testMultiLineFileSplitting(){
		//these get split, because there's no room for \n
		var builder = new GaugeBlobDto.GaugesSplittingStringBuilders(ulid1GaugeNameLine.length() * 2);
		append(builder, ulid, "gaugeName", "1");
		append(builder, ulid2, "gaugeName", "1");

		var list = builder.scanSplitGauges().list();
		Assert.assertEquals(list.size(), 2);
		Assert.assertEquals(list.get(0), ulid1GaugeNameLine);
		Assert.assertEquals(list.get(1), ulid2GaugeNameLine);

		//these don't get split, because there's room for the \n
		builder = new GaugeBlobDto.GaugesSplittingStringBuilders(ulid1GaugeNameLine.length() * 2 + 1);
		append(builder, ulid, "gaugeName", "1");
		append(builder, ulid2, "gaugeName", "1");

		list = builder.scanSplitGauges().list();
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0),
				ulid1GaugeNameLine
				+ "\n" + ulid2GaugeNameLine);
	}

	@Test
	public void testNormalSizeAppending(){
		var builder = new GaugeBlobDto.GaugesSplittingStringBuilders(256 * 1024 - 30);

		append(builder, ulid, "gaugeName", "1");
		append(builder, ulid2, "gaugeName1", "3");
		append(builder, ulid2, "gaugeName2", "2");
		append(builder, ulid2, "gaugeName3", "1");
		append(builder, ulid, "gaugeName", "1");
		append(builder, ulid2, "gaugeName", "1");

		var list = builder.scanSplitGauges().list();
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0), ulid1GaugeNameLine
				+ "\ngaugeName1\t" + ulid2 + "\t3"
				+ "\ngaugeName2\t" + ulid2 + "\t2"
				+ "\ngaugeName3\t" + ulid2 + "\t1"
				+ "\n" + ulid1GaugeNameLine
				+ "\n" + ulid2GaugeNameLine);
	}

	//method to allow minmizing code changes from CountsSplittingStringBuildersIntegrationTests
	//switches order and type of GaugeBlobItemDto arguments
	private static void append(GaugesSplittingStringBuilders builder, String ulid, String name, String count){
		builder.append(new GaugeBlobItemDto(name, ulid, Long.parseLong(count)));
	}

}
