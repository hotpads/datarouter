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
package io.datarouter.webappinstance;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.webappinstance.WebappInstanceTableService.ColumnUsageStat;
import io.datarouter.webappinstance.WebappInstanceTableService.WebappInstanceColumn;

public class UsageStatsJspDtoTests{

	private static final List<Pair> pairs = List.of(
			new Pair("a", "1"),
			new Pair("a", "1"),
			new Pair("a", "1"),
			new Pair("a", "2"));

	@Test
	public void allCommon(){
		var leftColumn = new WebappInstanceColumn<>("col", Pair::left);
		var leftUsage = new ColumnUsageStat<>(pairs, leftColumn);
		Assert.assertTrue(leftUsage.allCommon);
		Assert.assertEquals(leftUsage.mostCommon, "a");
		Assert.assertEquals(leftUsage.uniqueCount, 1);
		Assert.assertEquals(leftUsage.usage.size(), 1);
		Assert.assertEquals(leftUsage.usage.getFirst().getUsagePercentagePrintable(), "100.0");
	}

	@Test
	public void notAllCommon(){
		var rightColumn = new WebappInstanceColumn<>("col", Pair::right);
		var rightUsage = new ColumnUsageStat<>(pairs, rightColumn);
		Assert.assertFalse(rightUsage.allCommon);
		Assert.assertEquals(rightUsage.mostCommon, "1");
		Assert.assertEquals(rightUsage.uniqueCount, 2);
		Assert.assertEquals(rightUsage.usage.size(), 2);
		Assert.assertTrue(rightUsage.usage.get(0).getUsagePercentagePrintable().contains("75.0"));
		Assert.assertTrue(rightUsage.usage.get(1).getUsagePercentagePrintable().contains("25.0"));
	}

	private record Pair(
			String left,
			String right){
	}
}
