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
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
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

	@Override
	public String getTitle(){
		return "Changelog";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		List<Changelog> list = getChangelogs(zoneId);
		if(list.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Changelog", paths.datarouter.changelog.viewAll);
		var description = small("For the current day");
		var table = buildEmailTable(list, zoneId);
		return Optional.of(div(header, description, table));
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<Changelog> list = getChangelogs(zoneId);
		if(list.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(Rml.paragraph(
				digestService.makeHeading("Changelog", paths.datarouter.changelog.viewAll),
				Rml.text("For the current day").italic(),
				Rml.table(
						Rml.tableRow(
								Rml.tableHeader(Rml.text("")),
								Rml.tableHeader(Rml.text("Date")),
								Rml.tableHeader(Rml.text("Type")),
								Rml.tableHeader(Rml.text("Name")),
								Rml.tableHeader(Rml.text("Action")),
								Rml.tableHeader(Rml.text("User"))))
						.with(list.stream()
								.map(log -> Rml.tableRow(
										Rml.tableCell(Rml.text("#").link(changelogLink(log))),
										Rml.tableCell(Rml.timestamp(
												log.getKey().getMilliTimeReversed().format(zoneId),
												log.getKey().getMilliTimeReversed().toEpochMilli())),
										Rml.tableCell(Rml.text(log.getKey().getChangelogType())),
										Rml.tableCell(Rml.text(log.getKey().getName())),
										Rml.tableCell(Rml.text(log.getAction())),
										Rml.tableCell(Rml.text(log.getUsername())))))));
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
				.withColumn(new J2HtmlEmailTableColumn<>(
						"",
						row -> digestService.makeATagLink("#", changelogLink(row))))
				.withColumn("Date", row -> row.getKey().getMilliTimeReversed().format(zoneId))
				.withColumn("Type", row -> row.getKey().getChangelogType())
				.withColumn("Name", row -> row.getKey().getName())
				.withColumn("Action", Changelog::getAction)
				.withColumn("User", Changelog::getUsername)
				.build(rows);
	}

	private String changelogLink(Changelog log){
		ChangelogKey key = log.getKey();

		return paths.datarouter.changelog.viewExact.toSlashedString()
				+ "?" + ViewExactChangelogHandler.P_reversedDateMs + "=" + key.getMilliTimeReversed()
				+ "&" + ViewExactChangelogHandler.P_changelogType + "=" + key.getChangelogType()
				+ "&" + ViewExactChangelogHandler.P_name + "=" + key.getName();
	}

}
