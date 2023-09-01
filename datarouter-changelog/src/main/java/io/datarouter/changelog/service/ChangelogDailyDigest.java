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
package io.datarouter.changelog.service;

import static j2html.TagCreator.div;
import static j2html.TagCreator.small;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.changelog.storage.ChangelogKey;
import io.datarouter.changelog.web.ViewExactChangelogHandler;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.types.MilliTime;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ChangelogDailyDigest implements DailyDigest{

	@Inject
	private ChangelogDao dao;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterChangelogPaths paths;
	@Inject
	private ViewChangelogService viewChangelogService;

	@Override
	public Optional<DivTag> getPageContent(ZoneId zoneId){
		var list = getChangelogs(zoneId);
		if(list.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Changelog", paths.datarouter.changelog.viewAll);
		var description = small("For the current day");
		var table = viewChangelogService.buildTable(list, zoneId);
		return Optional.of(div(header, description, table));
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		var list = getChangelogs(zoneId);
		if(list.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Changelog", paths.datarouter.changelog.viewAll);
		var description = small("For the current day");
		var table = buildEmailTable(list, zoneId);
		return Optional.of(div(header, description, table));
	}

	@Override
	public String getTitle(){
		return "Changelog";
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
	}

	private List<Changelog> getChangelogs(ZoneId zoneId){
		var startTime = MilliTime.atEndOfDay(zoneId).toMilliTimeReversed();
		var endTime = MilliTime.atStartOfDay(zoneId).toMilliTimeReversed();
		var start = new ChangelogKey(startTime, null, null);
		var stop = new ChangelogKey(endTime, null, null);
		Range<ChangelogKey> range = new Range<>(start, true, stop, true);
		return dao.scan(range).list();
	}

	private TableTag buildEmailTable(List<Changelog> rows, ZoneId zoneId){
		return new J2HtmlEmailTable<Changelog>()
				.withColumn(new J2HtmlEmailTableColumn<>("", row -> {
					String href = paths.datarouter.changelog.viewExact.toSlashedString()
							+ "?" + ViewExactChangelogHandler.P_reversedDateMs + "="
									+ row.getKey().getMilliTimeReversed()
							+ "&" + ViewExactChangelogHandler.P_changelogType + "=" + row.getKey().getChangelogType()
							+ "&" + ViewExactChangelogHandler.P_name + "=" + row.getKey().getName();
					return digestService.makeATagLink("#", href);
				}))
				.withColumn("Date", row -> row.getKey().getMilliTimeReversed().format(zoneId))
				.withColumn("Type", row -> row.getKey().getChangelogType())
				.withColumn("Name", row -> row.getKey().getName())
				.withColumn("Action", Changelog::getAction)
				.withColumn("User", Changelog::getUsername)
				.build(rows);
	}

}
