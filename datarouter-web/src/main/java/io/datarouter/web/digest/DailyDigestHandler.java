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
package io.datarouter.web.digest;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.datarouter.plugin.PluginInjector;
import io.datarouter.web.digest.DailyDigest.DailyDigestType;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import io.datarouter.web.user.session.CurrentUserSessionInfoService;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class DailyDigestHandler extends BaseHandler{

	@Inject
	private PluginInjector pluginInjector;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private CurrentUserSessionInfoService currentSessionInfoService;

	@Handler
	public Mav viewSummary(){
		return view(DailyDigestType.SUMMARY);
	}

	@Handler
	public Mav viewActionable(){
		return view(DailyDigestType.ACTIONABLE);
	}

	private Mav view(DailyDigestType type){
		ZoneId zoneId = currentSessionInfoService.getZoneId(request);
		List<Digest> digestsWithContent = pluginInjector.scanInstances(DailyDigest.KEY)
				.include(digest -> digest.getType() == type)
				.map(dailyDigest -> new Digest(dailyDigest, dailyDigest.getPageContent(zoneId).orElse(null)))
				.include(obj -> Objects.nonNull(obj.content))
				.sort(Comparator.comparing(obj -> obj.digest, DailyDigest.COMPARATOR))
				.list();

		DivTag content = DailyDigestHtml.makeContent(type, digestsWithContent);
		return pageFactory.startBuilder(request)
				.withTitle("Daily Digest " + type.display)
				.withContent(content)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.buildMav();
	}

	public record Digest(
			DailyDigest digest,
			DomContent content){
	}

}
