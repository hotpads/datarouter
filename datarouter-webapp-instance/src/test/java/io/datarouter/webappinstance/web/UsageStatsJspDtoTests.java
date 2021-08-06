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
package io.datarouter.webappinstance.web;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.tuple.Pair;
import io.datarouter.webappinstance.web.WebappInstanceHandler.UsageStatsJspDto;

public class UsageStatsJspDtoTests{

	private static final List<Pair<String,String>> pairs = List.of(
			new Pair<>("a", "1"),
			new Pair<>("a", "1"),
			new Pair<>("a", "1"),
			new Pair<>("a", "2"));

	@Test
	public void allCommon(){
		var leftUsage = new UsageStatsJspDto(pairs, Pair::getLeft);
		Assert.assertTrue(leftUsage.getAllCommon());
		Assert.assertEquals(leftUsage.getMostCommon(), "a");
		Assert.assertEquals(leftUsage.getUniqueCount(), 1);
		Assert.assertTrue(leftUsage.getUsageBreakdownHtml().contains("100.0%"));
	}

	@Test
	public void notAllCommon(){
		var rightUsage = new UsageStatsJspDto(pairs, Pair::getRight);
		Assert.assertFalse(rightUsage.getAllCommon());
		Assert.assertEquals(rightUsage.getMostCommon(), "1");
		Assert.assertEquals(rightUsage.getUniqueCount(), 2);
		Assert.assertTrue(rightUsage.getUsageBreakdownHtml().contains("75.0%"));
		Assert.assertTrue(rightUsage.getUsageBreakdownHtml().contains("25.0%"));
	}

}
