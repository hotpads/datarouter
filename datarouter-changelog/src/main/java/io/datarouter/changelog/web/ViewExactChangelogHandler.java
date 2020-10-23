/**
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
package io.datarouter.changelog.web;

import static j2html.TagCreator.div;

import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.changelog.storage.ChangelogKey;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.J2HtmlLegendTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;

public class ViewExactChangelogHandler extends BaseHandler{

	public static final String P_reversedDateMs = "reversedDateMs";
	public static final String P_changelogType = "changelogType";
	public static final String P_name = "name";

	@Inject
	private ChangelogDao dao;
	@Inject
	private Bootstrap4PageFactory pageFactory;

	@Handler(defaultHandler = true)
	public Mav viewExact(
			@Param(P_reversedDateMs) Long reversedDateMs,
			@Param(P_changelogType) String changelogType,
			@Param(P_name) String name){
		var key = new ChangelogKey(reversedDateMs, changelogType, name);
		Optional<Changelog> changelog = dao.find(key);
		if(changelog.isEmpty()){
			return new MessageMav("Unable to find changelog");
		}
		return pageFactory.startBuilder(request)
				.withTitle("Changelog")
				.withContent(makeContent(changelog.get()))
				.buildMav();
	}

	private ContainerTag makeContent(Changelog changelog){
		var table = new J2HtmlLegendTable()
				.withClass("table table-sm border table-striped")
				.withSingleRow(false)

				.withEntry("Date", new Date(changelog.getKey().getReversedDateMs()).toString())
				.withEntry("Changelog Type", changelog.getKey().getChangelogType())
				.withEntry("Name", changelog.getKey().getName())
				.withEntry("Action", changelog.getAction())
				.withEntry("Username", changelog.getUsername())
				.withEntry("Comment", Optional.ofNullable(changelog.getComment()).orElse(""))
				.build();
		return div(table)
				.withClass("container my-4");
	}

}
