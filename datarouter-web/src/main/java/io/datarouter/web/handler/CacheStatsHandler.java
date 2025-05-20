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
package io.datarouter.web.handler;

import static j2html.TagCreator.h4;
import static j2html.TagCreator.i;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import io.datarouter.web.cache.CacheRegistry;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.TagCreator;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;

public class CacheStatsHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private CacheRegistry cacheRegistry;

	@Handler
	public Mav cacheStats(){
		return pageFactory.simplePage(request, "Cache Stats", makeContent());
	}

	private DivTag makeContent(){
		var title = h4().withText("Cache Statistics").withClass("mb-4");
		var disclaimer =
				i().withText("Note: These statistics are server specific. Additionally, statsRecording must be "
						+ "enabled and the cache must be registered.").withClass("mb-4");
		var table = table().withClass("table");
		var thead = thead().with(
				th().withText("Cache Name"),
				th().withText("Hit Rate"),
				th().withText("Load Success Rate"),
				th().withText("Eviction Count"),
				th().withText("Eviction Weight"),
				th().withText("Total Load Time")).with(tr());
		var tbody = tbody();
		cacheRegistry.scan().forEach(cache -> {
			var stats = cache.getStats();
			tbody.with(tr().with(
					td().withText(cache.getName()),
					createRateRow("Hits", "Misses", stats.hitCount(), stats.missCount()),
					createRateRow("Load Successes", "Load Failures",
							stats.loadSuccessCount(), stats.loadFailureCount()),
					td().withText(String.valueOf(stats.evictionCount())),
					td().withText(String.valueOf(stats.evictionWeight())),
					td().withText(String.valueOf(stats.totalLoadTime()))));
		});
		table.with(thead, tbody);
		return TagCreator.div(title, table, disclaimer).withClass("container mt-4");
	}

	private TdTag createRateRow(String successTitle, String failureTitle, long success, long failure){
		double successRate = success + failure == 0 ? 0 : (double)(success * 100) / (success + failure);
		return td().withText(String.format("%.2f%%", successRate))
				.attr("title", successTitle + ": " + success + ", " + failureTitle + ": " + failure);
	}
}
