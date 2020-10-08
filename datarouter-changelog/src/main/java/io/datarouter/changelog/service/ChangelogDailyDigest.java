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
import static j2html.TagCreator.h3;
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

import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.changelog.storage.ChangelogKey;
import io.datarouter.changelog.web.ViewChangelogHandler;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.digest.DailyDigest;
import j2html.tags.ContainerTag;

@Singleton
public class ChangelogDailyDigest implements DailyDigest{

	@Inject
	private ChangelogDao dao;

	@Override
	public Optional<ContainerTag> getContent(){
		var start = new ChangelogKey(atEndOfDay(), null, null);
		var stop = new ChangelogKey(atStartOfDay(), null, null);
		Range<ChangelogKey> range = new Range<>(start, true, stop, true);
		var list = dao.scan(range).list();
		if(list.size() == 0){
			return Optional.empty();
		}
		return Optional.of(makeContent(list));
	}

	private static ContainerTag makeContent(List<Changelog> rows){
		var header = h3("Changelog");
		var description = small("For the current day");
		var table = ViewChangelogHandler.buildTable(rows);
		return div(header, description, table);
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
