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
package io.datarouter.changelog.web;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;

import java.text.ParseException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.changelog.service.ViewChangelogService;
import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.changelog.storage.ChangelogKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.user.session.CurrentUserSessionInfoService;
import j2html.tags.ContainerTag;

public class ViewChangelogForDateRangeHandler extends BaseHandler{

	private static final String P_reversedDateMs_exact = "dateExact";
	private static final String P_reversedDateMs_start = "dateStart";
	private static final String P_reversedDateMs_end = "dateEnd";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ChangelogDao dao;
	@Inject
	private DatarouterChangelogPaths paths;
	@Inject
	private ViewChangelogService service;
	@Inject
	private CurrentUserSessionInfoService currentSessionInfoService;

	@Handler(defaultHandler = true)
	public Mav viewForDateRange(
			@Param(P_reversedDateMs_exact) OptionalString dateExact,
			@Param(P_reversedDateMs_start) OptionalString dateStart,
			@Param(P_reversedDateMs_end) OptionalString dateEnd){
		var formExact = new HtmlForm();
		formExact.addDateField()
				.withDisplay("Exact Date")
				.withName(P_reversedDateMs_exact)
				.withValue(dateExact.orElse(null))
				.isRequired();
		formExact.addButton()
				.withDisplay("Search")
				.withValue("anything");
		formExact.withAction(servletContext.getContextPath() + paths.datarouter.changelog.viewForDateRange
				.toSlashedString());

		var formRange = new HtmlForm();
		formRange.addDateField()
				.withDisplay("Date Start")
				.withName(P_reversedDateMs_start)
				.withValue(dateStart.orElse(null))
				.isRequired();
		formRange.addDateField()
				.withDisplay("Date End")
				.withName(P_reversedDateMs_end)
				.withValue(dateEnd.orElse(null))
				.isRequired();
		formRange.addButton()
				.withDisplay("Search")
				.withValue("anything");
		formRange.withAction(servletContext.getContextPath() + paths.datarouter.changelog.viewForDateRange
				.toSlashedString());
		ContainerTag<?> formExactTag = Bootstrap4FormHtml.render(formExact, true);
		ContainerTag<?> formRangeTag = Bootstrap4FormHtml.render(formRange, true);
		Scanner<Changelog> scanner = Scanner.empty();

		if(dateExact.isPresent() && dateStart.isEmpty() && dateEnd.isEmpty()){
			var range = makeRange(dateExact.get());
			if(range.isPresent()){
				scanner = dao.scan(range.get());
			}
		}
		if(dateExact.isEmpty() && dateStart.isPresent() && dateEnd.isPresent()){
			var range = makeRange(dateStart.get(), dateEnd.get());
			if(range.isPresent()){
				scanner = dao.scan(range.get());
			}
		}

		return pageFactory.startBuilder(request)
				.withTitle("Changelog")
				.withContent(makeContent(formExactTag, formRangeTag, scanner.list()))
				.buildMav();
	}

	private ContainerTag<?> makeContent(ContainerTag<?> formExact, ContainerTag<?> formRange, List<Changelog> rows){
		var table = service.buildTable(rows, currentSessionInfoService.getZoneId(request));
		return div(br(), formExact, h5("or"), formRange, table)
				.withClass("container-fluid");
	}

	private Optional<Range<ChangelogKey>> makeRange(String dateExact){
		if(dateExact.isEmpty()){
			return Optional.empty();
		}
		long dateStartMs;
		try{
			dateStartMs = Bootstrap4FormHtml.DATE_FORMAT.parse(dateExact).getTime();
		}catch(ParseException e){
			return Optional.empty();
		}
		long dateEndMs = dateStartMs + Duration.ofDays(1).toMillis();
		return Optional.of(makeRangeInternal(dateStartMs, dateEndMs));
	}

	private Optional<Range<ChangelogKey>> makeRange(String dateStart, String dateEnd){
		if(dateStart.isEmpty() || dateEnd.isEmpty()){
			return Optional.empty();
		}
		long dateStartMs;
		long dateEndMs;
		try{
			dateStartMs = Bootstrap4FormHtml.DATE_FORMAT.parse(dateStart).getTime();
			dateEndMs = Bootstrap4FormHtml.DATE_FORMAT.parse(dateEnd).getTime();
		}catch(ParseException e){
			return Optional.empty();
		}
		return Optional.of(makeRangeInternal(dateStartMs, dateEndMs));
	}

	private Range<ChangelogKey> makeRangeInternal(long dateStartMs, long dateEndMs){
		long reversedateMsStart = Long.MAX_VALUE - dateStartMs;
		long reversedateMsEnd = Long.MAX_VALUE - dateEndMs;
		var start = new ChangelogKey(reversedateMsStart, null, null);
		var stop = new ChangelogKey(reversedateMsEnd, null, null);
		return new Range<>(stop, true, start, true);
	}

}
