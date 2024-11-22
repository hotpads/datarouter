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

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredentialDao;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterDefaultAccountKeysDailyDigest implements DailyDigest{

	@Inject
	private DefaultDatarouterAccountKeysSupplier defaultDatarouterAccountKeys;
	@Inject
	private DailyDigestService dailyDigestService;
	@Inject
	private DatarouterAccountCredentialDao datarouterAccountCredentialDao;
	@Inject
	private DatarouterAuthPaths authPaths;

	@Override
	public String getTitle(){
		return "Accounts with default keys";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.MEDIUM;
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		List<String> accounts = getAccounts();
		if(accounts.isEmpty()){
			return Optional.empty();
		}
		var header = dailyDigestService.makeHeader(
				"Account Credentials with default api or secret keys",
				authPaths.datarouter.accountManager);
		var table = buildEmailTable(accounts);
		return Optional.of(div(header, table));
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<String> accounts = getAccounts();
		if(accounts.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(
				Rml.paragraph(
						dailyDigestService.makeHeading(
								"Account Credentials with default api or secret keys",
								authPaths.datarouter.accountManager),
						Rml.table(
								Rml.tableRow(Rml.tableHeader(Rml.text("Accounts"))))
								.with(accounts.stream()
										.map(Rml::text)
										.map(Rml::tableCell)
										.map(Rml::tableRow))));
	}

	private List<String> getAccounts(){
		return datarouterAccountCredentialDao.scan()
				.include(credential -> StringTool.equalsCaseInsensitive(credential.getKey().getApiKey(),
						defaultDatarouterAccountKeys.getDefaultApiKey())
						|| StringTool.equalsCaseInsensitive(credential.getSecretKey(),
								defaultDatarouterAccountKeys.getDefaultSecretKey()))
				.map(DatarouterAccountCredential::getAccountName)
				.list();
	}

	private TableTag buildEmailTable(List<String> rows){
		return new J2HtmlEmailTable<String>()
				.withColumn("Accounts", row -> row)
				.build(rows);
	}

}
