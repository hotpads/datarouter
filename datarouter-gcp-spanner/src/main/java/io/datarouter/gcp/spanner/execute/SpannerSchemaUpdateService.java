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
package io.datarouter.gcp.spanner.execute;

import static j2html.TagCreator.pre;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;

import io.datarouter.gcp.spanner.connection.SpannerDatabaseClientsHolder;
import io.datarouter.gcp.spanner.ddl.SpannerSingleTableSchemaUpdateFactory;
import io.datarouter.gcp.spanner.ddl.SpannerTableOperationsGenerator;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterSchemaUpdateScheduler;
import io.datarouter.storage.config.schema.BaseSchemaUpdateService;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.email.DatarouterHtmlEmailService;

@Singleton
public class SpannerSchemaUpdateService extends BaseSchemaUpdateService{

	private final SpannerSingleTableSchemaUpdateFactory singleTableSchemaUpdateFactory;
	private final SpannerTableOperationsGenerator tableOperationsGenerator;
	private final SpannerDatabaseClientsHolder clientPoolHolder;
	private final DatarouterHtmlEmailService htmlEmailService;
	private final DatarouterWebPaths datarouterWebPaths;

	@Inject
	public SpannerSchemaUpdateService(
			DatarouterProperties datarouterProperties,
			DatarouterAdministratorEmailService adminEmailService,
			SpannerSingleTableSchemaUpdateFactory singleTableSchemaUpdateFactory,
			SpannerTableOperationsGenerator tableOperationsGenerator,
			DatarouterSchemaUpdateScheduler executor,
			SpannerDatabaseClientsHolder clientPoolHolder,
			DatarouterHtmlEmailService htmlEmailService,
			DatarouterWebPaths datarouterWebPaths){
		super(datarouterProperties, adminEmailService, executor);
		this.singleTableSchemaUpdateFactory = singleTableSchemaUpdateFactory;
		this.tableOperationsGenerator = tableOperationsGenerator;
		this.clientPoolHolder = clientPoolHolder;
		this.htmlEmailService = htmlEmailService;
		this.datarouterWebPaths = datarouterWebPaths;
	}

	@Override
	protected Callable<Optional<SchemaUpdateResult>> makeSchemaUpdateCallable(
			ClientId clientId,
			SingletonSupplier<List<String>> existingTableNames,
			PhysicalNode<?,?,?> node){
		return singleTableSchemaUpdateFactory.new SpannerSingleTableSchemaUpdate(clientId, existingTableNames, node);
	}

	@Override
	protected List<String> fetchExistingTables(ClientId clientId){
		List<String> existingTableNames = new ArrayList<>();
		DatabaseClient dbClient = clientPoolHolder.getDatabaseClient(clientId);
		ResultSet resultSet = dbClient.singleUse().executeQuery(Statement.of(tableOperationsGenerator
				.getListOfTables()));
		while(resultSet.next()){
			existingTableNames.add(resultSet.getString("table_name"));
		}
		return existingTableNames;
	}

	@Override
	protected void sendEmail(String fromEmail, String toEmail, String subject, String body){
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(datarouterWebPaths.datarouter)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(subject)
				.withTitle("Spanner Schema Update")
				.withTitleHref(primaryHref)
				.withContent(pre(body));
		htmlEmailService.trySendJ2Html(fromEmail, toEmail, emailBuilder);
	}

}
