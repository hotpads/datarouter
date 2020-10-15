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
package io.datarouter.changelog.service;

import static j2html.TagCreator.div;
import static j2html.TagCreator.small;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.changelog.storage.ChangelogKey;
import io.datarouter.changelog.web.ViewChangelogHandler;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.email.J2HtmlEmailTable;
import j2html.tags.ContainerTag;

@Singleton
public class ChangelogDailyDigest implements DailyDigest{

	@Inject
	private ChangelogDao dao;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterChangelogPaths paths;

	@Override
	public Optional<ContainerTag> getPageContent(){
		var list = getChangelogs();
		if(list.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Changelog", paths.datarouter.changelog.view);
		var description = small("For the current day");
		var table = ViewChangelogHandler.buildTable(list);
		return Optional.of(div(header, description, table));
	}

	@Override
	public Optional<ContainerTag> getEmailContent(){
		var list = getChangelogs();
		if(list.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Changelog", paths.datarouter.changelog.view);
		var description = small("For the current day");
		var table = buildEmailTable(list);
		return Optional.of(div(header, description, table));
	}

	@Override
	public String getTitle(){
		return "Changelog";
	}

	private List<Changelog> getChangelogs(){
		var start = new ChangelogKey(atEndOfDay(), null, null);
		var stop = new ChangelogKey(atStartOfDay(), null, null);
		Range<ChangelogKey> range = new Range<>(start, true, stop, true);
		return dao.scan(range).list();
	}

	private ContainerTag buildEmailTable(List<Changelog> rows){
		return new J2HtmlEmailTable<Changelog>()
				.withColumn("Date", row -> {
					Long reversedDateMs = row.getKey().getReversedDateMs();
					return new Date(Long.MAX_VALUE - reversedDateMs);
				})
				.withColumn("Type", row -> row.getKey().getChangelogType())
				.withColumn("Name", row -> row.getKey().getName())
				.withColumn("Action", row -> row.getAction())
				.withColumn("User", row -> row.getUsername())
				.build(rows);
	}

	private static long atStartOfDay(){
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
		return Long.MAX_VALUE - localDateTimeToDate(startOfDay).getTime();
	}

	private static long atEndOfDay(){
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
		return Long.MAX_VALUE - localDateTimeToDate(endOfDay).getTime();
	}

	private static Date localDateTimeToDate(LocalDateTime localDateTime){
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

}
