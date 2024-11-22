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
package io.datarouter.auth.web.service;

import static j2html.TagCreator.div;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.web.config.DatarouterAuthSettingRoot;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import j2html.TagCreator;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.SmallTag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountDailyDigest implements DailyDigest{

	private static final Instant THRESHOLD = Instant.now().minus(Duration.ofDays(365));
	private static final SmallTag CAPTION = TagCreator.small("Old Accounts or Accounts without a callerType");

	@Inject
	private DatarouterAccountDao accountDao;
	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterAuthSettingRoot settings;

	@Override
	public String getTitle(){
		return "Accounts";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		if(!settings.enableAccountDailyDigest.get()){
			return Optional.empty();
		}
		List<DatarouterAccount> accounts = getAccounts();
		if(accounts.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Old Accounts", paths.datarouter.accountManager);
		var table = buildEmailTable(accounts, zoneId);
		return Optional.of(div(header, CAPTION, table));
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		if(!settings.enableAccountDailyDigest.get()){
			return Optional.empty();
		}
		List<DatarouterAccount> accounts = getAccounts();
		if(accounts.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(Rml.paragraph(
				digestService.makeHeading("Old Accounts", paths.datarouter.accountManager),
				Rml.text("Old Accounts or Accounts without a callerType").italic(),
				Rml.table(
						Rml.tableRow(
								Rml.tableHeader(Rml.text("Account")),
								Rml.tableHeader(Rml.text("Creator")),
								Rml.tableHeader(Rml.text("Created")),
								Rml.tableHeader(Rml.text("Last Used")),
								Rml.tableHeader(Rml.text("Caller Type"))))
						.with(accounts.stream()
								.map(account -> Rml.tableRow(
										Rml.tableCell(Rml.text(account.getKey().getAccountName())),
										Rml.tableCell(Rml.text(account.getCreator())),
										Rml.tableCell(Rml.text(account.getCreatedDate(zoneId))),
										Rml.tableCell(Rml.text(account.getLastUsedDate(zoneId))),
										Rml.tableCell(Rml.text(account.getCallerType())))))));
	}

	private List<DatarouterAccount> getAccounts(){
		return accountDao.scan()
				.include(account -> account.getLastUsedInstant().isBefore(THRESHOLD)
						|| account.getCallerType() == null)
				.list();
	}

	private static TableTag buildEmailTable(List<DatarouterAccount> rows, ZoneId zoneId){
		return new J2HtmlEmailTable<DatarouterAccount>()
				.withColumn("Account", row -> row.getKey().getAccountName())
				.withColumn("Creator", DatarouterAccount::getCreator)
				.withColumn("Created", row -> row.getCreatedDate(zoneId))
				.withColumn("Last Used", row -> row.getLastUsedDate(zoneId))
				.withColumn("Caller Type", DatarouterAccount::getCallerType)
				.build(rows);
	}

}
