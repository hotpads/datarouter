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

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.li;
import static j2html.TagCreator.ul;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.Objects;

import javax.inject.Inject;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.digest.DailyDigest.DailyDigestType;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import io.datarouter.web.user.session.CurrentUserSessionInfoService;
import j2html.tags.ContainerTag;

public class DailyDigestHandler extends BaseHandler{

	@Inject
	private DailyDigestRegistry dailyDigestRegistry;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterInjector injector;
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
		var digestsWithContent = Scanner.of(dailyDigestRegistry.registry)
				.map(injector::getInstance)
				.include(digest -> digest.getType() == type)
				.map(dailyDigest -> new Pair<>(dailyDigest, dailyDigest.getPageContent(zoneId).orElse(null)))
				.include(digestWithContent -> Objects.nonNull(digestWithContent.getRight()))
				.sort(Comparator.comparing(Pair::getLeft, DailyDigest.COMPARATOR))
				.list();

		ContainerTag<?> content;
		if(digestsWithContent.isEmpty()){
			content = div("No content for the daily digest.")
					.withClass("container-fluid");
		}else{
			ContainerTag<?> header = h2("Daily Digest - " + type.display);
			ContainerTag<?> toc = ul(each(digestsWithContent, digestWithContent -> {
				DailyDigest digest = digestWithContent.getLeft();
				return li(a(digest.getTitle()).withHref("#" + digest.getId()));
			}));
			content = div(header, toc, each(digestsWithContent, digestWithContent -> div(digestWithContent.getRight())
					.withId(digestWithContent.getLeft().getId())))
					.withClass("container-fluid");
		}
		return pageFactory.startBuilder(request)
				.withTitle("Daily Digest " + type.display)
				.withContent(content)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.buildMav();
	}

}
