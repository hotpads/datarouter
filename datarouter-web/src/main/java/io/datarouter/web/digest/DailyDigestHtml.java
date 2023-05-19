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
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.li;
import static j2html.TagCreator.ul;

import java.util.List;

import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.config.DatarouterWebPaths.DailyDigestPaths;
import io.datarouter.web.digest.DailyDigest.DailyDigestType;
import io.datarouter.web.digest.DailyDigestHandler.Digest;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4NavTabsHtml;
import io.datarouter.web.html.nav.NavTabs;
import io.datarouter.web.html.nav.NavTabs.NavTab;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.UlTag;

public class DailyDigestHtml{

	private static DailyDigestPaths PATHS = new DailyDigestPaths();

	public static DivTag makeContent(
			DailyDigestType dailyDigestType,
			List<Digest> digestsWithContent){
		PathNode currentPath = switch(dailyDigestType){
			case ACTIONABLE -> PATHS.viewActionable;
			case SUMMARY -> PATHS.viewSummary;
		};
		DivTag content;
		if(digestsWithContent.isEmpty()){
			content = DailyDigestHtml.makeNoContentAvailable();
		}else{
			content = div();
			if(digestsWithContent.size() > 1){
				content.with(DailyDigestHtml.makeTableOfContents(digestsWithContent)
						.withClass("mt-3"));
			}
			Scanner.of(digestsWithContent)
					.map(digestWithContent -> div(digestWithContent.content())
							.withId(digestWithContent.digest().getId())
							.withClass("mt-5"))
					.forEach(content::with);
		}
		return div(
				makeHeader(dailyDigestType),
				makeNavTabs(currentPath),
				content)
				.withClass("container mt-3");
	}

	private static DivTag makeHeader(DailyDigestType dailyDigestType){
		String subtitle = switch(dailyDigestType){
			case ACTIONABLE -> "List of actions necessary to update your application";
			case SUMMARY -> "Information about your application";
		};
		return div(
				h3("Daily Digest - " + dailyDigestType.display),
				div(subtitle),
				br())
				.withClass("mt-3");
	}

	private static UlTag makeNavTabs(PathNode currentPath){
		var navTabs = new NavTabs()
				.add(new NavTab(
						"Actionable",
						PATHS.viewActionable.getValue(),
						currentPath.equals(PATHS.viewActionable)))
				.add(new NavTab(
						"Summary",
						PATHS.viewSummary.getValue(),
						currentPath.equals(PATHS.viewSummary)));
		return Bootstrap4NavTabsHtml.render(navTabs);
	}

	public static DivTag makeTableOfContents(List<Digest> digestsWithContent){
		var list = ul(each(digestsWithContent, digestWithContent -> {
			DailyDigest digest = digestWithContent.digest();
			return li(a(digest.getTitle()).withHref("#" + digest.getId()));
		}));
		return div(
				h3("Table of Contents"),
				list);
	}

	public static DivTag makeNoContentAvailable(){
		return div("No content for the daily digest.");
	}
}
