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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.web.config.DatarouterAuthSettingRoot;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountDailyDigest implements DailyDigest{

	private static final String TASK_CATEGORY = "account";
	private static final String MISSING_CALLER_TYPE_CATEGORY = "missingCallerType";
	private static final String OLD_CATEGORY = "old";
	private static final Duration THRESHOLD = Duration.ofDays(365);

	@Inject
	private DatarouterAccountDao accountDao;
	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private DailyDigestRmlService digestService;
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

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		return Scanner.concat(
				accountDao.scan()
						.include(account -> account.getLastUsedInstant().isBefore(Instant.now().minus(THRESHOLD)))
						.map(old -> new DailyDigestPlatformTask(
								List.of(TASK_CATEGORY, OLD_CATEGORY, old.getKey().getAccountName()),
								List.of(TASK_CATEGORY, OLD_CATEGORY),
								"Old account: " + old.getKey().getAccountName(),
								Rml.doc(Rml.paragraph(
										Rml.text("Account "),
										Rml.text(old.getKey().getAccountName()).strong(),
										Rml.text(" has not been used in over " + THRESHOLD
												+ ". Consider deleting it."))))),
				accountDao.scan()
						.include(account -> account.getCallerType() == null)
						.map(old -> new DailyDigestPlatformTask(
								List.of(TASK_CATEGORY, MISSING_CALLER_TYPE_CATEGORY, old.getKey().getAccountName()),
								List.of(TASK_CATEGORY, MISSING_CALLER_TYPE_CATEGORY),
								"Missing caller type: " + old.getKey().getAccountName(),
								Rml.doc(Rml.paragraph(
										Rml.text("Account "),
										Rml.text(old.getKey().getAccountName()).strong(),
										Rml.text(" is missing a callerType."))))))
				.list();
	}

	private List<DatarouterAccount> getAccounts(){
		return accountDao.scan()
				.include(account -> account.getLastUsedInstant().isBefore(Instant.now().minus(THRESHOLD))
						|| account.getCallerType() == null)
				.list();
	}

}
